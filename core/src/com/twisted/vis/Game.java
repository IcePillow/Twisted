package com.twisted.vis;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.twisted.Main;
import com.twisted.logic.GameHost;
import com.twisted.logic.desiptors.CurrentJob;
import com.twisted.logic.desiptors.Gem;
import com.twisted.logic.desiptors.Grid;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.Station;
import com.twisted.net.client.Client;
import com.twisted.net.client.ClientContact;
import com.twisted.net.msg.*;
import com.twisted.net.msg.gameRequest.MJobRequest;
import com.twisted.net.msg.gameUpdate.MAddShip;
import com.twisted.net.msg.gameUpdate.MChangeJob;
import com.twisted.net.msg.gameUpdate.MGameOverview;
import com.twisted.net.msg.gameUpdate.MShipUpd;
import com.twisted.net.msg.remaining.MDenyRequest;
import com.twisted.net.msg.remaining.MGameStart;
import com.twisted.vis.state.GameState;

import java.util.ArrayList;
import java.util.Map;


/**
 * The Game Screen
 *
 * Structure Comment
    > shapeRenderer
    > stage
        > minimapGroup (shows map and allows switching between grids)
        > fleetGroup (shows player controlled entities)
            ~ fleetWindowGroup
            ~ fleetDetailsGroup
        > industryGroup (shows the stations and summary of industry operations)
        > optionsGroup (opened with esc for changing settings)
 */
public class Game implements Screen, ClientContact {

    //static references
    private static final String[] COLOR_FILENAMES = {"blue", "orange", "gray"};

    //exterior references
    private Main main;
    private GameHost host;
    public void setHost(GameHost host){
        this.host = host;
    }
    private Client client;
    public void setClient(Client client){
        this.client = client;
    }

    //game state tracking
    private GameState state;

    //visual state tracking
    private int activeGridId; //the id of the active grid
    private int industryFocusedStationId = -1; //the grid id of the focused station

    //graphics high level utilities
    private Stage stage;
    private Skin skin;
    private OrthographicCamera camera;
    private Vector2 camPos;
    private SpriteBatch sprite;

    //graphics personalized utilities
    private Thread industryLogFadeThread;

    //top level groups
    private Group minimapGroup, fleetGroup, industryGroup, optionsGroup;

