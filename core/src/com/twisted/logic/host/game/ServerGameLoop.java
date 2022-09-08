package com.twisted.logic.host.game;

import com.badlogic.gdx.math.Vector2;
import com.twisted.Main;
import com.twisted.logic.descriptors.CurrentJob;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.descriptors.events.EvGameEnd;
import com.twisted.logic.descriptors.events.EvStationStageChange;
import com.twisted.logic.entities.*;
import com.twisted.logic.entities.attach.StationTransport;
import com.twisted.logic.entities.attach.Weapon;
import com.twisted.logic.mobs.Mobile;
import com.twisted.net.msg.gameReq.*;
import com.twisted.net.msg.gameUpdate.*;
import com.twisted.net.msg.remaining.MDenyRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

class ServerGameLoop {

    //exterior references
    private final GameHost host;
    private final ServerGameState state;


    ServerGameLoop(GameHost host, ServerGameState state){
        this.host = host;
        this.state = state;
    }


    /* Loop Methods */

    /**
     * The function where the game logic is performed.
     */
    void loop(HashMap<MGameReq, Integer> requests){
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
            else if(r instanceof MGemMoveReq) handleGemMoveReq(id, (MGemMoveReq) r);
        }

        //timer
        state.timeElapsed += GameHost.FRAC;

        //updating
        updateStationTimers();
        calculateTrajectories();
        updatePhysics();
        updateCombat();

        //telling clients
        sendEntMobData();

