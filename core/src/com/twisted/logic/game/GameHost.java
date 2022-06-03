package com.twisted.logic.game;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.Player;
import com.twisted.logic.descriptors.CurrentJob;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.*;
import com.twisted.net.msg.*;
import com.twisted.net.msg.gameRequest.MShipAlignRequest;
import com.twisted.net.msg.gameRequest.MShipMoveRequest;
import com.twisted.net.msg.gameUpdate.MAddShip;
import com.twisted.net.msg.gameUpdate.MShipUpd;
import com.twisted.net.msg.remaining.MDenyRequest;
import com.twisted.net.msg.gameRequest.MGameRequest;
import com.twisted.net.msg.gameRequest.MJobRequest;
import com.twisted.net.msg.gameUpdate.MChangeJob;
import com.twisted.net.msg.gameUpdate.MGameOverview;
import com.twisted.net.msg.remaining.MGameStart;
import com.twisted.net.msg.remaining.MSceneChange;
import com.twisted.net.server.Server;
import com.twisted.net.server.ServerContact;
import com.twisted.local.game.state.PlayColor;

import java.text.DecimalFormat;
import java.util.*;


public class GameHost implements ServerContact {

    /* Constants */

    public static final int TICK_DELAY = 50; //millis between each tick
    public static final float TICKS = TICK_DELAY / 1000f; //ticks per second

    private static final DecimalFormat df2 = new DecimalFormat("0.00");


    /* Exterior Reference Variables */

    private Server server;


    /* Storage Variables */

    //player details
    private final HashMap<Integer, Player> players;
    private final int hostId;

    //networking and game loop
    private final HashMap<MGameRequest, Integer> requests; //requests being read during game logic
    private final Map<MGameRequest, Integer> hotRequests; //requests stored between ticks (synchronized)
    private boolean looping;
    private float millisSinceMajor;

    //map details
    private int mapWidth;
    private int mapHeight;