    //lower level industry actors
    private VerticalGroup industryVertical, jobQueueWidget;
    private Label industryFocusStation, industryLogLabel;


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
        camera.update();
        sprite.setProjectionMatrix(camera.combined);
        if(state != null && state.readyToRender) {
            renderViewport();
            renderIndustry();
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
        sprite.dispose();
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
                state.myId = m.yourPlayerId;
                state.mapWidth = m.mapWidth;
                state.mapHeight = m.mapHeight;
                state.grids = m.grids;

                //load the graphics on the gdx thread
                Gdx.app.postRunnable(() -> {
                    loadGraphics();
                    loadMinimap();
                    loadViewport();
                    loadIndustry();

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
                updateIndustryLog(deny.reason, new float[]{1,0,0});
            }
        }
        else if(msg instanceof MAddShip){
            MAddShip add = (MAddShip) msg;

            state.grids[add.grid].ships.put(add.ship.shipId, add.ship);
        }
        else if(msg instanceof MShipUpd){
            MShipUpd upd = (MShipUpd) msg;
            upd.copyDataToShip(state.grids[upd.grid].ships.get(upd.shipId));
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


    /* Handling Input */

    /**
     * Called each tick to handle user input.
     */
    private void handleInput(){

        //move the camera around
        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
            camera.translate(5, 0);
            camPos.x += 5;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.translate(-5, 0);
            camPos.x -= 5;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.translate(0, 5);
            camPos.y += 5;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.translate(0, -5);
            camPos.y -= 5;
        }

    }

    /**
     * Called when the current grid being looked at in the viewport needs to be switched.
     */
    private void switchGrid(int newGrid){
        activeGridId = newGrid;

        //undo all movements of the camera and update the position
        camera.translate(-camPos.x, -camPos.y);
        camPos.x = 0;
        camPos.y = 0;

        //move minimap selection square
        minimapGroup.getChild(2).setPosition(3 + state.grids[activeGridId].x*250f/1000f - 10,
                3 + state.grids[activeGridId].y*250f/1000f - 10);
    }

    /**
     * Called when the user clicks on a station in the industry menu.
     */
    private void industryFocusStation(Station station){

        industryFocusedStationId = station.grid;
        industryFocusStation.setText(station.name);

        //add to have enough children
        for(int i=jobQueueWidget.getChildren().size; i<station.currentJobs.size(); i++){
            jobQueueWidget.addActor(new Label("X", skin, "small", Color.LIGHT_GRAY));
        }
        //remove to not have too many children
        for(int i=jobQueueWidget.getChildren().size; i>station.currentJobs.size(); i--){
            jobQueueWidget.removeActorAt(i-1, false).clear();
        }

        //fill in the current data
        for(int i=0; i<jobQueueWidget.getChildren().size; i++){
            ((Label) jobQueueWidget.getChild(i)).setText(station.currentJobs.get(i).jobType + "  " +
                    (int) Math.ceil(station.currentJobs.get(i).timeLeft));
        }

    }

    /**
     * Called when the user attempts to start a job at a station.
     */
    private void industryJobRequest(Station station, Station.Job job){
        updateIndustryLog("Build " + job.name() + " @ " + station.name,
                new float[]{0.7f, 0.7f, 0.7f});

        client.send(new MJobRequest(station.grid, job));
    }

    /**
     * Called when the industry log window needs to have a new message displayed.
     * @param color Array of length 3 with the initial color in rgb form.
     */
    private void updateIndustryLog(String string, float[] color){

        //deal with current thread if it exists
        if(industryLogFadeThread != null) industryLogFadeThread.interrupt();

        //set up the new string and initial color
        industryLogLabel.setText(string);
        industryLogLabel.setColor(color[0], color[1], color[2], 1);

        //create and start the new thread
        industryLogFadeThread = new Thread(() -> {
            try {
                Thread.sleep(500);

                for(float i=1; i>0; i-=0.1f){
                    //check a new string hasn't overwritten
                    if(!(industryLogLabel.getText().toString().equals(string))) break;

                    //update color then wait
                    industryLogLabel.setColor(color[0], color[1], color[2], i);
                        Thread.sleep(80);
                }
            } catch (InterruptedException e) {
                //exit
            }

        });
        industryLogFadeThread.start();
    }


    /* Deal with Graphics */

    /**
     * Initial function for loading
     *
     * Init Functions - Called once upon construction of the Game class.
     * Load Functions - Called when the gameStart message is received from the network sector.
     * Render Functions - Called each frame.
     */
    private void initGraphics(){

        //load the skin
        skin = new Skin(Gdx.files.internal("skins/sgx/skin/sgx-ui.json"));

        //and the background
        initViewport();

        //and the groups
        stage.addActor(initMinimapGroup());
        stage.addActor(initFleetGroup());
        stage.addActor(initIndustryGroup());
        stage.addActor(initOptionsGroup());

        //logic
        activeGridId = 0;
    }
    private void loadGraphics(){
        for(Grid g : state.grids){
            if(g.station.owner == state.myId){
                switchGrid(g.id);
                break;
            }
        }
    }

    /**
     * Viewport
     */
    private void initViewport(){

        camera = new OrthographicCamera(stage.getWidth(), stage.getHeight());
        camPos = new Vector2(0, 0);

        sprite = new SpriteBatch();

    }
    private void loadViewport(){

        //load the background
        state.viewportBackground = new Texture(Gdx.files.internal("images/pixels/navy.png"));

        //load in the station graphics
        for(Station.Type type : Station.Type.values()){
            String s1 = type.name().toLowerCase();

            //loop through the possible colors
            for(String s2 : COLOR_FILENAMES){
                Station.viewportSprites.put(s1 + "-" + s2,
                        new Texture(Gdx.files.internal("images/stations/" + s1 + "-" + s2 + ".png")));
            }
        }

        //load in the ship graphics
        for(Ship.Type type : Ship.Type.values()){
            String s1 = type.name().toLowerCase();

            for(String s2 : COLOR_FILENAMES){
                Ship.viewportSprites.put(s1 + "-" + s2,
                        new Texture(Gdx.files.internal("images/ships/" + s1 + "-" + s2 + ".png")));
            }
        }

    }
    private void renderViewport(){

        //access the grid and start drawing
        Grid g = state.grids[activeGridId];
        sprite.begin();

        //background
        sprite.draw(state.viewportBackground, camPos.x-stage.getWidth()/2f, camPos.y-stage.getHeight()/2f,
                stage.getWidth(), stage.getHeight());

        //draw the station
        Texture stationTexture;
        if(g.station.owner == 0) {
            stationTexture = Station.viewportSprites.get(g.station.getFilename().toLowerCase() + "-gray");
        }
        else {
            stationTexture = Station.viewportSprites.get(g.station.getFilename().toLowerCase() + "-" + state.players.get(g.station.owner).color.file);
        }
        sprite.draw(stationTexture, -g.station.getSize().x/2f, -g.station.getSize().y/2f,
                g.station.getSize().x/2f, g.station.getSize().y/2f,
                g.station.getSize().x, g.station.getSize().y,
                1, 1, 0f,
                0, 0, 128, 128, false, false);

        //draw the ships
        for(Ship ship : g.ships.values()){
            Texture shipTexture;
            if(ship.owner == 0) {
                shipTexture = Ship.viewportSprites.get(ship.getFilename().toLowerCase() + "-gray");
            }
            else {
                shipTexture = Ship.viewportSprites.get(ship.getFilename().toLowerCase() + "-" + state.players.get(ship.owner).color.file);
            }

            sprite.draw(shipTexture, ship.position.x-ship.getSize().x/2f, ship.position.y-ship.getSize().y/2f,
                    ship.getSize().x/2f, ship.getSize().y/2f,
                    ship.getSize().x, ship.getSize().y,
                    1, 1, ship.rotation,
                    0, 0, 16, 16, false, false);
        }

        //end drawing
        sprite.end();
    }

    /**
     * Minimap
     */
    private Group initMinimapGroup(){
        minimapGroup = new Group();
        minimapGroup.setBounds(Main.WIDTH-256, 0, 256, 256);

        Image main = new Image(new Texture(Gdx.files.internal("images/pixels/darkpurple.png")));
        main.setSize(minimapGroup.getWidth(), minimapGroup.getHeight());
        minimapGroup.addActor(main);

        Image embedded = new Image(new Texture(Gdx.files.internal("images/pixels/black.png")));
        embedded.setPosition(3, 3);
        embedded.setSize(minimapGroup.getWidth()-6, minimapGroup.getHeight()-6);
        minimapGroup.addActor(embedded);

        Image activeSquare = new Image(new Texture(Gdx.files.internal("images/ui/white-square-1.png")));
        activeSquare.setPosition(minimapGroup.getWidth()/2, minimapGroup.getHeight()/2);
        activeSquare.setSize(20, 20);
        minimapGroup.addActor(activeSquare);

        return minimapGroup;
    }
    private void loadMinimap(){

        //load the grids
        for(Grid g : state.grids){
            //load the minimap icon
            if(g.station.owner == 0){
                g.station.minimapSprite = new Image(new Texture(Gdx.files.internal("images/circles/gray.png")));
            }
            else {
                g.station.minimapSprite = new Image(new Texture(Gdx.files.internal("images/circles/"
                        + state.players.get(g.station.owner).color.file + ".png")));
            }

            //position is (indent + scaled positioning - half the width)
            g.station.minimapSprite.setPosition(3 + g.x*250f/1000f - 5, 3 + g.y*250f/1000f - 5);
            g.station.minimapSprite.setSize(10, 10);

            //load the minimap label
            Label label = new Label(g.station.name, skin, "small", Color.GRAY);
            g.station.minimapLabel = label;
            label.setVisible(false);
            if(g.station.minimapSprite.getX() < 3+label.getWidth()/2f){
                label.setPosition((g.x*250f/1000f-label.getWidth()/2f) + (3+label.getWidth()/2f) - (g.station.minimapSprite.getX()), g.y*250f/1000f + 6);
            }
            else if(g.station.minimapSprite.getX() + label.getWidth()/2f > 248){
                label.setPosition((g.x*250f/1000f-label.getWidth()/2f) - (g.station.minimapSprite.getX()+label.getWidth()/2f) + (248), g.y*250f/1000f + 6);
            }
            else {
                label.setPosition((g.x*250f/1000f-label.getWidth()/2f), g.y*250f/1000f + 6);
            }

            //add to the minimap group
            minimapGroup.addActor(g.station.minimapSprite);
            minimapGroup.addActor(g.station.minimapLabel);

            //listeners
            g.station.minimapSprite.addListener(event -> {
                //entering and exiting
                if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.enter){
                    g.station.minimapLabel.setVisible(true);
                }
                else if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.exit){
                    g.station.minimapLabel.setVisible(false);
                }

                return false;
            });

            g.station.minimapSprite.addListener(new ClickListener(Input.Buttons.LEFT){
                @Override
                public void clicked(InputEvent event, float x, float y){
                    //update the active grid
                    switchGrid(g.id);
                }
            });
        }

