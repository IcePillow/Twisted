package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.twisted.Main;
import com.twisted.local.curtain.Curtain;
import com.twisted.local.game.cosmetic.Explosion;
import com.twisted.local.game.state.ClientGameState;
import com.twisted.logic.descriptors.CurrentJob;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Gem;
import com.twisted.logic.entities.attach.StationTransport;
import com.twisted.logic.host.game.GameHost;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.*;
import com.twisted.logic.mobs.BlasterBolt;
import com.twisted.logic.mobs.Mobile;
import com.twisted.net.client.Client;
import com.twisted.net.client.ClientContact;
import com.twisted.net.msg.*;
import com.twisted.net.msg.gameReq.MGameReq;
import com.twisted.net.msg.gameReq.MJobReq;
import com.twisted.net.msg.gameUpdate.*;
import com.twisted.net.msg.remaining.MDenyRequest;
import com.twisted.net.msg.lobby.MGameStart;

import java.util.*;


/**
 * The Game Screen
 */
public class Game implements Screen, ClientContact {

    //static references
    public static final float LTR = 100; //logical to rendered

    //exterior references
    private final Main main;
    private GameHost host;
    public void setHost(GameHost host){
        this.host = host;
    }
    Client client;
    public void setClient(Client client){
        this.client = client;
    }

    //game state tracking
    private ClientGameState state;

    //sectors
    private Sector[] sectors;
    private SecMinimap minimapSec;
    private SecFleet fleetSec;
    private SecIndustry industrySec;
    private SecOptions optionsSec;
    private SecViewport viewportSec;
    private SecDetails detailsSec;
    private SecOverlay overlaySec;
    private SecLog logSec;

    //cross sector
    private Sector crossSectorListener;

    //visual state tracking
    private int grid; //the id of the active grid
    public int getGrid(){
        return grid;
    }

    //graphics high level utilities
    private final Stage stage;
    public Skin skin;


    /* Constructor */

    public Game(Main main) {
        //copy references
        this.main = main;

        //graphics
        stage = new Stage(new FitViewport(Main.WIDTH, Main.HEIGHT));
        Gdx.input.setInputProcessor(stage);
        initSectors();
    }


    /* Screen Methods */

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        //reset background
        Gdx.gl.glClearColor(0, 0, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //input
        handleInput();

        //live drawings
        if(state != null && state.readyToRender) {
            //render each sector
            for(Sector sector : sectors){
                sector.render(delta);
            }
        }

        //scene2d updates
        Main.glyph.reset();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        //top level
        skin.dispose();
        stage.dispose();

        //sectors
        for(Sector s : sectors){
            s.dispose();
        }
    }


    /* ClientContact Methods */

    /**
     * Should never be called in Game.
     */
    @Override
    public void connectedToServer() {

    }

    /**
     * Should never be called in Game.
     */
    @Override
    public void failedToConnect() {

    }

    /**
     * Receiving messages from the server.
     */
    @Override
    public void clientReceived(Message m) {
        if(m instanceof MGameStart) receiveGameStart((MGameStart) m);
        else if(m instanceof MJobTimerUpd) receiveGameOverview((MJobTimerUpd) m);
        else if(m instanceof MChangeJob) receiveChangeJob((MChangeJob) m);
        else if(m instanceof MDenyRequest) receiveDenyRequest((MDenyRequest) m);
        else if(m instanceof MAddShip) receiveAddShip((MAddShip) m);
        else if(m instanceof MShipUpd) receiveShipUpd((MShipUpd) m);
        else if(m instanceof MShipEnterWarp) receiveShipEnterWarp((MShipEnterWarp) m);
        else if(m instanceof MShipExitWarp) receiveShipExitWarp((MShipExitWarp) m);
        else if(m instanceof MMobileUps) receiveMobileUps((MMobileUps) m);
        else if(m instanceof MRemShip) receiveRemShip((MRemShip) m);
        else if(m instanceof MShipDockingChange) receiveShipDockingChange((MShipDockingChange) m);
        else if(m instanceof MStationUpd) receiveStationUpd((MStationUpd) m);
        else if(m instanceof MPackedStationMove) receivePackedStationMove((MPackedStationMove) m);
        else if(m instanceof MGameEnd) receiveGameEnd((MGameEnd) m);
        else if(m instanceof MResourceChange) receiveResourceChange((MResourceChange) m);
    }

    @Override
    public void disconnected(String reason){
        //TODO this function
    }

    @Override
    public void lostConnection() {
        //TODO this function
    }


    /* Client Message Receiving */

