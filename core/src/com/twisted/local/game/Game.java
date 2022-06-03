package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.twisted.Main;
import com.twisted.logic.game.GameHost;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.*;
import com.twisted.net.client.Client;
import com.twisted.net.client.ClientContact;
import com.twisted.net.msg.*;
import com.twisted.net.msg.gameRequest.MGameRequest;
import com.twisted.net.msg.gameRequest.MJobRequest;
import com.twisted.net.msg.gameUpdate.*;
import com.twisted.net.msg.remaining.MDenyRequest;
import com.twisted.net.msg.remaining.MGameStart;
import com.twisted.local.game.state.GameState;

import java.util.Map;


/**
 * The Game Screen
 *
 * Size Comparison
     > To go from logical units to rendered units, multiply by 100
 */
public class Game implements Screen, ClientContact {

    //static references
    static final String[] COLOR_FILENAMES = {"blue", "orange", "gray"};
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
    private SecMinimap minimapSector;
    private SecFleet fleetSector;
    private SecIndustry industrySector;
    private SecOptions optionsSector;
    private SecViewport viewportSector;
    private SecDetails detailsSector;

    //cross-sector tracking
    private Sector viewportListener;

    //visual state tracking
    private int grid; //the id of the active grid
    public int getGrid(){
        return grid;
    }

    //graphics high level utilities
    private Stage stage;
    private Skin skin;


    /* Constructor */