        //end condition
        loopEndCondition();
    }


    /* Loop Helper Methods */

    /**
     * Updates the station state due to time-based events.
     */
    private void updateStationTimers(){
        for(Grid grid : state.grids){
            Station st = grid.station;

            //job updates
            if(st.currentJobs.size() > 0) {
                //otherwise, grab the first job
                CurrentJob job = st.currentJobs.get(0);
                if(!job.blocking) job.timeLeft -= GameHost.FRAC;

                if(job.timeLeft <= 0){
                    boolean jobBlocking = false;

                    //create the ship or the packaged station
                    Ship sh = null;
                    switch(job.jobType){
                        case Frigate:
                            sh = new Frigate(state.useNextShipId(), grid.id, job.owner,true);
                            break;
                        case Barge:
                            sh = new Barge(state.useNextShipId(), grid.id, job.owner, true);
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

                                    //tracking
                                    state.players.get(st.owner).tracking.incrEntsBuilt(job.jobType.getPackedStationType());

                                    //tell players
                                    host.broadcastMessage(new MPackedStationMove(null,
                                            -1, EntPtr.createFromEntity(st), idx,
                                            job.jobType.getPackedStationType()));
                                    break;
                                }
                                else if(idx == st.packedStations.length-1){
                                    jobBlocking = true;
                                    break;
                                }
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

                        //tracking
                        state.players.get(st.owner).tracking.incrEntsBuilt(sh.subtype());

                        //tell users
                        host.broadcastMessage(MAddShip.createFromShipBody(sh));
                    }

                    //end the job and tell the user
                    if(!jobBlocking){
                        st.currentJobs.remove(0);
                        host.sendMessage(job.owner, new MChangeJob(MChangeJob.Action.FINISHED,
                                job.jobId, job.grid, null));
                    }
                    //tell the user the job is blocking
                    else if(!job.blocking){
                        job.blocking = true;
                        host.sendMessage(job.owner, new MChangeJob(MChangeJob.Action.BLOCKING,
                                job.jobId, job.grid, null));
                    }
                }
            }

            //stage timer
            st.stageTimer -= GameHost.FRAC;
            if(st.stageTimer <= 0){
                st.stageTimer = 0;

                //stage updates
                switch(st.stage){
                    case VULNERABLE:
                        st.stage = Station.Stage.SHIELDED;
                        st.shieldHealth = (float) Math.ceil(0.25f * st.model.getMaxShield());
                        break;
                    case ARMORED:
                        st.stage = Station.Stage.VULNERABLE;
                        st.stageTimer = st.model.getVulnerableDuration();
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

        for(Grid g : state.grids){
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
                        float speedCanStopFrom = (float) Math.sqrt( 2*s.model.getMaxAccel()*distanceToTarget );
                        float effectiveMaxSpeed = Math.min(0.8f*speedCanStopFrom, s.model.getMaxSpeed());

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
                                .nor().scl(0.9f*s.model.getMaxSpeed());

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
                    if(length > s.model.getMaxAccel() * GameHost.FRAC){
                        accel.x *= s.model.getMaxAccel() * GameHost.FRAC /length;
                        accel.y *= s.model.getMaxAccel() * GameHost.FRAC /length;
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
        ArrayList<Ship> shipsToDock = new ArrayList<>();
        ArrayList<Integer> mobilesToRemove = new ArrayList<>(); //mobile id

        //time step for ships not in warp
        for(Grid g : state.grids){
            //loop through ships on the grid
            for(Ship s : g.ships.values()){
                //move the ship
                s.pos.x += s.vel.x * GameHost.FRAC;
                s.pos.y += s.vel.y * GameHost.FRAC;

                //set the rotation
                if(s.vel.len() != 0){
                    s.rot = (float) Math.atan2(s.vel.y, s.vel.x);
                }

                //check if it should enter warp
                if(s.movement == Ship.Movement.ALIGN_FOR_WARP
                        && s.alignedForWarp(g, state.grids[s.warpTargetGridId])){
                    //remove the ship at the end of the loop
                    shipsToRemove.add(s);

                    //put in warp space
                    state.shipsInWarp.put(s.id, s);
                    s.warpTimeToLand = 3; //TODO set this based on dist/speed/etc

                    //update the movement description
                    s.movement = Ship.Movement.WARPING;
                    s.moveCommand = "Warping to " + state.grids[s.warpTargetGridId].nickname;

                    //update physics
                    s.pos.set(0, 0);
                    s.grid = -1;

                    //update targeting
                    s.targetingState = null;
                    s.targetEntity = null;

                    //tell the users
                    host.broadcastMessage(new MShipEnterWarp(s.id, g.id, s.warpTargetGridId));
                }

                //check if the ship should dock
                if(s.movement == Ship.Movement.MOVE_FOR_DOCK){
                    Station st = g.station;
                    //dock
                    if(s.pos.dst(st.pos) <= st.model.getDockingRadius() && st.owner == s.owner){
                        shipsToDock.add(s);
                    }
                    //owner changed
                    else if(st.owner != s.owner) {
                        s.movement = Ship.Movement.STOPPING;
                        s.moveCommand = "Stopping";
                    }
                }
            }

            //dock ships
            for(Ship s : shipsToDock){
                state.dockShipAtStation(s, g.station, g);
            }
            shipsToDock.clear();
            //remove ships that left the grid
            for(Ship s : shipsToRemove){
                g.ships.remove(s.id);
            }
            shipsToRemove.clear();
        }

        //time step for ships in warp
        for(Ship s : state.shipsInWarp.values()){
            //move the ship along
            s.warpTimeToLand -= GameHost.FRAC;

            if(s.warpTimeToLand <= 0){
                s.warpTimeToLand = 0;

                shipsToRemove.add(s);

                //tell the users
                host.broadcastMessage(new MShipExitWarp(s.id, s.warpTargetGridId));

                //add it to the correct grid
                state.grids[s.warpTargetGridId].ships.put(s.id, s);
                s.grid = s.warpTargetGridId;

                //exiting warp
                s.warpTargetGridId = -1;
                s.moveCommand = "Stopping";
                s.movement = Ship.Movement.STOPPING;

                //placing with correct physics (slightly varied)
                Vector2 warpVel = s.vel.cpy().nor().scl(1 + s.model.getMaxSpeed()).rotateRad((float) Math.random()*0.15f);
                s.pos.set(-warpVel.x, -warpVel.y); //TODO place ship in better place

                warpVel.nor().scl(s.model.getMaxSpeed()*0.75f);
                s.vel.set(warpVel.x, warpVel.y);
            }
        }
        //remove ships that exited warp
        for(Ship s : shipsToRemove){
            state.shipsInWarp.remove(s.id);
        }

        //time step for mobiles
        for(Grid g : state.grids) {
            //loop through mobiles on the grid
            for (Mobile m : g.mobiles.values()) {
                //take the time step and check for fizzle
                if(m.update(GameHost.FRAC, g)) {
                    mobilesToRemove.add(m.id);

                    //tell clients
                    host.broadcastMessage(MMobileUps.createFromMobile(m, g.id, true));
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

        for(Grid g : state.grids){
            //targeting and weapons
            for(Ship s : g.ships.values()){
                if(s.targetingState == Ship.Targeting.Locking){
                    s.targetTimeToLock -= GameHost.FRAC;

                    if(s.targetTimeToLock <= 0){
                        s.targetingState = Ship.Targeting.Locked;
                    }
                }

                //get the target entity
                Entity target = (s.targetEntity==null) ? null : s.targetEntity.retrieveFromGrid(g);

                //stop targeting if not valid to target (doesn't exist or range checks)
                if(target == null || target.pos.dst(s.pos) > s.model.getTargetRange()){
                    s.targetingState = null;
                    s.targetEntity = null;
                }
                //update weapons
                for(Weapon w : s.weapons){
                    w.tick(state, g, s, target, s.targetingState, GameHost.FRAC);
                }
            }

            //ship health checks
            for(Ship s : g.ships.values()){
                if(s.health <= 0){
                    host.broadcastMessage(new MRemShip(s.id, g.id, Ship.Removal.EXPLOSION, false));
                    toBeRemoved.add(s);

                    //tracking
                    state.players.get(s.lastHit).tracking.incrEntsKilled(s.subtype());
                }
            }
            //remove ships
            for(Ship s : toBeRemoved){
                g.ships.remove(s.id);
            }

            //station health checks TODO station gradual healing while shielded
            Station st = g.station;
            if(st.stage == Station.Stage.SHIELDED && st.shieldHealth <= 0){
                st.shieldHealth = 0;
                st.stageTimer = st.model.getArmoredDuration();
                st.stage = Station.Stage.ARMORED;

                //add event
                state.addToEventHistory(new EvStationStageChange(st.getId(), Station.Stage.SHIELDED,
                        st.owner, Station.Stage.ARMORED, st.owner));
            }
            if(st.stage == Station.Stage.VULNERABLE && st.hullHealth <= 0){
                //add event
                state.addToEventHistory(new EvStationStageChange(st.getId(), Station.Stage.VULNERABLE,
                        st.owner, Station.Stage.RUBBLE, 0));

                //change to rubble
                g.station.hullHealth = 0;
                g.station.stage = Station.Stage.RUBBLE;
                g.station.owner = 0;

                //stop all ships from targeting it
                for(Ship s : g.ships.values()){
                    if(s.targetingState != null && s.targetEntity.matches(g.station)
                            && !(s.subtype() == Ship.Model.Barge)){
                        s.targetingState = null;
                        s.targetEntity = null;
                        s.targetTimeToLock = 0;
                    }
                }

                //tracking
                state.players.get(st.lastHit).tracking.incrEntsKilled(st.subtype());

                //other updates in station
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
        for(Grid g : state.grids){
            for(Ship s : g.ships.values()){
                host.broadcastMessage(MShipUpd.createFromShip(s));
            }
        }
        for(Ship s : state.shipsInWarp.values()){
            host.broadcastMessage(MShipUpd.createFromShip(s));
        }

        //station data
        for(Grid g : state.grids){
            host.broadcastMessage(MStationUpd.createFromStation(g.station));
            if(g.station.owner != 0 && !state.players.get(g.station.owner).ai){
                host.sendMessage(g.station.owner, MJobTimerUpd.createFromStation(g.station));
            }
        }

        //mobile data
        for(Grid g : state.grids){
            for(Mobile m : g.mobiles.values()){
                host.broadcastMessage(MMobileUps.createFromMobile(m, g.id, false));
            }
        }
    }

    /**
     * Checks for end condition and ends the game if it is met.
     */
    private void loopEndCondition(){

        //check end condition
        int winner = -1;
        for(Grid g : state.grids){
            for(Station.Model t : g.station.packedStations){
                if(t == Station.Model.Liquidator){
                    winner = g.station.owner;
                    break;
                }
            }
            if(winner != -1) break;
        }
        if(winner == -1) return;

        //add event
        state.addToEventHistory(new EvGameEnd(winner));

        //tell host
        host.endGame(winner);
    }


    /* Client Request Handling */

    private void handleJobReq(int userId, MJobReq msg){
        Station s = (Station) state.findEntityInState(Entity.Type.Station, msg.stationGrid, msg.stationGrid);
        Station.Job j = msg.job;
        if(s == null) {
            System.out.println("Couldn't find ship in GameHost.handleJobRequest()");
            return;
        }

        //reject if conditions not met
        if(s.owner != userId || !s.enoughForJob(j) || s.stage == null){

            MDenyRequest deny = new MDenyRequest(msg);
            if(!s.enoughForJob(j)){
                deny.reason = "Not enough resources for job: " + j.name();
            }
            else if(s.stage == null) {
                deny.reason = s.getFullName() + " cannot build right now";
            }
            else {
                deny.reason = "Job denied for unexpected reason";
            }

            host.sendMessage(userId, deny);
        }
        //otherwise accept
        else {
            //update on the serverside
            s.removeResourcesForJob(j);
            CurrentJob currentJob = new CurrentJob(state.useNextJobId(), userId, j, s.grid, j.duration);
            s.currentJobs.add(currentJob);

            //send information to the client
            host.sendMessage(userId, new MChangeJob(MChangeJob.Action.ADDING,
                    currentJob.jobId, currentJob.grid, currentJob));
        }
    }

    private void handleShipMoveReq(int userId, MShipMoveReq msg){
        //get the ship
        Ship s = (Ship) state.findEntityInState(Entity.Type.Ship, msg.shipId, msg.grid);
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

            host.sendMessage(userId, deny);
        }
        else {
            //set the ship's movement
            s.movement = Ship.Movement.MOVE_TO_POS;
            s.moveTargetPos = msg.location;

            //set the description of the movement
            s.moveCommand = "Moving to (" + Main.df2.format(s.moveTargetPos.x) + ", "
                    + Main.df2.format(s.moveTargetPos.y) + ")";
        }

    }

    private void handleShipOrbitReq(int userId, MShipOrbitReq msg){
        //get the ship
        Ship s = (Ship) state.findEntityInState(Entity.Type.Ship, msg.shipId, msg.grid);
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
            else if(s.id == msg.targetId && msg.targetType == Entity.Type.Ship){
                deny.reason = "Ship cannot orbit itself";
            }
            else if(s.docked){
                deny.reason = "Cannot command a docked ship";
            }
            else {
                deny.reason = "Cannot command ship for unexpected reason";
            }

            host.sendMessage(userId, deny);
        }
        else {
            Entity ent;
            if((ent = state.grids[msg.grid].retrieveEntity(msg.targetType, msg.targetId)) != null){
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
                    s.moveCommand = "Orbiting " + ((Ship) ent).subtype();
                }
            }
        }
    }

    private void handleShipAlignReq(int userId, MShipAlignReq msg){
        //get the ship
        Ship s = (Ship) state.findEntityInState(Entity.Type.Ship, msg.shipId, msg.grid);
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

            host.sendMessage(userId, deny);
        }
        else {
            //set the ship's movement
            s.movement = Ship.Movement.ALIGN_TO_ANG;
            s.trajectoryVel = s.trajectoryVel.set(
                    (float) Math.cos(msg.angle) * s.model.getMaxSpeed() * 0.8f,
                    (float) Math.sin(msg.angle) * s.model.getMaxSpeed() * 0.8f);

            //set the description of the movement
            int degrees = -((int) (msg.angle*180/Math.PI - 90));
            if(degrees < 0) degrees += 360;
            s.moveCommand = "Alinging to " + degrees + " N";
        }
    }

    private void handleShipWarpReq(int userId, MShipWarpReq msg){
        //get the ship
        Ship s = (Ship) state.findEntityInState(Entity.Type.Ship, msg.shipId, msg.grid);
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

            host.sendMessage(userId, deny);
        }
        else{
            //set the target grid id
            s.movement = Ship.Movement.ALIGN_FOR_WARP;
            s.warpTargetGridId = msg.targetGridId;
            s.moveCommand = "Aligning to grid " + state.grids[msg.targetGridId].nickname;

            //get the angle to the target grid
            float angle = (float) Math.atan2(state.grids[msg.targetGridId].pos.y-state.grids[msg.grid].pos.y,
                    state.grids[msg.targetGridId].pos.x-state.grids[msg.grid].pos.x);
            s.trajectoryVel = s.trajectoryVel.set(
                    (float) Math.cos(angle) * s.model.getMaxSpeed() * 0.8f,
                    (float) Math.sin(angle) * s.model.getMaxSpeed() * 0.8f);
        }
    }

    private void handleShipTargetReq(int userId, MTargetReq msg){
        //get the ship
        Ship s = (Ship) state.findEntityInState(Entity.Type.Ship, msg.shipId, msg.grid);
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

            host.sendMessage(userId, deny);
        }
        else {
            //get the target entity
            Entity target = state.findEntityInState(msg.targetType, msg.targetId, msg.grid);

            //start locking on to the target
            if(target != null && target.pos.dst(s.pos) <= s.model.getTargetRange()){
                s.targetingState = Ship.Targeting.Locking;
                s.targetEntity = EntPtr.createFromEntity(target);

                //TODO calculate locking time based on entity types
                s.targetTimeToLock = 1;
            }
            //not in range
            else if(target != null){
                MDenyRequest deny = new MDenyRequest(msg);
                deny.reason = "Target not in targeting range";
                host.sendMessage(userId, deny);
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
        Ship s = (Ship) state.findEntityInState(Entity.Type.Ship, msg.shipId, msg.grid);
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

            host.sendMessage(userId, deny);
        }
        else {
            switch(s.weapons[msg.weaponId].getType()){
                case Blaster: {
                    s.weapons[msg.weaponId].active = msg.active;
                    break;
                }
                case StationTransport: {
                    //check need to deny
                    MDenyRequest deny = new MDenyRequest(msg);
                    if(s.grid == -1){
                        deny.reason = "Cannot deploy station from warp";
                    }
                    else if (s.targetingState != Ship.Targeting.Locked || s.targetEntity.type != Entity.Type.Station) {
                        deny.reason = "Must target station rubble to deploy";
                    }
                    else {
                        //retrieve station
                        Station st = (Station) s.targetEntity.retrieveFromGrid(state.grids[s.grid]);
                        if(st.stage != Station.Stage.RUBBLE){
                            deny.reason = "Station must be rubble to deploy";
                        }
                        else if(st.subtype() != ((StationTransport) s.weapons[msg.weaponId]).cargo){
                            deny.reason = "Station type must match to be able to deploy";
                        }
                        else if(st.pos.dst(s.pos) > s.weapons[msg.weaponId].getMaxRange()){
                            deny.reason = "Barge must be within range to deploy";
                        }
                    }
                    //send deny
                    if(!deny.reason.equals("")) {
                        host.sendMessage(userId, deny);
                        break;
                    }

                    //activate weapon if didn't deny
                    s.weapons[msg.weaponId].active = msg.active;

                    break;
                }
            }
        }
    }

    private void handleShipStopReq(int userId, MShipStopReq msg){
        //get the ship
        Ship s = (Ship) state.findEntityInState(Entity.Type.Ship, msg.shipId, msg.grid);
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

            host.sendMessage(userId, deny);
        }
        else {
            //set the ship's movement
            s.movement = Ship.Movement.STOPPING;

            //set the description of the movement
            s.moveCommand = "Stopping";
        }
    }

    private void handleShipUndockReq(int userId, MShipUndockReq msg){
        Ship s = state.grids[msg.stationId].station.dockedShips.get(msg.shipId);
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

            host.sendMessage(userId, deny);
        }
        else {
            //move it to the grid
            state.grids[msg.stationId].station.dockedShips.remove(s.id);
            state.grids[msg.stationId].ships.put(s.id, s);

            //set physics TODO set to not intersect
            s.rot = (float) Math.random()*6.28f;
            s.pos.set(0.8f*(float)Math.cos(s.rot), 0.8f*(float)Math.sin(s.rot));
            s.vel.set(s.model.getMaxSpeed()/3 * (float)Math.cos(s.rot), s.model.getMaxSpeed()/3 * (float)Math.sin(s.rot));
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

            host.broadcastMessage(new MShipDockingChange(s.id, msg.stationId, false,
                    MShipUpd.createFromShip(s)));
        }
    }

    private void handleShipDockReq(int userId, MShipDockReq msg){
        Ship s = state.grids[msg.grid].ships.get(msg.shipId);
        if(s == null) {
            System.out.println("Couldn't find ship in GameHost.handleShipDockReq()");
            return;
        }

        //check permissions and others TODO add in combat checks
        if(s.owner != userId || state.grids[msg.grid].station.owner != s.owner){
            MDenyRequest deny = new MDenyRequest(msg);

            if(s.owner != userId){
                deny.reason = "Cannot command ship for unexpected reason";
            }
            else if(s.grid == -1){
                deny.reason = "Cannot dock a ship in warp";
            }
            else if(state.grids[msg.grid].station.owner != s.owner){
                deny.reason = "You do not own that station";
            }

            host.sendMessage(userId, deny);
        }
        else {
            s.movement = Ship.Movement.MOVE_FOR_DOCK;
            s.moveCommand = "Taxiing to dock";
            s.moveTargetPos = state.grids[msg.grid].station.pos;
        }

    }

    private void handlePackedStationMoveReq(int userId, MPackedStationMoveReq msg){
        Entity removeFrom = msg.removeFrom.retrieveFromGrid(state.grids[msg.removeFrom.grid]);
        Entity addTo = msg.addTo.retrieveFromGrid(state.grids[msg.addTo.grid]);

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

            host.sendMessage(userId, deny);
        }
        else {
            Station st;
            Barge sh;

            //move station to barge
            if(removeFrom instanceof Station && addTo instanceof Barge){
                st = (Station) removeFrom;
                sh = (Barge) addTo;

                //final checks
                Station.Model packedStationType = st.packedStations[msg.idxRemoveFrom];
                int destIdx = -1;
                for(int i=0; i< Ship.Model.Barge.getWeaponSlots().length; i++) {
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
                    host.sendMessage(userId, deny);
                    return;
                }
                else if(destIdx == -1){
                    MDenyRequest deny = new MDenyRequest(msg);
                    deny.reason = "Can't move packed station due to space";
                    host.sendMessage(userId, deny);
                    return;
                }

                //make the swap
                st.packedStations[msg.idxRemoveFrom] = null;
                ((StationTransport) sh.weapons[destIdx]).cargo = packedStationType;

                //tell players
                host.broadcastMessage(new MPackedStationMove(
                        EntPtr.createFromEntity(st), msg.idxRemoveFrom,
                        EntPtr.createFromEntity(sh), destIdx,
                        packedStationType));
            }
            //move barge to station
            else if(removeFrom instanceof Barge && addTo instanceof Station){
                st = (Station) addTo;
                sh = (Barge) removeFrom;

                //final checks
                Station.Model packedStationType = ((StationTransport) sh.weapons[msg.idxRemoveFrom]).cargo;
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
                    host.sendMessage(userId, deny);
                    return;
                }
                else if(destIdx == -1){
                    MDenyRequest deny = new MDenyRequest(msg);
                    deny.reason = "Can't move packed station due to space";
                    host.sendMessage(userId, deny);
                    return;
                }

                //make the swap
                ((StationTransport) sh.weapons[msg.idxRemoveFrom]).cargo = null;
                st.packedStations[destIdx] = packedStationType;

                //tell players
                host.broadcastMessage(new MPackedStationMove(
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

    private void handleGemMoveReq(int userId, MGemMoveReq msg){
        Entity removeFrom = msg.removeFrom.retrieveFromGrid(state.grids[msg.removeFrom.grid]);
        Entity addTo = msg.addTo.retrieveFromGrid(state.grids[msg.addTo.grid]);

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

            host.sendMessage(userId, deny);
        }
        else {
            Station st;
            Barge sh;

            //move from station to barge
            if(removeFrom instanceof Station && addTo instanceof Barge){
                st = (Station) removeFrom;
                sh = (Barge) addTo;

                //modify amount if it's too large
                int amt = msg.amount;
                if(amt > st.resources[msg.gemType.index]){
                    amt = st.resources[msg.gemType.index];
                }
                if(amt > sh.maxGemsCanFit(msg.gemType)){
                    amt = (int) sh.maxGemsCanFit(msg.gemType);
                }

                //deny if necessary
                if(amt <= 0){
                    MDenyRequest deny = new MDenyRequest(msg);
                    deny.reason = "Can't transfer due to space or resource limits";
                    host.sendMessage(userId, deny);
                }
                //otherwise transfer
                else {
                    //make the changes
                    st.resources[msg.gemType.index] -= amt;
                    sh.resources[msg.gemType.index] += amt;

                    //tell clients
                    MResourceChange stMsg = new MResourceChange(EntPtr.createFromEntity(st));
                    stMsg.resourceChanges[msg.gemType.index] = -amt;
                    host.sendMessage(st.owner, stMsg);
                    MResourceChange shMsg = new MResourceChange(EntPtr.createFromEntity(sh));
                    shMsg.resourceChanges[msg.gemType.index] = amt;
                    host.sendMessage(sh.owner, shMsg);
                }
            }
            //move from barge to station
            else if(removeFrom instanceof Barge && addTo instanceof Station){
                st = (Station) addTo;
                sh = (Barge) removeFrom;

                //modify amount if it's too large
                int amt = msg.amount;
                if(msg.amount > sh.resources[msg.gemType.index]){
                    amt = sh.resources[msg.gemType.index];
                }

                //deny if necessary
                if(amt <= 0){
                    MDenyRequest deny = new MDenyRequest(msg);
                    deny.reason = "Can't transfer due to resource limits";
                    host.sendMessage(userId, deny);
                }
                //otherwise transfer
                else {
                    //make the changes
                    sh.resources[msg.gemType.index] -= amt;
                    st.resources[msg.gemType.index] += amt;

                    //tell clients
                    MResourceChange shMsg = new MResourceChange(EntPtr.createFromEntity(sh));
                    shMsg.resourceChanges[msg.gemType.index] = -amt;
                    host.sendMessage(sh.owner, shMsg);
                    MResourceChange stMsg = new MResourceChange(EntPtr.createFromEntity(st));
                    stMsg.resourceChanges[msg.gemType.index] = amt;
                    host.sendMessage(st.owner, stMsg);
                }
            }
            else {
                System.out.println("Unexpected requested inventory move");
                new Exception().printStackTrace();
            }
        }

    }

}