    private void receiveGameStart(MGameStart m){
        if(state != null){
            System.out.println("[Warning] Unexpected MGameStart received with an active GameState");
        }
        else {
            //get the message and create the state
            state = new ClientGameState(m.getPlayers(), m.getPlayerFiles());

            //copy data
            state.serverTickDelay = m.tickDelay;
            state.myId = m.yourPlayerId;
            state.mapWidth = m.mapWidth;
            state.mapHeight = m.mapHeight;

            //copy grid data
            state.grids = new Grid[m.gridPositions.length];
            for(int i=0; i < state.grids.length; i++) {
                state.grids[i] = new Grid(i, m.gridPositions[i], m.gridNicknames[i]);
                if(m.stationTypes[i] == Station.Model.Extractor){
                    state.grids[i].station = new Extractor(i, state.grids[i].nickname, m.stationOwners[i], m.stationStages[i]);
                }
                else if(m.stationTypes[i] == Station.Model.Harvester){
                    state.grids[i].station = new Harvester(i, state.grids[i].nickname, m.stationOwners[i], m.stationStages[i]);
                }
                else if(m.stationTypes[i] == Station.Model.Liquidator){
                    state.grids[i].station = new Liquidator(i, state.grids[i].nickname, m.stationOwners[i], m.stationStages[i]);
                }

                System.arraycopy(m.stationResources[i], 0, state.grids[i].station.resources,
                        0, Gem.NUM_OF_GEMS);
            }

            //pass the state reference
            for(Sector sector : sectors){
                sector.setState(state);
            }

            //load the graphics on the gdx thread
            Gdx.app.postRunnable(() -> {
                loadSectors();

                for(Grid g : state.grids){
                    fleetSec.checkAddEntity(g.station);
                    industrySec.stationResourceUpdate(g.station);
                }

                state.readyToRender = true;
            });
        }
    }

    private void receiveGameOverview(MJobTimerUpd m){
        //update each job timer
        for(Map.Entry<Integer, Float> e : m.jobTimers.entrySet()){
            CurrentJob job = state.jobs.get(e.getKey());
            job.timeLeft = e.getValue();

            //tell sectors
            industrySec.upsertStationJob(job.grid, job, state.grids[grid].station.currentJobs.indexOf(job));
        }
    }

    private void receiveChangeJob(MChangeJob m){
        if(m.action == MChangeJob.Action.ADDING){
            state.jobs.put(m.job.jobId, m.job);
            state.grids[m.job.grid].station.currentJobs.add(m.job);

            //tell sectors
            industrySec.upsertStationJob(m.gridId, m.job, state.grids[m.job.grid].station.currentJobs.size()-1);
        }
        else if(m.action == MChangeJob.Action.FINISHED){
            state.jobs.remove(m.jobId);
            CurrentJob job = state.grids[m.gridId].station.currentJobs.remove(0);

            //tell sectors
            industrySec.removeStationJob(m.gridId, job);
        }
        else if(m.action == MChangeJob.Action.BLOCKING){
            state.jobs.get(m.jobId).blocking = true;

            String nick = state.grids[m.gridId].nickname;
            addToLog("Your job at grid " + nick + " is blocking", SecLog.LogColor.RED);
        }
        else if(m.action == MChangeJob.Action.CANCELING){
            //TODO
        }
    }

    private void receiveDenyRequest(MDenyRequest m){
        if(m.request instanceof MJobReq){
            logSec.addToLog(m.reason, SecLog.LogColor.RED);
        }
        else {
            logSec.addToLog(m.reason, SecLog.LogColor.GRAY);
        }
    }

    private void receiveAddShip(MAddShip m){
        //create the ship, add the ship, then retrieve it
        Ship ship = m.createDrawableShip();
        if(!ship.docked){
            state.grids[m.grid].ships.put(m.shipId, ship);
        }
        else {
            state.grids[m.grid].station.dockedShips.put(m.shipId, ship);
        }

        //tell sectors
        fleetSec.checkAddEntity(ship);
        if(ship.docked) industrySec.addDockedShip(ship);
    }

    private void receiveShipUpd(MShipUpd m){
        Ship s;
        //not in warp
        if(m.grid != -1){
            //not docked
            if(!m.docked) {
                s = state.grids[m.grid].ships.get(m.shipId);
            }
            else {
                s = state.grids[m.grid].station.dockedShips.get(m.shipId);
            }
        }
        else {
            s = state.inWarp.get(m.shipId);
        }

        m.copyDataToShip(s);

        //update visuals if not in warp
        if(m.grid != -1) s.updatePolygon();

        //update the sectors if needed
        detailsSec.updateEntity(EntPtr.createFromEntity(s));
        viewportSec.updateSelectionGridsAsNeeded(s, m.grid);
        fleetSec.updEntityValues(s);
    }