    //state variables
    private Grid[] grids;

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
                new Grid(0, new Vector2(50, 320)),
                new Grid(1, new Vector2(200, 100)),
                new Grid(2, new Vector2(80, 920)),
                new Grid(3, new Vector2(950, 680)),
                new Grid(4, new Vector2(800, 900)),
                new Grid(5, new Vector2(920, 80)),
                new Grid(6, new Vector2(400, 500)),
                new Grid(7, new Vector2(500, 400)),
        };
        grids[0].station = new Extractor(0, "Extractor A", players[0].getId(), Station.Stage.ARMORED, false);
        grids[1].station = new Extractor(1, "Extractor B", players[0].getId(), Station.Stage.ARMORED, false);
        grids[2].station = new Harvester(2, "Harvester A", players[0].getId(), Station.Stage.ARMORED, false);
        grids[3].station = new Extractor(3, "Extractor C", players[1].getId(), Station.Stage.ARMORED, false);
        grids[4].station = new Extractor(4, "Extractor D", players[1].getId(), Station.Stage.ARMORED, false);
        grids[5].station = new Harvester(5, "Harvester B", players[1].getId(), Station.Stage.ARMORED, false);
        grids[6].station = new Liquidator(6, "Liquidator A", 0, Station.Stage.NONE, false);
        grids[7].station = new Liquidator(7, "Liquidator B", 0, Station.Stage.NONE, false);

        //add initial resources
        grids[0].station.resources[0] += 20;
        grids[0].station.resources[1] += 6;
        grids[3].station.resources[0] += 20;
        grids[3].station.resources[1] += 6;
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
        HashMap<Integer, PlayColor> idToColor = new HashMap<>();
        int i = 0;
        for(Player p : players.values()){
            if(i==0) idToColor.put(p.getId(), PlayColor.BLUE);
            else if(i==1) idToColor.put(p.getId(), PlayColor.ORANGE);
            else break;
            i++;
        }

        //create the message
        MGameStart msg = new MGameStart(idToName, idToColor, grids.length);

        //fill in the message
        msg.tickDelay = TICK_DELAY;
        msg.mapWidth = mapWidth;
        msg.mapHeight = mapHeight;

        //fill in the grid parts of the message
        for(int j=0; j<grids.length; j++){
            Grid g = grids[j];

            msg.gridPositions[j] = g.position;
            msg.stationTypes[j] = g.station.getType();
            msg.stationNames[j] = g.station.name;
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

        //dev ship
        Ship s = new Frigate(useNextShipId(), 1, new Vector2(1, 0), new Vector2(0,0), 0, false);
        grids[0].ships.put(s.id, s);

        //tell users
        server.broadcastMessage(MAddShip.createFromShipBody(0, s));

    }


    /* ServerContact Methods */

    /**
     * Called when the server receives a message from a client.
     */
    @Override
    public void serverReceived(int clientId, Message message) {
        if(message instanceof MGameRequest){
            hotRequests.put((MGameRequest) message, clientId);
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
        for(MGameRequest request : requests.keySet()){
            int userId = requests.get(request);

            if(request instanceof MJobRequest) handleJobRequest(userId, (MJobRequest) request);
            else if(request instanceof MShipMoveRequest) handleShipMoveRequest(userId, (MShipMoveRequest) request);
            else if(request instanceof MShipAlignRequest) handleShipAlignRequest(userId, (MShipAlignRequest) request);
        }

        //updating
        updateStationTimers();
        calculateTrajectories();
        updatePhysics();

        //telling clients
        sendEntityData();

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

        //fill station timers
        for(Grid g : grids){
            for(CurrentJob j : g.station.currentJobs){
                overviews.get(j.owner).jobToTimeLeft.put(j.jobId, j.timeLeft);
            }
        }
        //fill station resources
        for(Grid g : grids){
            Station s = g.station;
            if(s.owner > 0){ //check there is an owner
                overviews.get(s.owner).stationToResources.put(s.grid, s.resources.clone());
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
            //skip if no jobs
            if(grid.station.currentJobs.size() > 0) {
                //otherwise, grab the first job
                CurrentJob job = grid.station.currentJobs.get(0);
                job.timeLeft -= TICKS;

                if(job.timeLeft <= 0){
                    //end the job and tell the user
                    grid.station.currentJobs.remove(0);
                    server.sendMessage(job.owner, new MChangeJob(MChangeJob.Action.FINISHED,
                            job.jobId, job.grid, null));

                    //create the ship
                    Ship sh = null;
                    switch(job.jobType){
                        case Frigate:
                            //TODO place ship to not intersect with something else
                            sh = new Frigate(useNextShipId(), job.owner,
                                    new Vector2(1, 0), new Vector2(0, 0), 0,
                                    false);

                            break;
                        //TODO add the rest of the ship cases
                        default:
                            System.out.println("Unexpected job type in GameHost.updateStationTimers()");
                            break;
                    }

                    if(sh != null){
                        //create the body and add the ship to the grid
                        grid.ships.put(sh.id, sh);

                        //tell users
                        server.broadcastMessage(MAddShip.createFromShipBody(grid.id, sh));
                    }
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
                if(s.movement == Ship.Movement.STATIONARY){
                    s.trajectoryVel.set(0, 0);
                }
                else if(s.movement == Ship.Movement.MOVE_TO_POS){
                    //already close enough to target position
                    if((distanceToTarget=s.position.dst(s.targetPos)) <= 0.01f){
                        s.trajectoryVel.set(0, 0);
                    }
                    else {
                        //set the targetVel to the correct direction and normalize
                        s.trajectoryVel = new Vector2(s.targetPos.x - s.position.x,
                                s.targetPos.y - s.position.y).nor();

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

                //accelerate the ship
                if(s.trajectoryVel != null){
                    //find the desired, then clamp it

                    accel.set(s.trajectoryVel.x-s.velocity.x, s.trajectoryVel.y-s.velocity.y);

                    float length = accel.len();
                    if(length > s.getMaxAccel() * TICKS){
                        accel.x *= s.getMaxAccel() * TICKS /length;
                        accel.y *= s.getMaxAccel() * TICKS /length;
                    }

                    //update the velocity
                    s.velocity.x += accel.x;
                    s.velocity.y += accel.y;
                }

            }
        }

    }

    /**
     * Updates the world of each grid.
     */
    private void updatePhysics(){

        //take the time step
        for(Grid g : grids){
            for(Ship s : g.ships.values()){

                //move the ship
                s.position.x += s.velocity.x* TICKS;
                s.position.y += s.velocity.y* TICKS;

                //set the rotation
                if(s.velocity.len() != 0){
                    s.rotation = (float) -Math.atan2(s.velocity.x, s.velocity.y);
                }
            }
        }

    }

    /**
     * Send entity data to the users.
     */
    private void sendEntityData(){

        //ship data
        for(Grid g : grids){
            for(Ship s : g.ships.values()){
                server.broadcastMessage(MShipUpd.createFromShip(s, g.id));
            }
        }

    }


    /* Client Request Handling */

    /**
     * Handles MJobRequest
     */
    private void handleJobRequest(int userId, MJobRequest msg){

        Station s = grids[msg.stationGrid].station;
        Station.Job j = msg.job;

        //reject if conditions not met
        if(s.owner != userId || !s.enoughForJob(j) || s.stage == Station.Stage.NONE ||
                s.stage == Station.Stage.DEPLOYMENT){

            MDenyRequest deny = new MDenyRequest(msg);
            if(!s.enoughForJob(j)) deny.reason = "Not enough resources for job";
            else if(s.owner == userId) deny.reason = "Station cannot build right now";
            else deny.reason = "Unknown reason for denial";

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

    /**
     * Handles MShipMoveRequest
     */
    private void handleShipMoveRequest(int userId, MShipMoveRequest msg){
        //get the ship
        Ship ship = grids[msg.grid].ships.get(userId);
        if(ship == null){
            System.out.println("Couldn't find ship in GameHost.handleShipMoveRequest()");
            return;
        }

        //check permissions
        if(ship.owner != userId) return;

        //otherwise, set the ship's movement
        ship.movement = Ship.Movement.MOVE_TO_POS;
        ship.targetPos = msg.location;

        //set the description of the movement
        ship.moveCommand = "Moving to (" + df2.format(ship.targetPos.x) + ", "
                + df2.format(ship.targetPos.y) + ")";
    }

    /**
     * Handles MShipMoveRequest
     */
    private void handleShipAlignRequest(int userId, MShipAlignRequest msg){
        //get the ship
        Ship ship = grids[msg.grid].ships.get(userId);
        if(ship == null){
            System.out.println("Couldn't find ship in GameHost.handleShipMoveRequest()");
            return;
        }

        //check permissions
        if(ship.owner != userId) return;

        //otherwise, set the ship's movement
        ship.movement = Ship.Movement.ALIGN_TO_ANG;
        ship.trajectoryVel = ship.trajectoryVel.set(
                (float) Math.cos(msg.angle) * ship.getMaxSpeed() * 0.75f,
                (float) Math.sin(msg.angle) * ship.getMaxSpeed() * 0.75f);

        //set the description of the movement
        int degrees = -((int) (msg.angle*180/Math.PI - 90));
        if(degrees < 0) degrees += 360;
        ship.moveCommand = "Alinging to " + degrees + " N";

    }

}