    public Game(Main main) {
        this.main = main;

        stage = new Stage(new FitViewport(Main.WIDTH, Main.HEIGHT));
        Gdx.input.setInputProcessor(stage);

        initGraphics();
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

            //
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

        //station sprites
        for(String key : Station.viewportSprites.keySet().toArray(new String[0])){
            Station.viewportSprites.remove(key).dispose();
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
    public void clientReceived(Message msg) {
        if(msg instanceof MGameStart){
            if(state != null){
                System.out.println("[Warning] Unexpected MGameStart received with an active GameState");
            }
            else {
                //get the message and create the state
                MGameStart m = (MGameStart) msg;
                state = new GameState(m.getPlayers(), m.getColors());

                //copy data
                state.serverTickDelay = m.tickDelay;
                state.myId = m.yourPlayerId;
                state.mapWidth = m.mapWidth;
                state.mapHeight = m.mapHeight;

                //copy grid data
                state.grids = new Grid[m.gridPositions.length];
                for(int i=0; i < state.grids.length; i++) {
                    state.grids[i] = new Grid(i, m.gridPositions[i]);
                    if(m.stationTypes[i] == Station.Type.Extractor){
                        state.grids[i].station = new Extractor(i, m.stationNames[i], m.stationOwners[i], m.stationStages[i], true);
                    }
                    else if(m.stationTypes[i] == Station.Type.Harvester){
                        state.grids[i].station = new Harvester(i, m.stationNames[i], m.stationOwners[i], m.stationStages[i], true);
                    }
                    else if(m.stationTypes[i] == Station.Type.Liquidator){
                        state.grids[i].station = new Liquidator(i, m.stationNames[i], m.stationOwners[i], m.stationStages[i], true);
                    }
                }

                //pass the state reference
                for(Sector sector : sectors){
                    sector.setState(state);
                }

                //load the graphics on the gdx thread
                Gdx.app.postRunnable(() -> {

                    loadGraphics();
                    state.readyToRender = true;
                });
            }
        }
        else if(msg instanceof MGameOverview){
            MGameOverview m = (MGameOverview) msg;

            //update each timer
            for(Map.Entry<Integer, Float> e : m.jobToTimeLeft.entrySet()){
                state.jobs.get(e.getKey()).timeLeft = e.getValue();
            }
            //update station resources
            for(Map.Entry<Integer, int[]> e : m.stationToResources.entrySet()){
                System.arraycopy(e.getValue(), 0, state.grids[e.getKey()].station.resources, 0, e.getValue().length);
            }
        }
        else if(msg instanceof MChangeJob){
            MChangeJob m = (MChangeJob) msg;

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
        else if(msg instanceof MDenyRequest){
            MDenyRequest deny = (MDenyRequest) msg;

            if(deny.request instanceof MJobRequest){
                industrySector.updateIndustryLog(deny.reason, new float[]{1,0,0});
            }
        }
        else if(msg instanceof MAddShip){
            MAddShip add = (MAddShip) msg;

            if(add.type == Ship.Type.Frigate){
                //create the ship, add the ship, then retrieve it
                state.grids[add.grid].ships.put(add.shipId, add.createDrawableShip());
                Ship ship = state.grids[add.grid].ships.get(add.shipId);

                ship.polygon.setPosition(ship.position.x, ship.position.y);
                ship.polygon.rotate(ship.rotation);
            }
            //TODO other types of ships
        }
        else if(msg instanceof MShipUpd){
            MShipUpd upd = (MShipUpd) msg;

            Ship ship = state.grids[upd.grid].ships.get(upd.shipId);

            upd.copyDataToShip(ship);
            ship.updatePolygon();

            if(detailsSector.selectedShipId == ship.id) detailsSector.updateShipData(ship);
        }
    }

    @Override
    public void disconnected(String reason){
        //TODO this function
    }

    @Override
    public void lostConnection() {
        //TODO this function
    }


    /* High Level Input Handling */

    /**
     * Called each tick to handle user input.
     */
    private void handleInput(){

        //move the camera around
        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
            viewportSector.moveCamera(SecViewport.Direction.RIGHT);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A)) {
            viewportSector.moveCamera(SecViewport.Direction.LEFT);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.W)) {
            viewportSector.moveCamera(SecViewport.Direction.UP);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)) {
            viewportSector.moveCamera(SecViewport.Direction.DOWN);
        }

    }

    /**
     * Called by a sector to start listening to input on the viewport. New requests overwrite
     * old requests. To stop listening, set receiver as null.
     * @param receiver The sector that should receive the event notifications. Usually the same as
     *                 the sector that called this method.
     */
    void startViewportListen(Sector receiver){
        if(viewportListener != null) {
            viewportListener.viewportClickEvent(-1, null, null, null, -1);
        }
        viewportListener = receiver;
    }

    /**
     * Called when a viewport click event occurs.
     * @param typeId Different value based on type. For SPACE, ignored. For SHIP, shipId.
     */
    void viewportClickEvent(int button, Vector2 screenPos, Vector2 gamePos,
                            SecViewport.ClickType type, int typeId){
        //normal behavior
        if(viewportListener == null){
            //selecting a ship for details
            if(type == SecViewport.ClickType.SHIP && button == Input.Buttons.LEFT){
                shipSelectedForDetails(grid, typeId);
            }
        }
        //responding to the details sector
        else if(viewportListener == detailsSector){
            detailsSector.viewportClickEvent(button, screenPos, gamePos, type, typeId);
        }
    }

    /**
     * Send a game request to the server.
     */
    void sendGameRequest(MGameRequest request){
        client.send(request);
    }


    /* Input Handling Utility */

    /**
     * Called when the user selects a particular ship's details to be displayed.
     */
    private void shipSelectedForDetails(int gridId, int shipId){
        detailsSector.shipSelected(gridId, shipId);
    }

    /**
     * Called when the current grid being looked at in the viewport needs to be switched.
     */
    void switchGrid(int newGrid){
        grid = newGrid;

        viewportSector.switchFocusedGrid();
        minimapSector.switchFocusedGrid(newGrid);
    }

    /**
     * Stop listening to the viewport.
     */
    private void releaseViewportListen(){
        viewportListener = null;
    }


    /* Deal with Graphics */

    /**
     * Called during construction.
     */
    private void initGraphics(){

        //load the skin
        skin = new Skin(Gdx.files.internal("skins/sgx/skin/sgx-ui.json"));

        //prepare the viewport
        viewportSector = new SecViewport(this, skin, stage);
        viewportSector.init();

        //prepare the other sectors
        minimapSector = new SecMinimap(this, skin);
        stage.addActor(minimapSector.init());

        fleetSector = new SecFleet(this, skin);
        stage.addActor(fleetSector.init());

        detailsSector = new SecDetails(this, skin);
        stage.addActor(detailsSector.init());

        industrySector = new SecIndustry(this, skin);
        stage.addActor(industrySector.init());

        optionsSector = new SecOptions(this, skin, stage);
        stage.addActor(optionsSector.init());

        this.sectors = new Sector[]{
                viewportSector, minimapSector, fleetSector, detailsSector, industrySector, optionsSector
        };

        //logic
        grid = 0;
    }

    /**
     * Called when the server sends the start message.
     */
    private void loadGraphics(){

        for(Grid g : state.grids){
            if(g.station.owner == state.myId){
                switchGrid(g.id);
                break;
            }
        }

        viewportSector.load();
        minimapSector.load();
        industrySector.load();
        fleetSector.load();
        optionsSector.load();
    }



}