    private void receiveShipEnterWarp(MShipEnterWarp m){
        //move the ship to "in warp"
        Ship ship = state.grids[m.originGridId].ships.get(m.shipId);
        ship.grid = -1;
        state.grids[m.originGridId].ships.remove(ship.id);
        state.inWarp.put(ship.id, ship);

        //tell sectors
        fleetSec.reloadEntity(ship);
        detailsSec.reloadEntity(ship);
    }

    private void receiveShipExitWarp(MShipExitWarp m){
        Ship ship = state.inWarp.get(m.shipId);
        ship.grid = m.destGridId;
        state.inWarp.remove(ship.id);
        state.grids[m.destGridId].ships.put(ship.id, ship);

        //tell sectors
        fleetSec.reloadEntity(ship);
        detailsSec.reloadEntity(ship);
    }

    private void receiveMobileUps(MMobileUps m){
        //if the mobile already exists
        if(state.grids[m.gridId].mobiles.containsKey(m.mobileId)){
            if(m.fizzle){
                state.grids[m.gridId].mobiles.remove(m.mobileId);
            }
            else {
                Mobile mob = state.grids[m.gridId].mobiles.get(m.mobileId);
                mob.pos = m.pos;
                mob.vel = m.vel;
                mob.rot = m.rot;
            }
        }
        //if it does not
        else if(!m.fizzle) {
            Mobile mob = null;

            switch(m.type){
                case BlasterBolt:
                    mob = new BlasterBolt(m.mobileId, m.pos, null, null,
                            -1);
                    break;
            }

            if(mob != null){
                mob.vel = m.vel;
                mob.rot = m.rot;
                state.grids[m.gridId].mobiles.put(m.mobileId, mob);
            }
        }
    }

    private void receiveRemShip(MRemShip m){
        //get the object
        EntPtr ptr = new EntPtr(Entity.Type.Ship, m.shipId, m.grid, m.docked);
        Ship ship = (Ship) state.findEntity(ptr);

        //remove from the state
        state.grids[grid].ships.remove(m.shipId);
        state.inWarp.remove(m.shipId);

        //tell sectors
        detailsSec.deselectEntity(EntPtr.createFromEntity(ship));
        viewportSec.removeSelectionsOfEntity(ptr);
        fleetSec.checkRemoveEntity(ship);

        //explosion
        if(ship != null && m.grid != -1){
            Explosion explosion = new Explosion(m.grid, ship.subtype().getPaddedLogicalRadius(), 1f, ship.pos);
            explosion.color = state.findColorForOwner(ship.owner);
            viewportSec.addCosmetic(explosion);
        }
    }

    private void receiveShipDockingChange(MShipDockingChange m){
        if(m.docked){
            //get ship and update values
            Ship s = state.grids[m.grid].ships.get(m.shipId);
            m.update.copyDataToShip(s);

            //move the ship
            state.grids[m.grid].ships.remove(s.id);
            state.grids[m.grid].station.dockedShips.put(s.id, s);

            //tell the sectors
            industrySec.addDockedShip(s);
            detailsSec.reloadEntity(s);
            fleetSec.checkRemoveEntity(s);
            fleetSec.updEntityValues(s);
        }
        else {
            //get the ship and update values
            Ship s = state.grids[m.grid].station.dockedShips.get(m.shipId);
            m.update.copyDataToShip(s);

            //move the ship
            state.grids[m.grid].station.dockedShips.remove(s.id);
            state.grids[m.grid].ships.put(s.id, s);

            //tell the sectors
            industrySec.removeDockedShip(m.grid, s);
            detailsSec.reloadEntity(s);
            fleetSec.checkAddEntity(s);
        }
    }

    private void receiveStationUpd(MStationUpd m){
        Station s = state.grids[m.stationId].station;
        Station.Stage oldStage = s.stage;

        //copy data
        m.copyDataToStation(s);

        //if newly rubble
        if(oldStage != Station.Stage.RUBBLE && s.stage == Station.Stage.RUBBLE){
            s.dockedShips.clear();
            s.currentJobs.clear();
            Arrays.fill(s.resources, 0);
        }

        //update sectors generally
        detailsSec.updateEntity(s);
        fleetSec.updEntityValues(s);

        //update sectors on stage change
        if(oldStage != s.stage){
            minimapSec.updateStation(s);
            fleetSec.checkRemoveEntity(s);
            fleetSec.checkAddEntity(s);
            industrySec.stationStageUpdate(s);
        }
    }

