package com.twisted.logic.host.game;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.twisted.Main;
import com.twisted.logic.descriptors.*;
import com.twisted.logic.descriptors.events.EvGameEnd;
import com.twisted.logic.descriptors.events.EvStationStageChange;
import com.twisted.logic.entities.*;
import com.twisted.logic.entities.attach.StationTrans;
import com.twisted.logic.entities.attach.Weapon;
import com.twisted.logic.entities.ship.*;
import com.twisted.logic.entities.station.Station;
import com.twisted.logic.mobs.Mobile;
import com.twisted.net.msg.gameReq.*;
import com.twisted.net.msg.gameUpdate.*;
import com.twisted.net.msg.remaining.MDenyRequest;
import com.twisted.util.Quirk;

import java.util.*;

class ServerGameLoop {

    //exterior references
    private final GameHost host;
    private final ServerGameState state;


    /**
     * Constructor
     */
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
        updateDockedShips();
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
        for(Grid g : state.grids){
            Station st = g.station;

            //job updates
            if(st.currentJobs.size() > 0) {
                //grab the first job
                CurrentJob job = st.currentJobs.get(0);
                //start the job if needed
                if(!job.started){
                    st.removeResourcesForJob(job.jobType);
                    job.started = true;

                    //send the resource change message
                    MResourceChange msg = new MResourceChange(EntPtr.createFromEntity(st), st.resources);
                    host.sendMessage(st.owner, msg);
                }
                //count down if not blocking
                if(!job.blocking){
                    job.timeLeft -= GameHost.FRAC;
                }

                if(job.timeLeft <= 0) {
                    boolean jobBlocking = false;

                    //create the ship or the packaged station
                    if (job.jobType.getType() == Entity.Type.Ship) {
                        Ship sh = null;
                        switch ((Ship.Tier) job.jobType.getTier()) {
                            case Frigate:
                                sh = new Frigate((Ship.Model) job.jobType.getModel(), state.useNextShipId(), g.id, job.owner, true);
                                break;
                            case Cruiser:
                                sh = new Cruiser((Ship.Model) job.jobType.getModel(), state.useNextShipId(), g.id, job.owner, true);
                                break;
                            case Battleship:
                                sh = new Battleship((Ship.Model) job.jobType.getModel(), state.useNextShipId(), g.id, job.owner, true);
                                break;
                            case Barge:
                                sh = new Barge((Ship.Model) job.jobType.getModel(), state.useNextShipId(), g.id, job.owner, true);
                                break;
                            case Titan:
                                sh = new Titan((Ship.Model) job.jobType.getModel(), state.useNextShipId(), g.id, job.owner, true);
                                break;
                        }

                        if (sh != null) {
                            //add ship to world
                            if (!sh.docked) {
                                g.ships.put(sh.id, sh);
                            } else {
                                g.station.dockedShips.put(sh.id, sh);
                            }

                            //tracking
                            state.players.get(st.owner).tracking.incrEntsBuilt(sh.entityModel().getTier());

                            //tell users
                            host.broadcastMessage(MAddShip.createFromShipBody(sh));
                        }
                        else {
                            new Quirk(Quirk.Q.UnknownGameData).print();
                        }
                    }
                    else if (job.jobType.getType() == Entity.Type.Station) {
                        int idx;
                        for (idx = 0; idx < st.packedStations.length; idx++) {
                            if (st.packedStations[idx] == null) {
                                st.packedStations[idx] = (Station.Model) job.jobType.getModel();

                                //tracking
                                state.players.get(st.owner).tracking.incrEntsBuilt(job.jobType.getModel().getTier());

                                //tell players
                                host.broadcastMessage(new MPackedStationMove(null,
                                        -1, EntPtr.createFromEntity(st), idx,
                                        (Station.Model) job.jobType.getModel()));
                                break;
                            } else if (idx == st.packedStations.length - 1) {
                                jobBlocking = true;
                                break;
                            }
                        }
                    }
                    else {
                        new Quirk(Quirk.Q.UnknownClientDataSpecification).print();
                    }

                    //end the job and tell the user
                    if (!jobBlocking) {
                        st.currentJobs.remove(0);
                        host.sendMessage(job.owner, new MChangeJob(MChangeJob.Action.FINISHED,
                                job.jobId, job.grid, null));
                    }
                    //tell the user the job is blocking
                    else if (!job.blocking) {
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
                        st.shieldHealth = (float) Math.ceil(0.25f * st.model.maxShield);
                        break;
                    case ARMORED:
                        st.stage = Station.Stage.VULNERABLE;
                        st.stageTimer = st.model.vulnerableDuration;
                        break;
                }
            }

            //resource updates
            if(st.owner != 0 && (st.stage == Station.Stage.SHIELDED || st.stage == Station.Stage.VULNERABLE)){
                boolean resourceChange = false;
                for(int i=0; i<Gem.NUM_OF_GEMS; i++){
                    st.chargeResource[i] += GameHost.FRAC;

                    if(st.chargeResource[i] >= 1/g.resourceGen[i]){
                        st.chargeResource[i] -= 1/g.resourceGen[i];
                        if(st.resources[i] < 10000){
                            st.resources[i] += 1;
                            resourceChange = true;
                        }
                    }
                }
                //tell clients
                if(resourceChange && !state.players.get(st.owner).ai){
                    host.broadcastMessage(new MResourceChange(EntPtr.createFromEntity(st), st.resources));
                }
            }
        }
    }
    /**
     * Updates the ships that are docked.
     */
    private void updateDockedShips(){
        for(Grid g : state.grids){
            for(Ship s : g.station.dockedShips.values()){
                //increment health
                s.health += GameHost.FRAC * s.model.maxHealth * 0.02f * s.model.getDockedRegenMult();
                if(s.health > s.model.maxHealth) s.health = s.model.maxHealth;

                //weapon ticks
                for(Weapon w : s.weapons){
                    w.invalidTick(GameHost.FRAC);
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
                        s.moveDescription = "Stationary";
                    }
                }
                else if(s.movement == Ship.Movement.PREP_FOR_WARP){
                    Entity warpTarget = (s.warpTarget == null) ? null : s.warpTarget.retrieveFromGrid(state.grids[s.warpTarget.grid]);

                    //cancel prep
                    if(warpTarget == null || !warpTarget.isValidBeacon()){
                        s.moveDescription = "Warp cancelled";
                        s.trajectoryVel.set(0, 0);
                        s.warpTarget = null;
                    }
                    //continue with prep
                    else {
                        s.trajectoryVel.set(0, 0);
                        //start charging warp
                        if(s.vel.len() == 0){
                            s.moveDescription = "Charging warp to " + state.grids[warpTarget.grid].nickname;
                            shipBeginWarpCharging(s);
                        }
                    }
                }
                else if(s.movement == Ship.Movement.MOVE_TO_POS){
                    //already close enough to target position
                    if((distanceToTarget=s.pos.dst(s.moveTargetPos)) <= 0.01f){
                        s.trajectoryVel.set(0, 0);
                        if(s.vel.len() == 0){
                            s.movement = Ship.Movement.STOPPING;
                            s.moveDescription = "Stationary";
                        }
                    }
                    else {
                        //set the targetVel to the correct direction and normalize
                        s.trajectoryVel.set(s.moveTargetPos.x - s.pos.x,
                                s.moveTargetPos.y - s.pos.y).nor();

                        //find the effective max speed (compare speed can stop from to actual max speed)
                        float speedCanStopFrom = (float) Math.sqrt( 2*s.model.maxAccel*distanceToTarget );
                        float effectiveMaxSpeed = Math.min(0.8f*speedCanStopFrom, s.model.maxSpeed);

                        //update the target velocity
                        s.trajectoryVel.x *= effectiveMaxSpeed;
                        s.trajectoryVel.y *= effectiveMaxSpeed;
                    }
                }
                else if(s.movement == Ship.Movement.ORBIT_ENT){
                    Entity tar = null;
                    if(s.moveTargetEnt != null) tar = s.moveTargetEnt.retrieveFromGrid(g);

                    //TODO handle orbiting better at smaller radii
                    if(tar == null){
                        s.movement = Ship.Movement.STOPPING;
                        s.moveDescription = "Stationary";
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
                                .nor().scl(0.9f*s.model.maxSpeed);

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
                    if(length > s.model.maxAccel * GameHost.FRAC){
                        accel.x *= s.model.maxAccel * GameHost.FRAC /length;
                        accel.y *= s.model.maxAccel * GameHost.FRAC /length;
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
                int result = shipPhysicsOnGrid(g, s);

                if(result == 1) shipsToDock.add(s);
                else if(result == 2) shipsToRemove.add(s);
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
            int result = shipPhysicsInWarp(s);

            if(result == 1) {
                shipsToRemove.add(s);
            }
            else {
                for(Weapon w : s.weapons){
                    w.invalidTick(GameHost.FRAC);
                }
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
                if(!m.update(GameHost.FRAC, g)) {
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
            //station regen
            switch(g.station.stage){
                case SHIELDED:
                    g.station.shieldHealth += g.station.model.shieldRegen * GameHost.FRAC;
                    if(g.station.shieldHealth > g.station.model.maxShield) g.station.shieldHealth = g.station.model.maxShield;
                    break;
                case VULNERABLE:
                    g.station.hullHealth += g.station.model.hullRegen * GameHost.FRAC;
                    if(g.station.hullHealth > g.station.model.maxHull) g.station.hullHealth = g.station.model.maxHull;
                    break;
                case ARMORED:
                case RUBBLE:
            }

            //targeting and weapons
            for(Ship s : g.ships.values()){
                //update weapons
                for(Weapon w : s.weapons){
                    w.tick(state, g, GameHost.FRAC);
                }
            }

            //ship health checks
            for(Ship s : g.ships.values()){
                if(s.health <= 0){
                    host.broadcastMessage(new MRemShip(s.id, g.id, Ship.Removal.EXPLOSION, false));
                    toBeRemoved.add(s);

                    //tracking
                    state.players.get(s.lastHit).tracking.incrEntsKilled(s.entityModel().getTier());
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
                st.stageTimer = st.model.armoredDuration;
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
                    for(Weapon w : s.weapons){
                        w.stopTargetingEntity(st);
                    }
                }

                //tracking
                state.players.get(st.lastHit).tracking.incrEntsKilled(st.entityModel().getTier());

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
            //in space
            for(Ship s : g.ships.values()){
                host.broadcastMessage(MShipUpd.createFromShip(s));
            }
            //docked
            for(Ship s : g.station.dockedShips.values()){
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
        HashSet<Integer> stationOwners = new HashSet<>();
        for(Grid g : state.grids){
            if(g.station.owner != 0) stationOwners.add(g.station.owner);
        }
        if(stationOwners.size() > 1) return;

        //add event
        state.addToEventHistory(new EvGameEnd(winner));
        //tell host
        host.endGame(winner);
    }


    /* Loop Extension Helper Children */

    /**
     * Handles the physics for a single ship in space on a grid.
     * @return 0 for nothing, 1 to dock, 2 to remove from grid
     */
    private int shipPhysicsOnGrid(Grid g, Ship s){
        int result = 0;

        //move the ship
        if(Math.sqrt( Math.pow(s.pos.x + s.vel.x*GameHost.FRAC, 2) + Math.pow(s.pos.y + s.vel.y*GameHost.FRAC, 2) ) > g.radius){
            s.vel.set(0, 0);
            s.movement = Ship.Movement.STOPPING;
        }
        else {
            s.pos.x += s.vel.x * GameHost.FRAC;
            s.pos.y += s.vel.y * GameHost.FRAC;
        }

        //set the rotation
        if(s.vel.len() != 0){
            s.rot = (float) Math.atan2(s.vel.y, s.vel.x);
        }

        //cancel warp charge if needed
        if(s.vel.len() > 0 || s.movement != Ship.Movement.PREP_FOR_WARP){
            s.warpCharge = 0;
            s.warping = Ship.Warping.None;
        }

        //check if it should enter warp
        if(s.movement == Ship.Movement.PREP_FOR_WARP && s.vel.len() == 0){
            Entity target = s.warpTarget.retrieveFromGrid(state.grids[s.warpTarget.grid]);

            //charge for warp
            if(s.warpCharge < 1){
                s.warping = Ship.Warping.Charging;

                //set rotation
                s.rot = (float) Math.atan2(
                        state.grids[target.grid].loc.y - state.grids[s.grid].loc.y,
                        state.grids[target.grid].loc.x - state.grids[s.grid].loc.x);

                //charge
                s.warpCharge += 1f / (s.model.warpChargeTime*GameHost.TPS);
            }
            //enter warp
            else {
                //remove the ship at the end of the loop
                result = 2;
                //put in warp space
                state.shipsInWarp.put(s.id, s);

                //update warping stuff
                s.warpCharge = 0;
                s.warpTarget = null;
                s.warpPos.set(state.grids[s.grid].loc);
                s.warping = Ship.Warping.InWarp;
                s.warpSourceGrid = g.id;
                s.warpDestGrid = target.grid;

                //update warping physics
                chooseShipWarpLandingPos(s, target, state.grids[s.warpSourceGrid],
                        state.grids[s.warpDestGrid]);

                //update the movement description, physics, and targeting
                s.movement = Ship.Movement.WARPING;
                s.moveDescription = "Warping to " + state.grids[target.grid].nickname;
                s.pos.set(s.warpLandPos);
                s.grid = -1;

                //disable weapons
                for(Weapon w : s.weapons){
                    w.deactivate();
                }

                //tell the users
                host.broadcastMessage(new MShipEnterWarp(s.id, g.id, target.grid, s.warpLandPos));
            }
        }
        //check if the ship should dock
        if(s.movement == Ship.Movement.MOVE_FOR_DOCK){
            Station st = g.station;
            //dock
            if(s.pos.dst(st.pos) <= st.model.dockingRadius && st.owner == s.owner){
                result = 1;
            }
            //owner changed
            else if(st.owner != s.owner) {
                s.movement = Ship.Movement.STOPPING;
                s.moveDescription = "Stopping";
            }
        }

        return result;
    }
    /**
     * Handles the physics for a single ship that is in warp.
     * @return 0 for nothing, 1 to remove
     */
    private int shipPhysicsInWarp(Ship s){
        int result = 0;

        Vector2 warpVel = new Vector2(
                state.grids[s.warpDestGrid].loc.x-state.grids[s.warpSourceGrid].loc.x,
                state.grids[s.warpDestGrid].loc.y-state.grids[s.warpSourceGrid].loc.y
        );
        warpVel.nor().scl(s.model.warpSpeed/GameHost.TPS);

        //drop out of warp
        if(s.warpPos.dst(state.grids[s.warpDestGrid].loc) <= s.model.warpSpeed/GameHost.TPS){
            //remove from warp space
            result = 1;
            //add it to the correct grid
            state.grids[s.warpDestGrid].ships.put(s.id, s);
            s.grid = s.warpDestGrid;

            //exiting warp
            s.warping = Ship.Warping.None;
            s.warpDestGrid = -1;
            s.moveDescription = "Stopping";
            s.movement = Ship.Movement.STOPPING;

            //placing with correct physics
            s.pos.set(s.warpLandPos);
            s.vel.set(0, 0);
            if(s.pos.len() > state.grids[s.grid].radius){
                s.pos.nor().scl(state.grids[s.grid].radius);
            }

            //tell the users
            host.broadcastMessage(new MShipExitWarp(s.id, s.grid));
        }
        //move the ship in warp
        else {
            s.warpPos.add(warpVel);
        }

        return result;
    }
    /**
     * Called when a ship begins its warp charge.
     */
    private void shipBeginWarpCharging(Ship ship){
        //deactivate all weapons
        for(Weapon w : ship.weapons){
            w.deactivate();
        }
    }
    /**
     * Chooses and sets the warp landing point.
     */
    private void chooseShipWarpLandingPos(Ship ship, Entity target, Grid origin, Grid dest){
        //prepare geometry
        Vector2 warpDir = new Vector2(dest.loc.x-origin.loc.x, dest.loc.y-origin.loc.y).nor();
        Vector2 offset = new Vector2();
        Circle spot = new Circle(0, 0, ship.model.getLogicalRadius());

        //find all colliding points
        List<Circle> circles = new ArrayList<>();
        for(Ship s : dest.ships.values()){
            circles.add(new Circle(s.pos.x, s.pos.y, s.model.getLogicalRadius()));
        }
        for(Ship s : state.shipsInWarp.values()){
            if(s.warpDestGrid == dest.id){
                circles.add(new Circle(s.warpLandPos.x, s.warpLandPos.y, s.model.getLogicalRadius()));
            }
        }

        //find valid spot
        int ct = 1;
        boolean success = false;
        while(true){
            float d = 1.01f * (ct-1) * ship.model.getLogicalRadius();
            offset.set(-d*warpDir.x, -d*warpDir.y);

            for(int i=0; i<ct; i+=1){
                //increment from last
                if(i % 2 == 0){
                    offset.rotateDeg(i * 180f/ct);
                }
                else {
                    offset.rotateDeg(-i * 180f/ct);
                }

                //prepare values
                success = true;
                spot.setPosition(target.pos.x-warpDir.x, target.pos.y-warpDir.y);
                spot.x += offset.x;
                spot.y += offset.y;

                //loop through colliders
                for(Circle c : circles){
                    if(spot.overlaps(c)){
                        success = false;
                        break;
                    }
                }
                if(success) break;
            }

            if(success) break;
            //increment for next loop
            ct += 2;
        }

        ship.warpLandPos.set(spot.x, spot.y);
    }


    /* Client Request Handling */

    private void handleJobReq(int userId, MJobReq msg) {
        state.grids[6].ships.get(7).weapons[2].activate(null, null);
        Station s = (Station) state.findEntity(Entity.Type.Station, msg.stationGrid, msg.stationGrid, false);
        JobType j = msg.job;
        if(s == null) {
            new Quirk(Quirk.Q.MessageFromClientMismatch).print();
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
            CurrentJob currentJob = new CurrentJob(state.useNextJobId(), userId, j, s.grid, j.duration);
            s.currentJobs.add(currentJob);

            //send information to the client
            host.sendMessage(userId, new MChangeJob(MChangeJob.Action.ADDING,
                    currentJob.jobId, currentJob.grid, currentJob));
        }
    }

    private void handleShipMoveReq(int userId, MShipMoveReq msg){
        //get the ship
        Ship s = (Ship) state.findEntity(Entity.Type.Ship, msg.shipId, msg.grid, false);
        if(s == null){
            new Quirk(Quirk.Q.MessageFromClientMismatch).print();
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
            s.moveDescription = "Moving to (" + Main.df2.format(s.moveTargetPos.x) + ", "
                    + Main.df2.format(s.moveTargetPos.y) + ")";
        }

    }

    private void handleShipOrbitReq(int userId, MShipOrbitReq msg){
        //get the ship
        Ship s = (Ship) state.findEntity(Entity.Type.Ship, msg.shipId, msg.grid, false);
        if(s == null){
            new Quirk(Quirk.Q.MessageFromClientMismatch).print();
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
                s.moveTargetEnt = new EntPtr(msg.targetType, msg.targetId, msg.grid, false);
                s.moveRelativeDist = msg.radius;

                //set the description
                if(ent instanceof Station){
                    s.moveDescription = "Orbiting station";
                }
                else if(ent instanceof Ship) {
                    s.moveDescription = "Orbiting " + ((Ship) ent).entityModel();
                }
            }
        }
    }

    private void handleShipAlignReq(int userId, MShipAlignReq msg){
        //get the ship
        Ship s = (Ship) state.findEntity(Entity.Type.Ship, msg.shipId, msg.grid, false);
        if(s == null){
            new Quirk(Quirk.Q.MessageFromClientMismatch).print();
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
                    (float) Math.cos(msg.angle) * s.model.maxSpeed * 0.8f,
                    (float) Math.sin(msg.angle) * s.model.maxSpeed * 0.8f);

            //set the description of the movement
            int degrees = -((int) (msg.angle*180/Math.PI - 90));
            if(degrees < 0) degrees += 360;
            s.moveDescription = "Alinging to " + degrees + " N";
        }
    }

    private void handleShipWarpReq(int userId, MShipWarpReq msg){
        //get the ship
        Ship s = (Ship) state.findEntity(Entity.Type.Ship, msg.shipId, msg.grid, false);
        if(s == null){
            new Quirk(Quirk.Q.MessageFromClientMismatch).print();
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
            Entity warpTarget = state.findEntity(msg.beacon);

            //not valid
            if(warpTarget == null || !warpTarget.isValidBeacon() || warpTarget.grid == s.grid){
                MDenyRequest deny = new MDenyRequest(msg);
                deny.reason = "Not a valid beacon for warping";
                host.sendMessage(userId, deny);
            }
            //valid
            else {
                s.movement = Ship.Movement.PREP_FOR_WARP;
                s.warpTarget = msg.beacon;
                s.moveDescription = "Preparing to warp to " + state.grids[msg.beacon.grid].nickname;
            }
        }
    }

    private void handleWeaponActiveReq(int userId, MWeaponActiveReq msg){
        //get the ship
        Ship s = (Ship) state.findEntity(Entity.Type.Ship, msg.shipId, msg.grid, false);
        if(s == null){
            new Quirk(Quirk.Q.MessageFromClientMismatch).print();
            return;
        }

        //get the target
        Entity target = state.findEntity(msg.target);

        //check permissions
        MDenyRequest deny = new MDenyRequest(msg);
        if(s.owner != userId || s.docked || s.grid == -1 || s.weapons[msg.weaponId].cooldown > 0 ||
            s.warpCharge > 0){
            if(s.docked){
                deny.reason = "Cannot command a docked ship";
            }
            else if(s.grid == -1){
                deny.reason = "Cannot activate weapons while in warp";
            }
            else if(s.weapons[msg.weaponId].cooldown > 0){
                deny.reason = "That weapon is on cooldown";
            }
            else if(s.warpCharge > 0){
                deny.reason = "Cannot activate weapons while charging warp";
            }
            else {
                deny.reason = "Cannot command ship for unexpected reason";
            }

            host.sendMessage(userId, deny);
        }
        else {
            switch(s.weapons[msg.weaponId].getType()){
                case Blaster:
                case Laser: {
                    //deny
                    if(s.isValidBeacon()){
                        deny.reason = "Cannot activate weapons while beacon is active";
                        host.sendMessage(userId, deny);
                    }
                    //activate or deactivate
                    else {
                        if(msg.active){
                            if(target == null  || !s.weapons[msg.weaponId].checkEntityInRange(target)){
                                deny.reason = "Cannot activate weapon unexpectedly due to target or range";
                                host.sendMessage(userId, deny);
                            }
                            else {
                                s.weapons[msg.weaponId].activate(target, null);
                            }
                        }
                        else{
                            s.weapons[msg.weaponId].deactivate();
                        }
                    }
                    break;
                }
                case StationTrans: {
                    //check need to deny initially
                    if(s.isValidBeacon()){
                        deny.reason = "Cannot deploy station while beacon is active";
                        host.sendMessage(userId, deny);
                    }
                    else if(!(target instanceof Station)){
                        deny.reason = "Can only deploy to a station";
                        host.sendMessage(userId, deny);
                        break;
                    }
                    //convert to station and check need to deny
                    Station st = (Station) target;
                    if(st.stage != Station.Stage.RUBBLE){
                        deny.reason = "Station must be rubble to deploy";
                    }
                    else if(st.entityModel() != ((StationTrans) s.weapons[msg.weaponId]).cargo){
                        deny.reason = "Station type must match to be able to deploy";
                    }
                    else if(st.pos.dst(s.pos) > ((StationTrans) s.weapons[msg.weaponId]).model.range){
                        deny.reason = "Barge must be within range to deploy";
                    }
                    //accept
                    else {
                        //activate or deactivate
                        if(msg.active) s.weapons[msg.weaponId].activate(target, null);
                        else s.weapons[msg.weaponId].deactivate();
                    }
                    break;
                }
                case Beacon: {
                    //turning on
                    if(msg.active){
                        //check valid
                        if(s.vel.len2() > 0){
                            deny.reason = "Must be stationary to activate beacon";
                            host.sendMessage(userId, deny);
                        }
                        else if(s.countActiveWeapons() > 0){
                            deny.reason = "All weapons must be disabled to activate beacon";
                            host.sendMessage(userId, deny);
                        }
                        //is valid
                        else {
                            s.weapons[msg.weaponId].activate(null, null);
                            s.movement = Ship.Movement.STOPPING;
                            s.warpCharge = 0;
                        }
                    }
                    //turning off
                    else {
                        s.weapons[msg.weaponId].deactivate();
                    }

                    break;
                }
                case Doomsday: {
                    //deny
                    if(s.isValidBeacon() || s.vel.len() > 0){
                        deny.reason = "Cannot activate weapons while moving or while beacon is active";
                        host.sendMessage(userId, deny);
                    }
                    //activate or deactivate
                    else {
                        if(msg.active){
                            s.weapons[msg.weaponId].activate(null, msg.location);
                        }
                        else{
                            s.weapons[msg.weaponId].deactivate();
                        }
                    }
                    break;
                }
            }
        }
    }

    private void handleShipStopReq(int userId, MShipStopReq msg){
        //get the ship
        Ship s = (Ship) state.findEntity(Entity.Type.Ship, msg.shipId, msg.grid, false);
        if(s == null){
            new Quirk(Quirk.Q.MessageFromClientMismatch).print();
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
            s.moveDescription = "Stopping";
        }
    }

    private void handleShipUndockReq(int userId, MShipUndockReq msg){
        Ship s = (Ship) state.findEntity(Entity.Type.Ship, msg.shipId, msg.stationId, true);
        if(s == null) {
            new Quirk(Quirk.Q.MessageFromClientMismatch).print();
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
            s.vel.set(s.model.maxSpeed/3 * (float)Math.cos(s.rot), s.model.maxSpeed/3 * (float)Math.sin(s.rot));
            s.docked = false;

            //set ai
            s.movement = Ship.Movement.STOPPING;
            s.moveDescription = "Stopping";

            //reset other values
            for(Weapon w : s.weapons){
                w.deactivate();
            }
            s.warping = Ship.Warping.None;

            host.broadcastMessage(new MShipDockingChange(s.id, msg.stationId, false,
                    MShipUpd.createFromShip(s)));
        }
    }

    private void handleShipDockReq(int userId, MShipDockReq msg){
        Ship s = state.grids[msg.grid].ships.get(msg.shipId);
        if(s == null) {
            new Quirk(Quirk.Q.MessageFromClientMismatch).print();
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
            s.moveDescription = "Taxiing to dock";
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
                for(int i = 0; i< Ship.Model.Heron.weapons.length; i++) {
                    if(((StationTrans) sh.weapons[i]).cargo == null){
                        destIdx = i;
                        break;
                    }
                }

                //errors
                if(packedStationType == null){
                    new Quirk(Quirk.Q.MessageFromClientImprecise).print();

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
                ((StationTrans) sh.weapons[destIdx]).cargo = packedStationType;

                //tell players
                host.sendMessage(userId, new MPackedStationMove(
                        EntPtr.createFromEntity(st), msg.idxRemoveFrom,
                        EntPtr.createFromEntity(sh), destIdx,
                        packedStationType));
            }
            //move barge to station
            else if(removeFrom instanceof Barge && addTo instanceof Station){
                st = (Station) addTo;
                sh = (Barge) removeFrom;

                //final checks
                Station.Model packedStationType = ((StationTrans) sh.weapons[msg.idxRemoveFrom]).cargo;
                int destIdx = -1;
                for(int i=0; i<Station.PACKED_STATION_SLOTS; i++) {
                    if(st.packedStations[i] == null){
                        destIdx = i;
                        break;
                    }
                }

                //errors
                if(packedStationType == null){
                    new Quirk(Quirk.Q.MessageFromClientImprecise).print();

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
                ((StationTrans) sh.weapons[msg.idxRemoveFrom]).cargo = null;
                st.packedStations[destIdx] = packedStationType;

                //tell players
                host.sendMessage(userId, new MPackedStationMove(
                        EntPtr.createFromEntity(sh), msg.idxRemoveFrom,
                        EntPtr.createFromEntity(st), destIdx,
                        packedStationType));
            }
            else {
                new Quirk(Quirk.Q.MessageFromClientImprecise).print();
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
                    MResourceChange stMsg = new MResourceChange(EntPtr.createFromEntity(st), st.resources);
                    host.sendMessage(st.owner, stMsg);
                    MResourceChange shMsg = new MResourceChange(EntPtr.createFromEntity(sh), sh.resources);
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
                    MResourceChange shMsg = new MResourceChange(EntPtr.createFromEntity(sh), sh.resources);
                    host.sendMessage(sh.owner, shMsg);
                    MResourceChange stMsg = new MResourceChange(EntPtr.createFromEntity(st), st.resources);
                    host.sendMessage(st.owner, stMsg);
                }
            }
            else {
                new Quirk(Quirk.Q.MessageFromClientImprecise).print();
            }
        }

    }

}
