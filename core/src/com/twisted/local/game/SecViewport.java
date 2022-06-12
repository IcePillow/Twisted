package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.Station;

public class SecViewport extends Sector{

    //constants
    public static final float LTR = Game.LTR; //logical to rendered
    private static final Color GRAY = new Color(0x9d9d9dff);
    private static final Color SPACE = new Color(0x020036ff);


    //reference variables
    private Game game;

    //graphics utilities
    private Skin skin;
    private Stage stage;
    OrthographicCamera camera;
    SpriteBatch sprite;
    ShapeRenderer shape;

    //graphics state
    private Vector2 camPos;


    /**
     * Constructor
     */
    public SecViewport(Game game, Skin skin, Stage stage){
        this.game = game;
        this.skin = skin;
        this.stage = stage;
    }

    @Override
    Group init() {
        camera = new OrthographicCamera(stage.getWidth(), stage.getHeight());
        camPos = new Vector2(0, 0);

        sprite = new SpriteBatch();
        shape = new ShapeRenderer();

        return null;
    }

    @Override
    void load() {

        //load the background
        state.viewportBackground = new Texture(Gdx.files.internal("images/pixels/navy.png"));

        //load in the station graphics
        for(Station.Type type : Station.Type.values()){
            String s1 = type.name().toLowerCase();

            //loop through the possible colors
            for(String s2 : Game.COLOR_FILENAMES){
                Station.viewportSprites.put(s1 + "-" + s2,
                        new Texture(Gdx.files.internal("images/stations/" + s1 + "-" + s2 + ".png")));
            }
        }

        //load in the ship graphics
        for(Ship.Type type : Ship.Type.values()){
            String s1 = type.name().toLowerCase();

            for(String s2 : Game.COLOR_FILENAMES){
                Ship.viewportSprites.put(s1 + "-" + s2,
                        new Texture(Gdx.files.internal("images/ships/" + s1 + "-" + s2 + ".png")));
            }
        }

        //click listeners
        stage.addListener(new ClickListener(Input.Buttons.LEFT){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(!event.isHandled()) clickHandler(Input.Buttons.LEFT, event, x, y);
            }
        });
        stage.addListener(new ClickListener(Input.Buttons.RIGHT){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(!event.isHandled()) clickHandler(Input.Buttons.RIGHT, event, x, y);
            }
        });

    }

    @Override
    void render(float delta) {

        //must be at the beginning
        camera.update();
        sprite.setProjectionMatrix(camera.combined);
        shape.setProjectionMatrix(camera.combined);

        //access the grid and start drawing
        Grid g = state.grids[game.getGrid()];
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(SPACE);
        shape.rect(camPos.x-stage.getWidth()/2f, camPos.y-stage.getHeight()/2f, stage.getWidth(), stage.getHeight());
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);

        //draw the station
        if(g.station.owner == 0){
            shape.setColor(GRAY);
        }
        else {
            shape.setColor(state.players.get(g.station.owner).color.object);
        }
        Polygon stationDrawable = new Polygon(g.station.polygon.getVertices());
        stationDrawable.scale(LTR);
        shape.polygon(stationDrawable.getTransformedVertices());

        //draw the ships
        Polygon shipDrawable;
        for(Ship s : g.ships.values()){
            if(s.owner == 0){
                shape.setColor(GRAY);
            }
            else {
                shape.setColor(state.players.get(s.owner).color.object);
            }

            //draw the ship
            shipDrawable = new Polygon(s.getVertices());
            shipDrawable.scale(LTR);
            shipDrawable.translate(s.pos.x*LTR, s.pos.y*LTR);
            shipDrawable.rotate( (float) (s.rot*180/Math.PI)-90 );
            shape.polygon(shipDrawable.getTransformedVertices());
        }

        shape.end();

    }

    @Override
    void dispose() {
        sprite.dispose();
    }


    /* Event Methods */

    void switchFocusedGrid(){
        //undo all movements of the camera and update the position
        camera.translate(-camPos.x, -camPos.y);
        camPos.x = 0;
        camPos.y = 0;
    }

    /**
     * Move the camera
     */
    void moveCamera(Direction dir){

        if(dir == Direction.RIGHT){
            camera.translate(5, 0);
            camPos.x += 5;
        }
        else if(dir == Direction.LEFT) {
            camera.translate(-5, 0);
            camPos.x -= 5;
        }
        else if(dir == Direction.UP) {
            camera.translate(0, 5);
            camPos.y += 5;
        }
        else if(dir == Direction.DOWN) {
            camera.translate(0, -5);
            camPos.y -= 5;
        }

    }

    /**
     * Handles clicks on the stage. Necessary to be able to easily accept clicks from multiple
     * buttons.
     */
    private void clickHandler(int button, InputEvent event, float x, float y){

        //get the current grid and convert coords
        Grid g = state.grids[game.getGrid()];
        float adjX = (x-stage.getWidth()/2f+camPos.x)/100f;
        float adjY = (y-stage.getHeight()/2f+camPos.y)/100f;

        //prep the variables that tell what is clicked on
        String type = "none"; //none, station, ship
        int shipId = 0;

        //figure out what was clicked on
        if(g.station.polygon.contains(adjX, adjY)){
            type = "station";
        }
        for(Ship s : g.ships.values()){
            if(s.polygon.contains(adjX, adjY)){
                type = "ship";
                shipId = s.id;
            }
        }

        //do the correct thing based on the state and what was clicked on
        if(type.equals("ship")) {
            game.viewportClickEvent(button, new Vector2(x, y), new Vector2(adjX, adjY),
                    ClickType.SHIP, shipId);
        }
        else if(type.equals("station")){
            game.viewportClickEvent(button, new Vector2(x, y), new Vector2(adjX, adjY),
                    ClickType.STATION, game.getGrid());
        }
        else {
            game.viewportClickEvent(button, new Vector2(x, y), new Vector2(adjX, adjY), ClickType.SPACE, -1);
        }

    }


    /* Enums */

    /**
     * Four directions.
     */
    enum Direction {
        UP, RIGHT, DOWN, LEFT
    }

    /**
     *
     */
    enum ClickType {
        //clicked on nothing but the background
        SPACE,
        //clicked on an entity
        SHIP,
        STATION
    }

}
