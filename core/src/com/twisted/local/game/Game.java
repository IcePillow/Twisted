package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.twisted.Main;
import com.twisted.local.game.state.GameState;
import com.twisted.logic.host.GameHost;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.*;
import com.twisted.logic.mobs.BlasterBolt;
import com.twisted.logic.mobs.Mobile;
import com.twisted.net.client.Client;
import com.twisted.net.client.ClientContact;
import com.twisted.net.msg.*;
import com.twisted.net.msg.gameReq.MGameReq;
import com.twisted.net.msg.gameReq.MJobReq;
import com.twisted.net.msg.gameReq.MShipMoveReq;
import com.twisted.net.msg.gameUpdate.*;
import com.twisted.net.msg.remaining.MDenyRequest;
import com.twisted.net.msg.remaining.MGameStart;

import java.util.Map;


/**
 * The Game Screen
 */
public class Game implements Screen, ClientContact {

    //static references
    public static final float LTR = 100; //logical to rendered

    //exterior references
    private Main main;
    private GameHost host;
    public void setHost(GameHost host){
        this.host = host;
    }
    Client client;
    public void setClient(Client client){
        this.client = client;
    }

    //game state tracking
    private GameState state;

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
    public GlyphLayout glyph;


    /* Constructor */

    public Game(Main main) {
        this.main = main;

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

        //high level sprites
        state.viewportBackground.dispose();

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
     *
     * TODO break this apart into multiple utility methods or another class
     */
    @Override
    public void clientReceived(Message m) {
        if(m instanceof MGameStart) receiveGameStart((MGameStart) m);
        else if(m instanceof MGameOverview) receiveGameOverview((MGameOverview) m);
        else if(m instanceof MChangeJob) receiveChangeJob((MChangeJob) m);
        else if(m instanceof MDenyRequest) receiveDenyRequest((MDenyRequest) m);
        else if(m instanceof MAddShip) receiveAddShip((MAddShip) m);
        else if(m instanceof MShipUpd) receiveShipUpd((MShipUpd) m);
        else if(m instanceof MShipEnterWarp) receiveShipEnterWarp((MShipEnterWarp) m);
        else if(m instanceof MShipExitWarp) receiveShipExitWarp((MShipExitWarp) m);
        else if(m instanceof MMobileUps) receiveMobileUps((MMobileUps) m);
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
            state = new GameState(m.getPlayers(), m.getColors());

            //copy data
            state.serverTickDelay = m.tickDelay;
            state.myId = m.yourPlayerId;
            state.mapWidth = m.mapWidth;
            state.mapHeight = m.mapHeight;

            //copy grid data
            state.grids = new Grid[m.gridPositions.length];
            for(int i=0; i < state.grids.length; i++) {
                state.grids[i] = new Grid(i, m.gridPositions[i], m.gridNicknames[i]);
                if(m.stationTypes[i] == Station.Type.Extractor){
                    state.grids[i].station = new Extractor(i, state.grids[i].nickname, m.stationOwners[i], m.stationStages[i], true);
                }
                else if(m.stationTypes[i] == Station.Type.Harvester){
                    state.grids[i].station = new Harvester(i, state.grids[i].nickname, m.stationOwners[i], m.stationStages[i], true);
                }
                else if(m.stationTypes[i] == Station.Type.Liquidator){
                    state.grids[i].station = new Liquidator(i, state.grids[i].nickname, m.stationOwners[i], m.stationStages[i], true);
                }
            }

            //pass the state reference
            for(Sector sector : sectors){
                sector.setState(state);
            }

            //load the graphics on the gdx thread
            Gdx.app.postRunnable(() -> {
                for(Grid g : state.grids){
                    //create the graphics
                    g.station.createFleetRow(skin, state, fleetSec);
                }

                loadSectors();
                state.readyToRender = true;
            });
        }
    }

    private void receiveGameOverview(MGameOverview m){
        //update each timer
        for(Map.Entry<Integer, Float> e : m.jobToTimeLeft.entrySet()){
            state.jobs.get(e.getKey()).timeLeft = e.getValue();
        }
        //update station resources
        for(Map.Entry<Integer, int[]> e : m.stationToResources.entrySet()){
            System.arraycopy(e.getValue(), 0, state.grids[e.getKey()].station.resources, 0, e.getValue().length);
        }
    }

    private void receiveChangeJob(MChangeJob m){
        if(m.action == MChangeJob.Action.ADDING){
            state.jobs.put(m.job.jobId, m.job);
            state.grids[m.job.grid].station.currentJobs.add(m.job);
        }
        else if(m.action == MChangeJob.Action.FINISHED){
            state.jobs.remove(m.jobId);
            state.grids[m.gridId].station.currentJobs.remove(0);
        }
        else if(m.action == MChangeJob.Action.CANCELING){
            //TODO
        }
    }

    private void receiveDenyRequest(MDenyRequest m){
        if(m.request instanceof MJobReq){
            logSec.addToLog(m.reason, SecLog.LogColor.RED);
        }
        else if(m.request instanceof MShipMoveReq){
            logSec.addToLog(m.reason, SecLog.LogColor.GRAY);
        }
    }

