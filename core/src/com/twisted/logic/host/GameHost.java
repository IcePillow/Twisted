package com.twisted.logic.host;

import com.badlogic.gdx.math.Vector2;
import com.twisted.local.game.state.PlayerFile;
import com.twisted.logic.Player;
import com.twisted.logic.descriptors.CurrentJob;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.*;
import com.twisted.logic.entities.attach.StationTransport;
import com.twisted.logic.entities.attach.Weapon;
import com.twisted.logic.mobs.Mobile;
import com.twisted.net.msg.*;
import com.twisted.net.msg.gameReq.*;
import com.twisted.net.msg.gameUpdate.*;
import com.twisted.net.msg.remaining.MDenyRequest;
import com.twisted.net.msg.remaining.MGameStart;
import com.twisted.net.msg.remaining.MSceneChange;
import com.twisted.net.server.Server;
import com.twisted.net.server.ServerContact;

import java.text.DecimalFormat;
import java.util.*;

/**
 * TODO safety checks on all inputs from the users in the client request handling methods.
 */
public class GameHost implements ServerContact {

    /* Constants */

    public static final int TICK_DELAY = 50; //millis between each tick
    public static final float FRAC = TICK_DELAY / 1000f; //fraction of a second per tick

    private static final DecimalFormat df2 = new DecimalFormat("0.00");


    /* Exterior Reference Variables */

    private final Server server;


    /* Storage Variables */

    //player details
    private final HashMap<Integer, Player> players;
    private final int hostId;

    //networking and game loop
    private final HashMap<MGameReq, Integer> requests; //requests being read during game logic
    private final Map<MGameReq, Integer> hotRequests; //requests stored between ticks (synchronized)
    private boolean looping;
    private float millisSinceMajor;

    //map details
    private int mapWidth;
    private int mapHeight;

    //state variables
    private Grid[] grids;
    private HashMap<Integer, Ship> shipsInWarp;

    //tracking variables, should only be accessed through their respective sync'd methods
    private int nextJobId = 1;
    public synchronized int useNextJobId(){
        nextJobId++;
        return nextJobId-1;
    }
    private int nextShipId = 1;
    public synchronized int useNextShipId(){
        nextShipId++;
        return nextShipId-1;
    }
    private int nextMobileId = 1;
    public synchronized int useNextMobileId(){
        nextMobileId++;
        return nextMobileId-1;
    }


    /* Constructing & Launching */