        //move the active square
        Grid g = state.grids[activeGridId];
        minimapGroup.getChild(2).setPosition(3 + g.x*250f/1000f - 10, 3 + g.y*250f/1000f - 10);

    }

    /**
     * Fleet Window Group
     */
    private Group initFleetGroup(){
        //initialize the top level group
        fleetGroup = new Group();
        fleetGroup.setBounds(0, 100, 300, 550);

        //create the subgroups
        Group detailsGroup = new Group();
        detailsGroup.setSize(300, 125);
        fleetGroup.addActor(detailsGroup);
        Group windowGroup = new Group();
        windowGroup.setBounds(0, 130, 300, 420);
        fleetGroup.addActor(windowGroup);

        //add the main window backgrounds
        Image detailsMain = new Image(new Texture(Gdx.files.internal("images/pixels/darkpurple.png")));
        detailsMain.setSize(detailsGroup.getWidth(), detailsGroup.getHeight());
        detailsGroup.addActor(detailsMain);

        Image windowMain = new Image(new Texture(Gdx.files.internal("images/pixels/darkpurple.png")));
        windowMain.setSize(windowGroup.getWidth(), windowGroup.getHeight());
        windowGroup.addActor(windowMain);

        return fleetGroup;
    }

    /**
     * Industry Window
     */
    private Group initIndustryGroup(){
        final int FOCUS_HEIGHT = 150;
        final int LOG_HEIGHT = 20;

        //initialize the top level group
        industryGroup = new Group();
        industryGroup.setBounds(Main.WIDTH-275, 260, 275, 410); //original height=395

        //main background
        Image main = new Image(new Texture(Gdx.files.internal("images/pixels/darkpurple.png")));
        main.setSize(industryGroup.getWidth(), industryGroup.getHeight());
        industryGroup.addActor(main);

        /* primary scroll pane */

        //create the main scroll pane
        industryVertical = new VerticalGroup();
        industryVertical.top().left();
        industryVertical.columnAlign(Align.left);

        ScrollPane pane = new ScrollPane(industryVertical, skin);
        pane.setBounds(3, 3 + FOCUS_HEIGHT+3, industryGroup.getWidth()-6,
                industryGroup.getHeight()-6 - (3+FOCUS_HEIGHT) - (3+LOG_HEIGHT));
        pane.setScrollingDisabled(true, false);
        pane.setupFadeScrollBars(0.2f, 0.2f);
        pane.setSmoothScrolling(false);
        pane.setColor(Color.BLACK);

        industryGroup.addActor(pane);

        /* focus */

        //create the focus group
        Group focusGroup = new Group();
        focusGroup.setBounds(3, 3, industryGroup.getWidth()-6, FOCUS_HEIGHT);
        industryGroup.addActor(focusGroup);

        //make the background image
        Image focusBackground = new Image(new Texture(Gdx.files.internal("images/pixels/black.png")));
        focusBackground.setSize(focusGroup.getWidth(), focusGroup.getHeight());
        focusGroup.addActor(focusBackground);

        //name of the station currently in focus
        industryFocusStation = new Label("[Station]", skin, "small", Color.WHITE);
        industryFocusStation.setPosition(3, FOCUS_HEIGHT- industryFocusStation.getHeight()-3);
        focusGroup.addActor(industryFocusStation);

        //job queue title
        Label jobQueueTitle = new Label("Job Queue", skin, "small", Color.GRAY);
        jobQueueTitle.setPosition(focusGroup.getWidth()-3-150, industryFocusStation.getY()-jobQueueTitle.getHeight());
        focusGroup.addActor(jobQueueTitle);

        //job queue pane
        jobQueueWidget = new VerticalGroup();
        jobQueueWidget.left();
        ScrollPane queuePane = new ScrollPane(jobQueueWidget, skin);
        queuePane.setBounds(focusGroup.getWidth()-3-150, 3, 150, jobQueueTitle.getY()-3);
        queuePane.setColor(Color.GRAY);
        focusGroup.addActor(queuePane);

        //TODO add a flag/color for the current state (i.e. armored, reinforced)

        /* Log Group */
        Group logGroup = new Group();
        logGroup.setBounds(3, industryGroup.getHeight()-3-LOG_HEIGHT, industryGroup.getWidth()-6, LOG_HEIGHT);
        industryGroup.addActor(logGroup);

        //make the image
        Image logBackground = new Image(new Texture(Gdx.files.internal("images/pixels/black.png")));
        logBackground.setSize(logGroup.getWidth(), logGroup.getHeight());
        logGroup.addActor(logBackground);

        //make the label
        industryLogLabel = new Label("Log label", skin, "small", Color.GRAY);
        industryLogLabel.setPosition(3, -1);
        logGroup.addActor(industryLogLabel);


        return industryGroup;
    }
    private void loadIndustry(){
        //loop through the grids
        for(Grid g : state.grids){

            /* Top Level for Station */
            VerticalGroup stationGroup = new VerticalGroup();
            stationGroup.columnAlign(Align.left);
            industryVertical.addActor(stationGroup);

            /* Title bar for station */
            HorizontalGroup stationTitleBar = new HorizontalGroup();

            //create and add the dropdown icon
            Image dropdown = new Image(new Texture(Gdx.files.internal("images/ui/gray-arrow-3.png")));
            dropdown.setOrigin(dropdown.getWidth()/2f, dropdown.getHeight()/2f);
            stationTitleBar.addActor(dropdown);

            //create and add the name label
            Label stationNameLabel = new Label(g.station.name, skin, "small");
            stationNameLabel.setAlignment(Align.left);
            stationTitleBar.addActor(stationNameLabel);

            //add the group to the station group
            stationGroup.addActor(stationTitleBar);

            /* Expanded Section */
            VerticalGroup child = new VerticalGroup();
            child.columnAlign(Align.left);

            Table resourceBar = new Table();
            resourceBar.padLeft(16);

            //calculate widths for resource bar
            int allowedImageWidth = 18;
            int allowedLabelWidth = (int) Math.floor((industryVertical.getWidth()-10-16-18*4)/4);

            //loop through gem files
            g.station.industryResourceLabels = new Label[4];
            int index=0;
            for(String filename : new String[]{"calcite", "kernite", "pyrene", "crystal"}){

                Image image = new Image(new Texture(Gdx.files.internal("images/gems/" + filename + ".png")));
                Label label = new Label("0", skin, "small");

                resourceBar.add(image).minWidth(allowedImageWidth);
                resourceBar.add(label).minWidth(allowedLabelWidth);

                //add to the station object
                g.station.industryResourceLabels[index] = label;
                index++;
            }
            child.addActor(resourceBar);

            /* Jobs */
            //create costs groups
            Table jobTable = new Table();
            jobTable.align(Align.left);
            jobTable.padLeft(12).padBottom(5);
            child.addActor(jobTable);

            //loop through all the jobs
            for(Station.Job job : g.station.getPossibleJobs()){

                Label nameLabel = new Label(job.name(), skin, "small", Color.WHITE);
                nameLabel.setColor(Color.GRAY);
                jobTable.add(nameLabel).align(Align.left).padRight(10);

                //create the cost labels
                Label[] costLabels = new Label[4];
                int i=0;
                for(Gem gem : Gem.orderedGems){
                    costLabels[i] = new Label(""+job.getGemCost(gem), skin, "small", Color.WHITE);
                    costLabels[i].setColor(Color.GRAY);
                    jobTable.add(costLabels[i]).width(40);
                    i++;
                }

                jobTable.row();

                //listener for color change
                nameLabel.addListener(new InputListener() {
                    /*
                    These extra complications were necessary because of what seems like a bug in
                    the library. Clicking on the actor without moving causes an enter event then an
                    exit event to occur. These two booleans track and account for this.
                     */
                    boolean entered = false;
                    boolean extraEnter = false;
                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, @Null Actor fromActor){
                        if(entered){
                            extraEnter = true;
                        }
                        else {
                            changeNodeColors(Color.LIGHT_GRAY);
                            entered = true;
                        }
                    }
                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, @Null Actor fromActor){
                        if(extraEnter){
                            extraEnter = false;
                        }
                        else {
                            entered = false;
                            changeNodeColors(Color.GRAY);
                        }
                    }

                    //changes the color to light gray and back
                    private void changeNodeColors(Color color){
                        nameLabel.setColor(color);
                        for(Label label : costLabels){
                            label.setColor(color);
                        }
                    }
                });
                //listener for requesting jobs on clicks
                nameLabel.addListener(new ClickListener(Input.Buttons.LEFT) {
                    @Override
                    public void clicked(InputEvent event, float x, float y){
                        industryJobRequest(g.station, job);
                    }
                });
            }

            /* Add listeners */
            dropdown.addListener(new ClickListener(Input.Buttons.LEFT){
                private boolean down = false;
                @Override
                public void clicked(InputEvent event, float x, float y){
                    if(down){
                        dropdown.rotateBy(90);

                        //remove the child
                        stationGroup.removeActor(child);
                    }
                    //not down
                    else {
                        dropdown.rotateBy(-90);

                        //add the child
                        stationGroup.addActorAfter(stationTitleBar, child);
                    }
                    down = !down;
                }
            });
            stationNameLabel.addListener(new ClickListener(Input.Buttons.LEFT){
                @Override
                public void clicked(InputEvent event, float x, float y){
                    industryFocusStation(g.station);
                }
            });

            /* Visibility */
            if(g.station.owner != state.myId){
                stationGroup.getParent().removeActor(stationGroup);

                //TODO add them back to the parent when ownership is regained
            }

        }
    }
    private void renderIndustry(){
        //update the times on the current jobs
        if(industryFocusedStationId != -1){
            ArrayList<CurrentJob> arr = state.grids[industryFocusedStationId].station.currentJobs;

            //add to have enough children
            for(int i=jobQueueWidget.getChildren().size; i<arr.size(); i++){
                jobQueueWidget.addActor(new Label("", skin, "small", Color.LIGHT_GRAY));
            }
            //remove to not have too many children
            for(int i=jobQueueWidget.getChildren().size; i>arr.size(); i--){
                jobQueueWidget.removeActorAt(i-1, false).clear();
            }

            for(int i=0; i<arr.size(); i++){
                CurrentJob job = arr.get(i);
                ((Label) jobQueueWidget.getChild(i)).setText(job.jobType + "  " + (int) Math.ceil(job.timeLeft));
            }

        }
        //update the resources per station
        for(Grid g : state.grids){
            for(int i=0; i<g.station.industryResourceLabels.length; i++){
                g.station.industryResourceLabels[i].setText(g.station.resources[i]);
            }
        }
    }

    /**
     * Options Menu
     */
    private Group initOptionsGroup(){

        //create the group
        optionsGroup = new Group();
        optionsGroup.setBounds(420, 150, 600, 500);

        //set the background
        Image main = new Image(new Texture(Gdx.files.internal("images/pixels/black.png")));
        main.setSize(optionsGroup.getWidth(), optionsGroup.getHeight());
        optionsGroup.addActor(main);

        //toggle visibility of the options menu
        stage.addListener(new InputListener() {
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == 111){
                    optionsGroup.setVisible(! optionsGroup.isVisible());
                }
                return true;
            }
        });

        //make invisible and return
        optionsGroup.setVisible(false);
        return optionsGroup;
    }
}