    private void receiveAddShip(MAddShip m){
        //create the ship
        Ship ship = null;
        if(m.type == Ship.Type.Frigate){
            //create the ship, add the ship, then retrieve it
            state.grids[m.grid].ships.put(m.shipId, m.createDrawableShip());
            ship = state.grids[m.grid].ships.get(m.shipId);
        }

        //set things in the ship
        if(ship != null) {
            //physics
            ship.polygon.setPosition(ship.pos.x, ship.pos.y);
            ship.polygon.rotate(ship.rot);

            //graphics
            ship.createFleetRow(skin, state, fleetSec);
        }
        //TODO other types of ships
    }

    private void receiveShipUpd(MShipUpd m){
        Ship ship;
        if(m.grid != -1){
            ship = state.grids[m.grid].ships.get(m.shipId);
        }
        else {
            ship = state.inWarp.get(m.shipId);
        }

        m.copyDataToShip(ship);

        //update visuals if not in warp
        if(m.grid != -1) ship.updatePolygon();

        //update the sectors if needed
        if(detailsSec.selectShipId == ship.id){
            detailsSec.updateShipData(ship, m.grid);
        }
        viewportSec.updateSelectionGridsAsNeeded(ship, m.grid);
        fleetSec.upsertEntity(ship, m.grid);
    }

    private void receiveShipEnterWarp(MShipEnterWarp m){
        //move the ship to "in warp"
        Ship ship = state.grids[m.originGridId].ships.get(m.shipId);
        state.grids[m.originGridId].ships.remove(ship.id);
        state.inWarp.put(ship.id, ship);

        //update sectors
        fleetSec.updateEntityEnterWarp(ship, m.originGridId);
    }

    private void receiveShipExitWarp(MShipExitWarp m){
        Ship ship = state.inWarp.get(m.shipId);
        state.inWarp.remove(ship.id);
        state.grids[m.destGridId].ships.put(ship.id, ship);

        //update sectors
        fleetSec.updateEntityExitWarp(ship, m.destGridId);
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


    /* High Level Input Handling */

    /**
     * Called each tick to handle user input.
     */
    private void handleInput(){

        //move the camera around
        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
            viewportSec.moveCamera(SecViewport.Direction.RIGHT);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A)) {
            viewportSec.moveCamera(SecViewport.Direction.LEFT);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.W)) {
            viewportSec.moveCamera(SecViewport.Direction.UP);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)) {
            viewportSec.moveCamera(SecViewport.Direction.DOWN);
        }

    }

    /**
     * Modularization method for adding to the log.
     */
    void addToLog(String text, SecLog.LogColor logColor){
        logSec.addToLog(text, logColor);
    }


    /* Cross Sector Listening */

    /**
     * Called by a sector to start listening to input on the viewport. New requests overwrite
     * old requests. To stop listening, set receiver as null.
     * @param receiver The sector that should receive the event notifications. Usually the same as
     *                 the sector that called this method.
     * @param listenStatus A user-readable string describing what is being listened for.
     */
    void updateCrossSectorListening(Sector receiver, String listenStatus){
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
     * @param typeId Different value based on type. For SPACE, ignored. For SHIP, shipId.
     */
    void viewportClickEvent(int button, Vector2 screenPos, Vector2 gamePos,
                            Entity.Type type, int typeId){
        //normal behavior
        if(crossSectorListener == null){
            //selecting a ship for details
            if(type == Entity.Type.Ship && button == Input.Buttons.LEFT){
                shipSelectedForDetails(grid, typeId);
            }
        }
        //responding to external listeners
        else if(button == Input.Buttons.LEFT) {
            crossSectorListener.viewportClickEvent(screenPos, gamePos, type, typeId);
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
     * Called when a ship is selected from the fleet window.
     */
    void fleetClickEvent(int button, Entity entity){
        int gridId = state.findShipGridId(entity.getId());

        //normal behavior
        if(crossSectorListener == null){
            if(entity instanceof Ship){
                shipSelectedForDetails(gridId, entity.getId());
            }
        }
        //responding to listeners
        else {
            crossSectorListener.fleetClickEvent(entity, gridId);
        }
    }


    /* Input Handling Utility */

    /**
     * Called when the user selects a particular ship's details to be displayed.
     */
    private void shipSelectedForDetails(int gridId, int shipId){
        detailsSec.shipSelected(gridId, shipId);

        //TODO separate this out from selecting for details (maybe?)
        viewportSec.updateSelection(SecViewport.Select.BASIC, true, Entity.Type.Ship,
                gridId, shipId);
    }


    /* External Facing Input Handling */

    void viewportSelection(SecViewport.Select select, boolean toggle, Entity.Type type, int grid,
                           int id){
        viewportSec.updateSelection(select, toggle, type, grid, id);
    }

    /**
     * Called when the current grid being looked at in the viewport needs to be switched.
     */
    void switchGrid(int newGrid){
        grid = newGrid;

        viewportSec.switchFocusedGrid();
        minimapSec.switchFocusedGrid(newGrid);
        fleetSec.reloadTabEntities();
    }

    /**
     * Send a game request to the server.
     */
    void sendGameRequest(MGameReq request){
        client.send(request);
    }

    /**
     * Sets the scroll focus to the passed in actor. Null is allowed.
     */
    void scrollFocus(Actor actor){
        stage.setScrollFocus(actor);
    }


    /* Deal with Graphics */

    /**
     * Called during construction.
     */
    private void initSectors(){

        //load the skin and glyph
        skin = new Skin(Gdx.files.internal("skins/sgx/skin/sgx-ui.json"));
        glyph = new GlyphLayout();

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