    /**
     * Constructor. Makes itself the contact of the server.
     * @param players The array of players. Should not exceed the max number for the map.
     */
    public GameHost(Server server, HashMap<Integer, Player> players, int hostId){
        //initialize
        this.requests = new HashMap<>();
        this.hotRequests = Collections.synchronizedMap(new HashMap<>());
        this.looping = true;

        //copy players over
        this.hostId = hostId;
        this.players = players;

        //create ai players
        for(int i = this.players.size(); i < 2; i++) {
            int id = server.useNextId();

            Player aiPlayer = new Player(server, id, "AI"+id, true);
            this.players.put(id, aiPlayer);
            players.put(id, aiPlayer);
        }

        //make this the server contact and set the server
        this.server = server;
        this.server.setContact(this);

        //tell players to change scene
        MSceneChange sceneChange = new MSceneChange(MSceneChange.Change.GAME);
        sceneChange.isPlayer = true;
        for(Player p : players.values()){
            p.sendMessage(sceneChange);
        }

        //thread that starts the game
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //load initial game state
            loadInitialState(players.values().toArray(new Player[0]));

            //tell the players the information about the game starting
            sendGameStart();

            preLoopCalls();

            //begin the game loop
            startGameLoop();

        }).start();

    }

    /**
     * Loads the initial game state.
     * IMPORTANT - ONLY LOADS CLASSIC MAP
     */
    private void loadInitialState(Player[] players){

        //map basics
        mapWidth = 1000;
        mapHeight = 1000;

        //grids and stations
        grids = new Grid[]{
                new Grid(0, new Vector2(50, 320), "A"),
                new Grid(1, new Vector2(200, 100), "B"),
                new Grid(2, new Vector2(80, 920), "C"),
                new Grid(3, new Vector2(950, 680), "D"),
                new Grid(4, new Vector2(800, 900), "E"),
                new Grid(5, new Vector2(920, 80), "F"),
                new Grid(6, new Vector2(400, 500), "G"),
                new Grid(7, new Vector2(500, 400), "H"),
        };
        grids[0].station = new Extractor(0, grids[0].nickname, players[0].getId(), Station.Stage.SHIELDED, false);
        grids[1].station = new Extractor(1, grids[1].nickname, players[0].getId(), Station.Stage.SHIELDED, false);
        grids[2].station = new Harvester(2, grids[2].nickname, players[0].getId(), Station.Stage.ARMORED, false);
        grids[3].station = new Extractor(3, grids[3].nickname, players[1].getId(), Station.Stage.SHIELDED, false);
        grids[4].station = new Extractor(4, grids[4].nickname, players[1].getId(), Station.Stage.SHIELDED, false);
        grids[5].station = new Harvester(5, grids[5].nickname, players[1].getId(), Station.Stage.ARMORED, false);
        grids[6].station = new Liquidator(6, grids[6].nickname, 0, Station.Stage.RUBBLE, false);
        grids[7].station = new Liquidator(7, grids[7].nickname, 0, Station.Stage.RUBBLE, false);

        //add initial resources
        grids[0].station.resources[0] += 20;
        grids[0].station.resources[1] += 6;
        grids[0].station.resources[2] += 2;
        grids[3].station.resources[0] += 20;
        grids[3].station.resources[1] += 6;
        grids[3].station.resources[2] += 2;

        //set initial timers
        grids[2].station.stageTimer = 60;
        grids[5].station.stageTimer = 60;

        //warp
        shipsInWarp = new HashMap<>();
    }

    /**
     * Sends the game start message that contains the details for the initial game state.
     */
    private void sendGameStart(){

        //create maps of ids to player names and colors
        HashMap<Integer, String> idToName = new HashMap<>();
        for (Player p : players.values()){
            idToName.put(p.getId(), p.name);
        }
        HashMap<Integer, PlayerFile> idToFile = new HashMap<>();
        int i = 0;
        for(Player p : players.values()){
            if(i==0){
                idToFile.put(p.getId(), PlayerFile.BLUE);
            }
            else if(i==1){
                idToFile.put(p.getId(), PlayerFile.ORANGE);
            }
            else break;
            i++;
        }

        //create the message
        MGameStart msg = new MGameStart(idToName, idToFile, grids.length);

        //fill in the message
        msg.tickDelay = TICK_DELAY;
        msg.mapWidth = mapWidth;
        msg.mapHeight = mapHeight;

        //fill in the grid parts of the message
        for(int j=0; j<grids.length; j++){
            Grid g = grids[j];

            msg.gridPositions[j] = g.pos;
            msg.gridNicknames[j] = g.nickname;
            msg.stationTypes[j] = g.station.getType();
            msg.stationOwners[j] = g.station.owner;
            msg.stationStages[j] = g.station.stage;
            msg.stationResources[j] = g.station.resources;
        }

        //send the message to each player
        for(Player p : players.values()){
            //set the player's name
            msg.yourPlayerId = p.getId();
            //and send the message to the player
            p.sendMessage(msg);
        }

    }

    /**
     * Any calls that should be made after the game state is initialized but before the game loop
     * begins. Mostly for development.
     */
    private void preLoopCalls(){

        //dev ships
        Ship s1 = new Frigate(useNextShipId(), 0, 2, false);
        s1.pos.set(1, 1);
        s1.rot = (float) Math.PI/2;
        grids[0].ships.put(s1.id, s1);
        server.broadcastMessage(MAddShip.createFromShipBody(s1));

        Ship s2 = new Frigate(useNextShipId(), 0, 1, false);
        s2.pos.set(-0.9f, 0.9f);
        s2.rot = (float) -Math.PI/2;
        grids[0].ships.put(s2.id, s2);
        server.broadcastMessage(MAddShip.createFromShipBody(s2));
    }


    /* ServerContact Methods */

    /**
     * Called when the server receives a message from a client.
     */
    @Override
    public void serverReceived(int clientId, Message message) {
        if(message instanceof MGameReq){
            hotRequests.put((MGameReq) message, clientId);
        }
    }

    /**
     * Should never be called in this GameHost.
     */
    @Override
    public void clientConnected(int clientId) {

    }

    @Override
    public void clientDisconnected(int clientId, String reason) {
        //TODO this function
    }

    @Override
    public void clientLostConnection(int clientId) {
        //TODO this function
    }


    /* Game Loop */

    /**
     * Starts the game loop thread.
     */
    private void startGameLoop(){
        new Thread(() -> {
            //declare
            long sleepTime, startTime;

            //the game loop
            while(looping){
                startTime = System.currentTimeMillis();

                //run the methods
                prepRequests();
                loop();

                //sleep
                sleepTime = startTime+TICK_DELAY - System.currentTimeMillis();
                if(sleepTime > 0){
                    try {
                        Thread.sleep(sleepTime);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    System.out.println("[Warning] Game loop sleep time was " + sleepTime);
                }
            }

        }).start();
    }

    /**
     * Prepares the message arrays for the game loop by copying from hotRequests to requests.
     */
    private void prepRequests(){
        requests.clear();
        requests.putAll(hotRequests);
        hotRequests.clear();
    }

    /**
     * The function where the game logic is performed.
     */
    private void loop(){
        //handle requests
        for(MGameReq r : requests.keySet()){
            int id = requests.get(r);

            if(r instanceof MJobReq) handleJobReq(id, (MJobReq) r);
            else if(r instanceof MShipMoveReq) handleShipMoveReq(id, (MShipMoveReq) r);
            else if(r instanceof MShipAlignReq) handleShipAlignReq(id, (MShipAlignReq) r);
            else if(r instanceof MShipWarpReq) handleShipWarpReq(id, (MShipWarpReq) r);
            else if(r instanceof MTargetReq) handleShipTargetReq(id, (MTargetReq) r);
            else if(r instanceof MShipOrbitReq) handleShipOrbitReq(id, (MShipOrbitReq) r);
            else if(r instanceof MWeaponActiveReq) handleWeaponActiveReq(id, (MWeaponActiveReq) r);
            else if(r instanceof MShipStopReq) handleShipStopReq(id, (MShipStopReq) r);
            else if(r instanceof MShipUndockReq) handleShipUndockReq(id, (MShipUndockReq) r);
            else if(r instanceof MShipDockReq) handleShipDockReq(id, (MShipDockReq) r);
            else if(r instanceof MPackedStationMoveReq) handlePackedStationMoveReq(id, (MPackedStationMoveReq) r);
        }

        //updating
        updateStationTimers();
        calculateTrajectories();
        updatePhysics();
        updateCombat();

        //telling clients
        sendEntMobData();

        //major update
        millisSinceMajor += TICK_DELAY;
        if(millisSinceMajor > 1000){
            millisSinceMajor -= 1000;
            majorLoop();
        }
    }

    /**
     * A function approximately called once per second to do less frequent updates. It does not
     * change game state, only updates users.
     */
    private void majorLoop(){

        HashMap<Integer, MGameOverview> overviews = new HashMap<>(); //playerId -> message

        //create message shells
        for(Player player : players.values()) {
            overviews.put(player.getId(), new MGameOverview());
        }

        for(Grid g : grids){
            //fill station timers
            for(CurrentJob j : g.station.currentJobs){
                overviews.get(j.owner).jobToTimeLeft.put(j.jobId, j.timeLeft);
            }

            //fill station resources
            Station s = g.station;
            if(s.owner > 0){ //check there is an owner
                overviews.get(s.owner).stationToResources.put(s.grid, s.resources.clone());
            }

            //fill station stage timer
            for(MGameOverview o : overviews.values()){
                o.stationStageTimer.put(s.getId(), s.stageTimer);
            }
        }

        //send messages
        for(Map.Entry<Integer, MGameOverview> e : overviews.entrySet()) {
            if(!players.get(e.getKey()).ai){
                server.sendMessage(e.getKey(), e.getValue());
            }
        }
    }


    /* Game Loop Methods */

    /**
     * Updates the station state due to time-based events.
     */
    private void updateStationTimers(){
        for(Grid grid : grids){
            Station st = grid.station;

            //job updates
            if(st.currentJobs.size() > 0) {
                //otherwise, grab the first job
                CurrentJob job = st.currentJobs.get(0);
                if(!job.blocking) job.timeLeft -= FRAC;

                if(job.timeLeft <= 0){
                    boolean jobBlocking = false;

                    //create the ship or the packaged station
                    Ship sh = null;
                    switch(job.jobType){
                        case Frigate:
                            sh = new Frigate(useNextShipId(), grid.id, job.owner,true);
                            break;
                        case Barge:
                            sh = new Barge(useNextShipId(), grid.id, job.owner, true);
                            break;
                        //TODO add the rest of the ship cases
                        case Extractor:
                        case Harvester:
                        case Liquidator:
                            //add the packed station to the inventory
                            int idx;
                            for(idx=0; idx<st.packedStations.length; idx++){
                                if(st.packedStations[idx] == null){
                                    st.packedStations[idx] = job.jobType.getPackedStationType();
                                    break;
                                }
                                else if(idx == st.packedStations.length-1){
                                    jobBlocking = true;
                                    break;
                                }
                            }

                            //was successfully able to add it
                            if(!jobBlocking){
                                //tell players
                                server.broadcastMessage(new MPackedStationMove(null,
                                        -1, EntPtr.createFromEntity(st), idx,
                                        job.jobType.getPackedStationType()));
                            }
                            break;
                        default:
                            System.out.println("Unexpected job type in GameHost.updateStationTimers()");
                            break;
                    }

                    //deal with a created ship
                    if(sh != null){
                        //add ship to world
                        if(!sh.docked){
                            grid.ships.put(sh.id, sh);
                        }
                        else {
                            grid.station.dockedShips.put(sh.id, sh);
                        }

                        //tell users
                        server.broadcastMessage(MAddShip.createFromShipBody(sh));
                    }

                    //end the job and tell the user
                    if(!jobBlocking){
                        st.currentJobs.remove(0);
                        server.sendMessage(job.owner, new MChangeJob(MChangeJob.Action.FINISHED,
                                job.jobId, job.grid, null));
                    }
                    //tell the user the job is blocking
                    else if(!job.blocking){
                        job.blocking = true;
                        server.sendMessage(job.owner, new MChangeJob(MChangeJob.Action.BLOCKING,
                                job.jobId, job.grid, null));
                    }
                }
            }

            //stage timer
            st.stageTimer -= FRAC;
            if(st.stageTimer <= 0){
                st.stageTimer = 0;

                //stage updates
                switch(st.stage){
                    case VULNERABLE:
                        st.stage = Station.Stage.SHIELDED;
                        st.shieldHealth = (float) Math.ceil(0.25f * st.getMaxShield());

                        server.broadcastMessage(MStationStage.createFromStation(st));
                        break;
                    case ARMORED:
                        st.stage = Station.Stage.VULNERABLE;
                        st.stageTimer = st.getVulnerableDuration();

                        server.broadcastMessage(MStationStage.createFromStation(st));
                        break;
                    case DEPLOYMENT:
                        st.stage = Station.Stage.SHIELDED;
                        st.shieldHealth = st.getMaxShield();
                        st.hullHealth = st.getMaxHull();

                        server.broadcastMessage(MStationStage.createFromStation(st));
                        break;
                }
            }
        }
    }

    /**
     * Calculates the trajectory the ship wants to take to reach its desired location or velocity.
     */
    private void calculateTrajectories(){
        //variables being used each loop
        float distanceToTarget;
        Vector2 accel = new Vector2(0, 0);

        for(Grid g : grids){
            for(Ship s : g.ships.values()) {
                //check there is a targetPos and that it is not already close enough
                if(s.movement == Ship.Movement.STOPPING){
                    s.trajectoryVel.set(0, 0);
                    if(s.vel.len() == 0){
                        s.moveCommand = "Stationary";
                    }
                }
                else if(s.movement == Ship.Movement.MOVE_TO_POS){
                    //already close enough to target position
                    if((distanceToTarget=s.pos.dst(s.moveTargetPos)) <= 0.01f){
                        s.trajectoryVel.set(0, 0);
                        if(s.vel.len() == 0){
                            s.movement = Ship.Movement.STOPPING;
                            s.moveCommand = "Stationary";
                        }
                    }
                    else {
                        //set the targetVel to the correct direction and normalize
                        s.trajectoryVel.set(s.moveTargetPos.x - s.pos.x,
                                s.moveTargetPos.y - s.pos.y).nor();

                        //find the effective max speed (compare speed can stop from to actual max speed)
                        float speedCanStopFrom = (float) Math.sqrt( 2*s.getMaxAccel()*distanceToTarget );
                        float effectiveMaxSpeed = Math.min(0.8f*speedCanStopFrom, s.getMaxSpeed());

                        //update the target velocity
                        s.trajectoryVel.x *= effectiveMaxSpeed;
                        s.trajectoryVel.y *= effectiveMaxSpeed;
                    }
                }
                else if(s.movement == Ship.Movement.ALIGN_TO_ANG){
                    //placeholder, empty
                }
                else if(s.movement == Ship.Movement.ORBIT_ENT){
                    Entity tar = g.retrieveEntity(s.moveTargetEntType, s.moveTargetEntId);

                    //TODO handle orbiting better at smaller radii
                    if(tar == null){
                        s.movement = Ship.Movement.STOPPING;
                        s.moveCommand = "Stationary";
                        s.trajectoryVel.set(0, 0);
                    }
                    else {
                        //get the angles in positive degrees
                        double relPosAng = Math.atan2(tar.pos.y-s.pos.y, tar.pos.x-s.pos.x)*180/Math.PI;
                        if(relPosAng < 0) relPosAng += 360;
                        double velAng = Math.atan2(s.vel.y, s.vel.x)*180/Math.PI;
                        if(velAng < 0) velAng += 360;

                        //point on the orbit that the line between ship and target intersects
                        Vector2 oPoint = new Vector2(s.pos.x-tar.pos.x, s.pos.y-tar.pos.y)
                                .nor().scl(s.moveRelativeDist).add(tar.pos.x, tar.pos.y);

                        //vector to represent the direction of travel
                        Vector2 oPath = new Vector2(s.pos.x-tar.pos.x, s.pos.y-tar.pos.y)
                                .nor().scl(0.9f*s.getMaxSpeed());

                        if((velAng > relPosAng && velAng < relPosAng+180)
                           || (velAng+360 > relPosAng && velAng+360 < relPosAng+180)){
                            oPath.rotateRad((float) -Math.PI/2);
                        }
                        else {
                            oPath.rotateRad((float) Math.PI/2);
                        }

                        //set the trajectory
                        s.trajectoryVel.set(oPoint.x-s.pos.x+oPath.x, oPoint.y-s.pos.y+oPath.y);
                    }
                }
                else if(s.movement == Ship.Movement.MOVE_FOR_DOCK){
                    s.trajectoryVel.set(s.moveTargetPos.x-s.pos.x, s.moveTargetPos.y-s.pos.y);
                }

                //accelerate the ship
                if(s.trajectoryVel != null){
                    //find the desired, then clamp it
                    accel.set(s.trajectoryVel.x-s.vel.x, s.trajectoryVel.y-s.vel.y);

                    float length = accel.len();
                    if(length > s.getMaxAccel() * FRAC){
                        accel.x *= s.getMaxAccel() * FRAC /length;
                        accel.y *= s.getMaxAccel() * FRAC /length;
                    }

                    //update the velocity
                    s.vel.x += accel.x;
                    s.vel.y += accel.y;
                }
            }
        }
    }

    /**
     * Updates the physics of each grid.
     */
    private void updatePhysics(){
        //utility objects
        ArrayList<Ship> shipsToRemove = new ArrayList<>();
        ArrayList<Integer> mobilesToRemove = new ArrayList<>(); //mobile id

        //time step for ships not in warp
        for(Grid g : grids){
            //loop through ships on the grid
            for(Ship s : g.ships.values()){
                //move the ship
                s.pos.x += s.vel.x * FRAC;
                s.pos.y += s.vel.y * FRAC;

                //set the rotation
                if(s.vel.len() != 0){
                    s.rot = (float) Math.atan2(s.vel.y, s.vel.x);
                }

                //check if it should enter warp
                if(s.movement == Ship.Movement.ALIGN_FOR_WARP
                    && s.alignedForWarp(g, grids[s.warpTargetGridId])){
                    //remove the ship at the end of the loop
                    shipsToRemove.add(s);

                    //put in warp space
                    shipsInWarp.put(s.id, s);
                    s.warpTimeToLand = 3; //TODO set this based on dist/speed/etc

                    //update the movement description
                    s.movement = Ship.Movement.WARPING;
                    s.moveCommand = "Warping to " + grids[s.warpTargetGridId].nickname;

                    //update physics
                    s.pos.set(0, 0);
                    s.grid = -1;

                    //update targeting
                    s.targetingState = null;
                    s.targetEntity = null;

                    //tell the users
                    server.broadcastMessage(new MShipEnterWarp(s.id, g.id, s.warpTargetGridId));
                }

                //check if the ship should dock
                if(s.movement == Ship.Movement.MOVE_FOR_DOCK){
                    Station st = g.station;
                    //dock
                    if(s.pos.dst(st.pos) <= st.getDockingRadius() && st.owner == s.owner){
                        dockShipAtStation(s, st, g);
                    }
                    //owner changed
                    else if(st.owner != s.owner) {
                        s.movement = Ship.Movement.STOPPING;
                        s.moveCommand = "Stopping";
                    }
                }
            }

            //remove ships that left the grid
            for(Ship s : shipsToRemove){
                g.ships.remove(s.id);
            }
            shipsToRemove.clear();
        }

        //time step for ships in warp
        for(Ship s : shipsInWarp.values()){
            //move the ship along
            s.warpTimeToLand -= FRAC;

            if(s.warpTimeToLand <= 0){
                s.warpTimeToLand = 0;

                shipsToRemove.add(s);

                //tell the users
                server.broadcastMessage(new MShipExitWarp(s.id, s.warpTargetGridId));

                //add it to the correct grid
                grids[s.warpTargetGridId].ships.put(s.id, s);
                s.grid = s.warpTargetGridId;

                //exiting warp
                s.warpTargetGridId = -1;
                s.moveCommand = "Stopping";
                s.movement = Ship.Movement.STOPPING;

                //placing with correct physics
                Vector2 warpVel = s.vel.cpy().nor().scl(1 + s.getMaxSpeed());
                s.pos.set(-warpVel.x, -warpVel.y); //TODO place ship in better place

                warpVel.nor().scl(s.getMaxSpeed()*0.75f);
                s.vel.set(warpVel.x, warpVel.y);
            }
        }
        //remove ships that exited warp
        for(Ship s : shipsToRemove){
            shipsInWarp.remove(s.id);
        }

        //time step for mobiles
        for(Grid g : grids) {
            //loop through mobiles on the grid
            for (Mobile m : g.mobiles.values()) {
                //take the time step and check for fizzle
                if(m.update(FRAC, g)) {
                    mobilesToRemove.add(m.id);

                    //tell clients
                    server.broadcastMessage(MMobileUps.createFromMobile(m, g.id, true));
                }
            }
            //remove mobiles that fizzle
            for(Integer i : mobilesToRemove){
                g.mobiles.remove(i);
            }
            mobilesToRemove.clear();
        }
    }

    /**
     * Updates combat mechanics.
     */
    private void updateCombat(){

        ArrayList<Ship> toBeRemoved = new ArrayList<>();

        for(Grid g : grids){
            //targeting and weapons
            for(Ship s : g.ships.values()){
                if(s.targetingState == Ship.Targeting.Locking){
                    s.targetTimeToLock -= FRAC;

                    if(s.targetTimeToLock <= 0){
                        s.targetingState = Ship.Targeting.Locked;
                    }
                }

                //get the target entity
                Entity target = (s.targetEntity==null) ? null : s.targetEntity.retrieveFromGrid(g);

                //stop targeting if not valid to target (doesn't exist or range checks)
                if(target == null || target.pos.dst(s.pos) > s.getTargetRange()){
                    s.targetingState = null;
                    s.targetEntity = null;
                }
                //update weapons
                for(Weapon w : s.weapons){
                    w.tick(this, g, s, target, s.targetingState, FRAC);
                }
            }

            //ship health checks
            for(Ship s : g.ships.values()){
                if(s.health <= 0){
                    server.broadcastMessage(new MRemShip(s.id, g.id, Ship.Removal.EXPLOSION, false));
                    toBeRemoved.add(s);
                }
            }
            //remove ships
            for(Ship s : toBeRemoved){
                g.ships.remove(s.id);
            }

            //station health checks
            Station st = g.station;
            if(st.stage == Station.Stage.SHIELDED && st.shieldHealth <= 0){
                st.shieldHealth = 0;
                st.stageTimer = st.getArmoredDuration();
                st.stage = Station.Stage.ARMORED;
                server.broadcastMessage(MStationStage.createFromStation(st));
            }
            if(st.stage == Station.Stage.VULNERABLE && st.hullHealth <= 0){
                //change to rubble
                g.station.hullHealth = 0;
                g.station.stage = Station.Stage.RUBBLE;
                g.station.owner = 0;
                server.broadcastMessage(MStationStage.createFromStation(st));

                //other updates
                st.currentJobs.clear();
                Arrays.fill(st.packedStations, null);
                st.dockedShips.clear();
            }
        }
    }

    /**
     * Send entity data to the users.
     */
    private void sendEntMobData(){
        //ship data, not in warp then in warp
        for(Grid g : grids){
            for(Ship s : g.ships.values()){
                server.broadcastMessage(MShipUpd.createFromShip(s));
            }
        }
        for(Ship s : shipsInWarp.values()){
            server.broadcastMessage(MShipUpd.createFromShip(s));
        }

        //station data
        for(Grid g : grids){
            server.broadcastMessage(MStationUpd.createFromStation(g.station));
        }

        //mobile data
        for(Grid g : grids){
            for(Mobile m : g.mobiles.values()){
                server.broadcastMessage(MMobileUps.createFromMobile(m, g.id, false));
            }
        }
    }


    /* Client Request Handling */

    private void handleJobReq(int userId, MJobReq msg){
        Station s = (Station) findEntityInState(Entity.Type.Station, msg.stationGrid, msg.stationGrid);
        Station.Job j = msg.job;
        if(s == null) {
            System.out.println("Couldn't find ship in GameHost.handleJobRequest()");
            return;
        }

        //reject if conditions not met
        if(s.owner != userId || !s.enoughForJob(j) || s.stage == null ||
                s.stage == Station.Stage.DEPLOYMENT){

            MDenyRequest deny = new MDenyRequest(msg);
            if(!s.enoughForJob(j)){
                deny.reason = "Not enough resources for job: " + j.name();
            }
            else if(s.stage == null || s.stage == Station.Stage.DEPLOYMENT) {
                deny.reason = s.getFullName() + " cannot build right now";
            }
            else {
                deny.reason = "Job denied for unexpected reason";
            }

            server.sendMessage(userId, deny);
        }
        //otherwise accept
        else {
            //update on the serverside
            s.removeResourcesForJob(j);
            CurrentJob currentJob = new CurrentJob(useNextJobId(), userId, j, s.grid, j.duration);
            s.currentJobs.add(currentJob);

            //send information to the client
            server.sendMessage(userId, new MChangeJob(MChangeJob.Action.ADDING,
                    currentJob.jobId, currentJob.grid, currentJob));
        }
    }

    private void handleShipMoveReq(int userId, MShipMoveReq msg){
        //get the ship
        Ship s = (Ship) findEntityInState(Entity.Type.Ship, msg.shipId, msg.grid);
        if(s == null){
            System.out.println("Couldn't find ship in GameHost.handleShipMoveRequest()");
            return;
        }

        //check permissions
        if(s.owner != userId || s.grid == -1 || s.docked){
            MDenyRequest deny = new MDenyRequest(msg);

            if(s.grid == -1){
                deny.reason = "Cannot command a ship that is currently in warp";
            }
            else if(s.docked){
                deny.reason = "Cannot command a docked ship";
            }
            else {
                deny.reason = "Cannot command ship for unexpected reason";
            }

            server.sendMessage(userId, deny);
        }
        else {
            //set the ship's movement
            s.movement = Ship.Movement.MOVE_TO_POS;
            s.moveTargetPos = msg.location;

            //set the description of the movement
            s.moveCommand = "Moving to (" + df2.format(s.moveTargetPos.x) + ", "
                    + df2.format(s.moveTargetPos.y) + ")";
        }

    }

    private void handleShipOrbitReq(int userId, MShipOrbitReq msg){
        //get the ship
        Ship s = (Ship) findEntityInState(Entity.Type.Ship, msg.shipId, msg.grid);
        if(s == null){
            System.out.println("Couldn't find ship in GameHost.handleShipMoveRequest()");
            return;
        }

        //check permissions
        if(s.owner != userId || s.grid == -1 || s.id == msg.targetId || s.docked){
            MDenyRequest deny = new MDenyRequest(msg);

            if(s.grid == -1){
                deny.reason = "Cannot command a ship that is currently in warp";
            }
            else if(s.id == msg.targetId){
                deny.reason = "Ship cannot orbit itself";
            }
            else if(s.docked){
                deny.reason = "Cannot command a docked ship";
            }
            else {
                deny.reason = "Cannot command ship for unexpected reason";
            }

            server.sendMessage(userId, deny);
        }
        else {
            Entity ent;
            if((ent = grids[msg.grid].retrieveEntity(msg.targetType, msg.targetId)) != null){
                //set the ship's movement
                s.movement = Ship.Movement.ORBIT_ENT;
                s.moveTargetEntType = msg.targetType;
                s.moveTargetEntId = msg.targetId;
                s.moveRelativeDist = msg.radius;

                //set the description
                if(ent instanceof Station){
                    s.moveCommand = "Orbiting station";
                }
                else if(ent instanceof Ship) {
                    s.moveCommand = "Orbiting " + ((Ship) ent).getType();
                }
            }
        }
    }

    private void handleShipAlignReq(int userId, MShipAlignReq msg){
        //get the ship
        Ship s = (Ship) findEntityInState(Entity.Type.Ship, msg.shipId, msg.grid);
        if(s == null){
            System.out.println("Couldn't find ship in GameHost.handleShipMoveRequest()");
            return;
        }

        //check permissions
        if(s.owner != userId || s.grid == -1 || s.docked){
            MDenyRequest deny = new MDenyRequest(msg);

            if(s.grid == -1){
                deny.reason = "Cannot command a ship that is currently in warp";
            }
            else if(s.docked){
                deny.reason = "Cannot command a docked ship";
            }
            else {
                deny.reason = "Cannot command ship for unexpected reason";
            }

            server.sendMessage(userId, deny);
        }
        else {
            //set the ship's movement
            s.movement = Ship.Movement.ALIGN_TO_ANG;
            s.trajectoryVel = s.trajectoryVel.set(
                    (float) Math.cos(msg.angle) * s.getMaxSpeed() * 0.8f,
                    (float) Math.sin(msg.angle) * s.getMaxSpeed() * 0.8f);

            //set the description of the movement
            int degrees = -((int) (msg.angle*180/Math.PI - 90));
            if(degrees < 0) degrees += 360;
            s.moveCommand = "Alinging to " + degrees + " N";
        }
    }

    private void handleShipWarpReq(int userId, MShipWarpReq msg){
        //get the ship
        Ship s = (Ship) findEntityInState(Entity.Type.Ship, msg.shipId, msg.grid);
        if(s == null){
            System.out.println("Couldn't find ship in GameHost.handleShipWarpRequest()");
            return;
        }

        //check permissions and basics
        if(s.owner != userId || s.grid == -1 || s.docked){
            MDenyRequest deny = new MDenyRequest(msg);

            if(s.grid == -1){
                deny.reason = "Cannot command a ship that is currently in warp";
            }
            else if(s.docked){
                deny.reason = "Cannot command a docked ship";
            }
            else {
                deny.reason = "Cannot command ship for unexpected reason";
            }

            server.sendMessage(userId, deny);
        }
        else{
            //set the target grid id
            s.movement = Ship.Movement.ALIGN_FOR_WARP;
            s.warpTargetGridId = msg.targetGridId;
            s.moveCommand = "Aligning to grid " + grids[msg.targetGridId].nickname;

            //get the angle to the target grid
            float angle = (float) Math.atan2(grids[msg.targetGridId].pos.y-grids[msg.grid].pos.y,
                    grids[msg.targetGridId].pos.x-grids[msg.grid].pos.x);
            s.trajectoryVel = s.trajectoryVel.set(
                    (float) Math.cos(angle) * s.getMaxSpeed() * 0.8f,
                    (float) Math.sin(angle) * s.getMaxSpeed() * 0.8f);
        }
    }

    private void handleShipTargetReq(int userId, MTargetReq msg){
        //get the ship
        Ship s = (Ship) findEntityInState(Entity.Type.Ship, msg.shipId, msg.grid);
        if(s == null){
            System.out.println("Couldn't find ship in GameHost.handleShipTargetRequest()");
            return;
        }

        //check permissions and basics
        if(s.owner != userId || s.grid == -1 || s.docked){
            MDenyRequest deny = new MDenyRequest(msg);

            if(s.grid == -1){
                deny.reason = "Cannot command a ship that is currently in warp";
            }
            else if(s.docked){
                deny.reason = "Cannot command a docked ship";
            }
            else {
                deny.reason = "Cannot command ship for unexpected reason";
            }

            server.sendMessage(userId, deny);
        }
        else {
            //get the target entity
            Entity target = findEntityInState(msg.targetType, msg.targetId, msg.grid);

            //start locking on to the target
            if(target != null && target.pos.dst(s.pos) <= s.getTargetRange()){
                s.targetingState = Ship.Targeting.Locking;
                s.targetEntity = EntPtr.createFromEntity(target);

                //TODO calculate locking time based on entity types
                s.targetTimeToLock = 1;
            }
            //not in range
            else if(target != null){
                MDenyRequest deny = new MDenyRequest(msg);
                deny.reason = "Target not in targeting range";
                server.sendMessage(userId, deny);
            }
            //cancel any locking
            else {
                s.targetingState = null;
                s.targetEntity = null;
            }
        }
    }

    private void handleWeaponActiveReq(int userId, MWeaponActiveReq msg){
        //get the ship
        Ship s = (Ship) findEntityInState(Entity.Type.Ship, msg.shipId, msg.grid);
        if(s == null){
            System.out.println("Couldn't find ship in GameHost.handleWeaponActiveRequest()");
            return;
        }

        //check permissions
        if(s.owner != userId || s.docked){
            MDenyRequest deny = new MDenyRequest(msg);


            if(s.docked){
                deny.reason = "Cannot command a docked ship";
            }
            else {
                deny.reason = "Cannot command ship for unexpected reason";
            }

            server.sendMessage(userId, deny);
        }
        else {
            s.weapons[msg.weaponId].active = msg.active;
        }
    }

    private void handleShipStopReq(int userId, MShipStopReq msg){
        //get the ship
        Ship s = (Ship) findEntityInState(Entity.Type.Ship, msg.shipId, msg.grid);
        if(s == null){
            System.out.println("Couldn't find ship in GameHost.handleShipStopRequest()");
            return;
        }

        //check permissions
        if(s.owner != userId || s.grid == -1 || s.docked){
            MDenyRequest deny = new MDenyRequest(msg);

            if(s.grid == -1){
                deny.reason = "Cannot command a ship that is currently in warp";
            }
            else if(s.docked){
                deny.reason = "Cannot command a docked ship";
            }
            else {
                deny.reason = "Cannot command ship for unexpected reason";
            }

            server.sendMessage(userId, deny);
        }
        else {
            //set the ship's movement
            s.movement = Ship.Movement.STOPPING;

            //set the description of the movement
            s.moveCommand = "Stopping";
        }
    }

    private void handleShipUndockReq(int userId, MShipUndockReq msg){
        Ship s = grids[msg.stationId].station.dockedShips.get(msg.shipId);
        if(s == null) {
            System.out.println("Couldn't find ship in GameHost.handleShipUndockRequest()");
            return;
        }

        //check permissions
        if(s.owner != userId || s.grid == -1 || !s.docked){
            MDenyRequest deny = new MDenyRequest(msg);

            if(s.grid == -1){
                deny.reason = "Cannot undock a ship in warp";
            }
            else if(!s.docked){
                deny.reason = "Cannot undock an undocked ship";
            }
            else {
                deny.reason = "Cannot command ship for unexpected reason";
            }

            server.sendMessage(userId, deny);
        }
        else {
            //move it to the grid
            grids[msg.stationId].station.dockedShips.remove(s.id);
            grids[msg.stationId].ships.put(s.id, s);

            //set physics TODO set to not intersect
            s.rot = (float) Math.random()*6.28f;
            s.pos.set(0.8f*(float)Math.cos(s.rot), 0.8f*(float)Math.sin(s.rot));
            s.vel.set(s.getMaxSpeed()/3 * (float)Math.cos(s.rot), s.getMaxSpeed()/3 * (float)Math.sin(s.rot));
            s.docked = false;

            //set ai
            s.movement = Ship.Movement.STOPPING;
            s.moveCommand = "Stopping";

            //reset other values
            for(Weapon w : s.weapons){
                w.putOnFullCooldown();
            }
            s.targetTimeToLock = -1;
            s.targetingState = null;
            s.warpTimeToLand = 0;

            server.broadcastMessage(new MShipDockingChange(s.id, msg.stationId, false,
                    MShipUpd.createFromShip(s)));
        }
    }

    private void handleShipDockReq(int userId, MShipDockReq msg){
        Ship s = grids[msg.grid].ships.get(msg.shipId);
        if(s == null) {
            System.out.println("Couldn't find ship in GameHost.handleShipDockReq()");
            return;
        }

        //check permissions and others TODO add in combat checks
        if(s.owner != userId || grids[msg.grid].station.owner != s.owner){
            MDenyRequest deny = new MDenyRequest(msg);

            if(s.owner != userId){
                deny.reason = "Cannot command ship for unexpected reason";
            }
            else if(s.grid == -1){
                deny.reason = "Cannot dock a ship in warp";
            }
            else if(grids[msg.grid].station.owner != s.owner){
                deny.reason = "You do not own that station";
            }

            server.sendMessage(userId, deny);
        }
        else {
            s.movement = Ship.Movement.MOVE_FOR_DOCK;
            s.moveCommand = "Taxiing to dock";
            s.moveTargetPos = grids[msg.grid].station.pos;
        }

    }

    private void handlePackedStationMoveReq(int userId, MPackedStationMoveReq msg){
        Entity removeFrom = msg.removeFrom.retrieveFromGrid(grids[msg.removeFrom.grid]);
        Entity addTo = msg.addTo.retrieveFromGrid(grids[msg.addTo.grid]);

        //check permissions
        if(removeFrom.owner != userId || addTo.owner != userId || removeFrom.grid != addTo.grid ||
                (!removeFrom.isDocked() && !addTo.isDocked())){
            MDenyRequest deny = new MDenyRequest(msg);

            if(removeFrom.owner != userId || addTo.owner != userId){
                deny.reason = "Cannot complete move due to ownership";
            }
            else if(removeFrom.grid != addTo.grid){
                deny.reason = "Cannot complete move due to dif grids";
            }
            else if(!removeFrom.isDocked() && !addTo.isDocked()){
                deny.reason = "Neither participant is docked to complete move";
            }

            server.sendMessage(userId, deny);
        }
        else {
            Station st;
            Barge sh;

            //move station to barge
            if(removeFrom instanceof Station && addTo instanceof Barge){
                st = (Station) removeFrom;
                sh = (Barge) addTo;

                //final checks
                Station.Type packedStationType = st.packedStations[msg.idxRemoveFrom];
                int destIdx = -1;
                for(int i=0; i<Barge.weaponSlots.length; i++) {
                    if(((StationTransport) sh.weapons[i]).cargo == null){
                        destIdx = i;
                        break;
                    }
                }

                //errors
                if(packedStationType == null){
                    System.out.println("Unexpected error while transferring");
                    new Exception().printStackTrace();

                    MDenyRequest deny = new MDenyRequest(msg);
                    deny.reason = "Unexpectedly unable to transfer";
                    server.sendMessage(userId, deny);
                    return;
                }
                else if(destIdx == -1){
                    MDenyRequest deny = new MDenyRequest(msg);
                    deny.reason = "Can't move packed station due to space";
                    server.sendMessage(userId, deny);
                    return;
                }

                //make the swap
                st.packedStations[msg.idxRemoveFrom] = null;
                ((StationTransport) sh.weapons[destIdx]).cargo = packedStationType;

                //tell players
                server.broadcastMessage(new MPackedStationMove(
                        EntPtr.createFromEntity(st), msg.idxRemoveFrom,
                        EntPtr.createFromEntity(sh), destIdx,
                        packedStationType));
            }
            //move barge to station
            else if(removeFrom instanceof Barge && addTo instanceof Station){
                st = (Station) addTo;
                sh = (Barge) removeFrom;

                //final checks
                Station.Type packedStationType = ((StationTransport) sh.weapons[msg.idxRemoveFrom]).cargo;
                int destIdx = -1;
                for(int i=0; i<Station.PACKED_STATION_SLOTS; i++) {
                    if(st.packedStations[i] == null){
                        destIdx = i;
                        break;
                    }
                }

                //errors
                if(packedStationType == null){
                    System.out.println("Unexpected error while transferring");
                    new Exception().printStackTrace();

                    MDenyRequest deny = new MDenyRequest(msg);
                    deny.reason = "Unexpectedly unable to transfer";
                    server.sendMessage(userId, deny);
                    return;
                }
                else if(destIdx == -1){
                    MDenyRequest deny = new MDenyRequest(msg);
                    deny.reason = "Can't move packed station due to space";
                    server.sendMessage(userId, deny);
                    return;
                }

                //make the swap
                ((StationTransport) sh.weapons[msg.idxRemoveFrom]).cargo = null;
                st.packedStations[destIdx] = packedStationType;

                //tell players
                server.broadcastMessage(new MPackedStationMove(
                        EntPtr.createFromEntity(sh), msg.idxRemoveFrom,
                        EntPtr.createFromEntity(st), destIdx,
                        packedStationType));
            }
            else {
                System.out.println("Unexpected requested inventory move");
                new Exception().printStackTrace();
            }
        }
    }


    /* State Utility */

    private Entity findEntityInState(Entity.Type type, int entityId, int gridId){
        if(type == Entity.Type.Station && gridId >= 0 && gridId < grids.length){
            return grids[gridId].station;
        }
        else if(type == Entity.Type.Ship && gridId == -1) {
            return shipsInWarp.get(entityId);
        }
        else if(type == Entity.Type.Ship && gridId >= 0 && gridId < grids.length){
            return grids[gridId].ships.get(entityId);
        }
        else {
            return null;
        }
    }

    private void dockShipAtStation(Ship ship, Station station, Grid grid){
        //dock the ship
        grid.ships.remove(ship.id);
        station.dockedShips.put(ship.id, ship);

        //set physics
        ship.rot = 0;
        ship.pos.set(station.pos);
        ship.vel.set(0, 0);
        ship.docked = true;

        //reset other values
        for(Weapon w : ship.weapons){
            w.active = false;
        }
        ship.targetTimeToLock = -1;
        ship.targetingState = null;
        ship.warpTimeToLand = 0;

        //send to clients
        server.broadcastMessage(new MShipDockingChange(ship.id, station.grid, true,
                MShipUpd.createFromShip(ship)));
    }

}