    private void receivePackedStationMove(MPackedStationMove m){
        //remove
        Entity entity = state.findEntity(m.removeFrom);
        if(entity instanceof Station){
            ((Station) entity).packedStations[m.idxRemoveFrom] = null;

            //tell sectors
            industrySec.checkRemovePackedStation(m.removeFrom.id, m.type);
            for(Ship s : ((Station) entity).dockedShips.values()){
                if(s instanceof Barge) detailsSec.updateEntity(s);
            }
        }
        else if(entity instanceof Barge){
            ((StationTransport) ((Barge) entity).weapons[m.idxRemoveFrom]).cargo = null;

            detailsSec.updateEntity(entity);
        }

        //add
        entity = state.findEntity(m.addTo);
        if(entity instanceof Station){
            Station st = (Station) entity;
            for(int i=0; i<st.packedStations.length; i++){
                if(st.packedStations[i] == null){
                    st.packedStations[i] = m.type;
                    break;
                }
                else if(i == st.packedStations.length-1){
                    System.out.println("Trying to add packed station when there are no slots");
                    new Exception().printStackTrace();
                }
            }

            //tell sectors
            industrySec.checkAddPackedStation(m.addTo.id, m.type);
            for(Ship s : st.dockedShips.values()){
                if(s instanceof Barge) detailsSec.updateEntity(s);
            }
        }
        else if(entity instanceof Barge){
            ((StationTransport) ((Barge) entity).weapons[m.idxAddTo]).cargo = m.type;

            detailsSec.updateEntity(entity);
        }
    }

    private void receiveGameEnd(MGameEnd m){
        //end
        state.ending = true;
        optionsSec.ending(m);

        //close the client
        client.shutdown();
    }

    private void receiveResourceChange(MResourceChange m){
        Entity ent = state.findEntity(m.entity);
        int[] resources;

        //get the resources object
        if(ent instanceof Station){
            resources = ((Station) ent).resources;
        }
        else if(ent instanceof Barge){
            resources = ((Barge) ent).resources;
        }
        else {
            System.out.println("Unexpected entity changing resources");
            new Exception().printStackTrace();
            return;
        }

        //change the resources
        for(int i=0; i<resources.length; i++){
            resources[i] += m.resourceChanges[i];
        }

        //update the sectors
        if(ent instanceof Station){
            industrySec.stationResourceUpdate((Station) ent);

            for(Ship sh : ((Station) ent).dockedShips.values()){
                if(sh instanceof Barge) detailsSec.updateEntity(sh);
            }
        }
        detailsSec.updateEntity(ent);
    }


    /* High Level Input Handling */

    /**
     * Called each tick to handle user input.
     */
    private void handleInput(){
        if(state != null && state.ending) return;

        for(Sector s : sectors){
            s.keyboardInput();
        }
    }

    /**
     * Modularization method for adding to the log.
     */
    void addToLog(String text, SecLog.LogColor logColor){
        logSec.addToLog(text, logColor);
    }


    /* Listening to the Sectors */

    /**
     * Called by a sector to start listening to input on the viewport. New requests overwrite
     * old requests. To stop listening, set receiver as null.
     * @param receiver The sector that should receive the event notifications. Usually the same as
     *                 the sector that called this method.
     * @param listenStatus A user-readable string describing what is being listened for.
     */
    void updateCrossSectorListening(Sector receiver, String listenStatus){
        if(state.ending) return;

        //cancel the current listener
        if(crossSectorListener != null) {
            crossSectorListener.crossSectorListeningCancelled();
        }

        //update to the new listener and receiver
        crossSectorListener = receiver;

        //change the cosmetic status
        if(crossSectorListener == null || listenStatus == null){
            overlaySec.updateActionLabel("");
        }
        else {
            overlaySec.updateActionLabel(listenStatus);
        }
    }

    /**
     * Called when a viewport click event occurs.
     */
    void viewportClickEvent(int button, Vector2 screenPos, Vector2 gamePos, EntPtr ptr){
        if(state.ending) return;

        //normal behavior
        if(crossSectorListener == null){
            //selecting a ship for details
            if(ptr != null && button == Input.Buttons.LEFT){
                Entity entity = state.findEntity(ptr);
                entitySelectedForDetails(entity);
            }
        }
        //responding to external listeners
        else if(button == Input.Buttons.LEFT) {
            crossSectorListener.viewportClickEvent(screenPos, gamePos, ptr);
        }
        //cancelling external listeners
        else {
            crossSectorListener.crossSectorListeningCancelled();
            overlaySec.updateActionLabel("");
        }
    }

