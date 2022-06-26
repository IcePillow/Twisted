package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
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
import com.twisted.local.game.state.PlayColor;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;

public class SecViewport extends Sector{

    //constants
    public static final float LTR = Game.LTR; //logical to rendered
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

    //selected entity
    Entity.Type selEntType;
    private int selEntGrid;
    int selEntId;


    /**
     * Constructor
     */
    public SecViewport(Game game, Stage stage){
        this.game = game;
        this.stage = stage;
        this.skin = game.skin;
    }

    @Override
    Group init() {
        camera = new OrthographicCamera(stage.getWidth(), stage.getHeight());
        camPos = new Vector2(0, 0);

        sprite = new SpriteBatch();
        shape = new ShapeRenderer();

        selEntType = null;

        return null;
    }

    @Override
    void load() {

        //load the background
        state.viewportBackground = new Texture(Gdx.files.internal("images/pixels/navy.png"));

        //click listener
        stage.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(event.isHandled()) return;
                clickHandler(event.getButton(), event, x, y);
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
            shape.setColor(PlayColor.GRAY.object);
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
                shape.setColor(PlayColor.GRAY.object);
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

        //draw the selection circle
        if(selEntType == Entity.Type.SHIP && selEntGrid == game.getGrid()){
            Ship s = state.grids[selEntGrid].ships.get(selEntId);

            shape.setColor(Color.LIGHT_GRAY);
            shape.circle(s.pos.x*LTR, s.pos.y*LTR, s.getPaddedLogicalRadius()*LTR);
        }
        else{
            // TODO add the other cases
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

    /**
     * Select a given entity.
     */
    void selectedEntity(Entity.Type type, int grid, int id){
        this.selEntType = type;
        this.selEntGrid = grid;
        this.selEntId = id;
    }


    /* Updating Methods */

    void updateSelectedEntity(int grid){
        this.selEntGrid = grid;
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
