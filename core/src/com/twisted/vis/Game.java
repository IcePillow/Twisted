package com.twisted.vis;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.twisted.Main;
import com.twisted.logic.Grid;
import com.twisted.net.client.ClientContact;
import com.twisted.net.msg.MGameStart;
import com.twisted.net.msg.MGameState;
import com.twisted.net.msg.Message;
import com.twisted.vis.state.GameState;


/**
 * The Game Screen
 *
 * Structure Comment
    > stage
        > [background image]
        > viewGroup (main viewport)
        > minimapGroup (shows map and allows switching between grids)
        > fleetGroup (shows player controlled entities)
            ~ fleetWindowGroup
            ~ fleetDetailsGroup
        > industryGroup (shows the stations and summary of industry operations)
        > optionsGroup (opened with esc for changing settings)
 */
public class Game implements Screen, ClientContact {

    //exterior references
    private Main main;
    private ClientsideContact contact;
    public void setContact(ClientsideContact contact){
        this.contact = contact;
    }

    //state tracking
    private GameState state;

    //graphics
    private Stage stage;
    private Skin skin;

    //top level groups
    private Group viewGroup, minimapGroup, fleetGroup, industryGroup, optionsGroup;


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
        Gdx.gl.glClearColor(0, 0, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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

    }


    /* ClientContact Methods */

    @Override
    public void connectedToServer() {

    }

    @Override
    public void failedToConnect() {

    }

    @Override
    public void clientReceived(Message msg) {

        //standard message updating on the state of the game
        if(msg instanceof MGameState){

            //TODO update the state

            //TODO update the visuals

        }
        //start the game and load initial information
        if(msg instanceof MGameStart){
            if(state != null){
                System.out.println("[Warning] Unexpected MGameStart received with an active GameState");
            }
            else {

                //get the message and create the state
                MGameStart m = (MGameStart) msg;
                state = new GameState(m.yourPlayer, m.getPlayers());

                //copy data logically
                state.mapWidth = m.mapWidth;
                state.mapHeight = m.mapHeight;
                state.grids = m.grids;

                //TODO load the graphics
                loadMinimap();
            }
        }
    }

    @Override
    public void kickedFromServer(Message message) {

    }

    @Override
    public void lostConnectionToServer() {

    }


    /* Deal with Graphics */

    /**
     * Initial function for loading
     */
    private void initGraphics(){

        //load the skin
        skin = new Skin(Gdx.files.internal("skins/sgx/skin/sgx-ui.json"));

        //and the background
        initBackground();

        //and the groups
        stage.addActor(initMinimapGroup());
        stage.addActor(initViewGroup());
        stage.addActor(initFleetGroup());
        stage.addActor(initIndustryGroup());
        stage.addActor(initOptionsGroup());
    }

    /**
     * Background
     */
    private void initBackground(){
        //set the background
        Image image = new Image(new Texture(Gdx.files.internal("images/pixels/navy.png")));
        image.setBounds(0, 0, Main.WIDTH, Main.HEIGHT);
        stage.addActor(image);
    }

    /**
     * Viewport
     */
    private Group initViewGroup(){

        viewGroup = new Group();

        return viewGroup;
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

        return minimapGroup;
    }
    private void loadMinimap(){

        for(Grid grid : state.grids){

            Gdx.app.postRunnable(() -> {
                grid.station.minimapSprite = new Image(new Texture(Gdx.files.internal("images/circles/"
                        + state.players.get(grid.station.owner).color.toString().toLowerCase() + ".png")));

                grid.station.minimapSprite.setPosition(3 + grid.x*250f/1000f - 4.5f, 3 + grid.y*250f/1000f - 4.5f);
                grid.station.minimapSprite.setSize(9, 9);

                grid.station.minimapSprite.addListener(event -> {
                    if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.touchDown){
                        //TODO input handling
                    }
                    return false;
                });

                minimapGroup.addActor(grid.station.minimapSprite);
            });
        }

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
        //initialize the top level group
        industryGroup = new Group();
        industryGroup.setBounds(Main.WIDTH-275, 260, 275, 395);

        //main background
        Image main = new Image(new Texture(Gdx.files.internal("images/pixels/darkpurple.png")));
        main.setSize(industryGroup.getWidth(), industryGroup.getHeight());
        industryGroup.addActor(main);

        return industryGroup;
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