    /**
     * Called when a minimap click event occurs.
     */
    void minimapClickEvent(int button, int grid){
        if(state.ending) return;

        //normal behavior
        if(crossSectorListener == null){
            if(button == Input.Buttons.LEFT){
                switchGrid(grid);
            }
        }
        //responding to external listeners
        else if(button == Input.Buttons.LEFT) {
            crossSectorListener.minimapClickEvent(grid);
        }
    }

    /**
     * Called when an entity is selected from the fleet window.
     */
    void fleetClickEvent(Entity entity){
        if(state.ending) return;

        //normal behavior
        if(crossSectorListener == null){
            entitySelectedForDetails(entity);
        }
        //responding to listeners
        else {
            crossSectorListener.fleetClickEvent(EntPtr.createFromEntity(entity));
        }
    }

    /**
     * Called when an entity is selected for external viewing in the industry window.
     */
    void industryClickEvent(Entity entity){
        if(state.ending) return;

        entitySelectedForDetails(entity);
    }

    /**
     * Called when a click event occurs in the options sector.
     */
    void optionsClickEvent(SecOptions.OptionEvent event, Object obj){
        if(event == SecOptions.OptionEvent.END_GAME){
            Gdx.app.postRunnable(() -> {
                //create the new curtain
                Curtain curtain = new Curtain(main, (MGameEnd) obj, state);

                //change screen
                main.setScreen(curtain);
                this.dispose();
            });
        }
    }


    /* Input Handling Utility */

    /**
     * Called when the user selects a particular ship's details to be displayed.
     */
    private void entitySelectedForDetails(Entity entity){
        detailsSec.selectEntity(entity);

        viewportSec.updateSelection(SecViewport.Select.BASE_SELECT, true,
                EntPtr.createFromEntity(entity), Color.LIGHT_GRAY, 0);
    }


    /* External Facing Direct Input */

    /**
     * A selection is made in the viewport
     */
    void viewportSelection(SecViewport.Select select, boolean toggle, EntPtr ptr, Color color, float value){
        if(state.ending) return;

        viewportSec.updateSelection(select, toggle, ptr, color, value);
    }

    /**
     * Called when the current grid being looked at in the viewport needs to be switched.
     */
    void switchGrid(int newGrid){
        if(state.ending) return;

        grid = newGrid;

        viewportSec.switchFocusedGrid();
        minimapSec.switchFocusedGrid(newGrid);
        fleetSec.switchGridFocus();
    }

    /**
     * Send a game request to the server.
     */
    void sendGameRequest(MGameReq request){
        if(state.ending) return;

        client.send(request);
    }

    /**
     * Sets the scroll focus to the passed in actor. Null is allowed.
     */
    void scrollFocus(Actor actor){
        stage.setScrollFocus(actor);
    }

    /**
     * Sets the keyboard focus to the passed in actor. Null is allowed.
     */
    void keyboardFocus(Actor actor){
        stage.setKeyboardFocus(actor);
    }


    /* Deal with Graphics */

    /**
     * Called during construction.
     */
    private void initSectors(){

        //load the skin
        skin = new Skin(Gdx.files.internal("skins/sgx/skin/sgx-ui.json"));
        skin.getFont("small").getData().markupEnabled = true;
        skin.getFont("medium").getData().markupEnabled = true;
        skin.getFont("title").getData().markupEnabled = true;

        //prepare the viewport
        viewportSec = new SecViewport(this, stage);
        viewportSec.init();

        //prepare the other sectors
        minimapSec = new SecMinimap(this);
        stage.addActor(minimapSec.init());

        fleetSec = new SecFleet(this);
        stage.addActor(fleetSec.init());

        detailsSec = new SecDetails(this);
        stage.addActor(detailsSec.init());

        industrySec = new SecIndustry(this);
        stage.addActor(industrySec.init());

        logSec = new SecLog(this);
        stage.addActor(logSec.init());

        overlaySec = new SecOverlay(this);
        stage.addActor(overlaySec.init());

        optionsSec = new SecOptions(this, stage);
        stage.addActor(optionsSec.init());

        //set the sector array
        this.sectors = new Sector[]{
                viewportSec, minimapSec, fleetSec, detailsSec, industrySec,
                logSec, overlaySec, optionsSec
        };

        //logic
        grid = 0;
    }

    /**
     * Called when the server sends the start message.
     */
    private void loadSectors(){
        for(Sector s : sectors){
            s.load();
        }

        for(Grid g : state.grids){
            if(g.station.owner == state.myId){
                switchGrid(g.id);
                break;
            }
        }
    }

}
