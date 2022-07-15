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
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.twisted.local.game.state.PlayColor;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.mobs.Mobile;

import java.util.HashMap;

class SecViewport extends Sector{

    //constants
    public static final float LTR = Game.LTR; //logical to rendered
    private static final Color SPACE = new Color(0x020036ff);

    //high level input
    private Vector2 cursor;

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

    //selected
    private HashMap<Select, EntPtr> selections;


    /**
     * Constructor
     */
    SecViewport(Game game, Stage stage){
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

        selections = new HashMap<>();

        return null;
    }

    @Override
    void load() {

        //load the background
        state.viewportBackground = new Texture(Gdx.files.internal("images/pixels/navy.png"));

        //position listener
        cursor = new Vector2(0, 0);
        stage.addListener(event -> {
            if(event instanceof InputEvent){
                InputEvent inp = (InputEvent) event;

                cursor.set(inp.getStageX(), inp.getStageY());
            }
            return false;
        });

        //click listener
        stage.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(event.isHandled()) return;
                clickHandler(event.getButton(), event, x, y);
            }
        });
        stage.addListener(new ClickListener(Input.Buttons.RIGHT){
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

        //draw the mobiles
        Polygon mobDrawable;
        shape.setColor(Color.LIGHT_GRAY); //TODO color based on the particular mobile
        for(Mobile m : g.mobiles.values()){
            mobDrawable = new Polygon(m.getVertices());
            mobDrawable.scale(LTR);
            mobDrawable.translate(m.pos.x*LTR, m.pos.y*LTR);
            mobDrawable.rotate((float) (m.rot*180/Math.PI)-90);
            shape.polygon(mobDrawable.getTransformedVertices());
        }

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
            shipDrawable.rotate((float) (s.rot*180/Math.PI)-90 );
            shape.polygon(shipDrawable.getTransformedVertices());
        }

        //draw the basic selection circle
        if(selections.get(Select.BASIC) != null){
            EntPtr sel = selections.get(Select.BASIC);

            if(sel.type == Entity.Type.Ship && sel.grid == game.getGrid()){
                Ship s = state.grids[sel.grid].ships.get(sel.id);

                shape.setColor(Color.LIGHT_GRAY);
                shape.circle(s.pos.x*LTR, s.pos.y*LTR, s.getPaddedLogicalRadius()*LTR);
            }
            else{
                // TODO add case for station
            }
        }

        //draw the orbit selection circle
        if(selections.get(Select.CIRCLE) != null){
            Entity ent = selections.get(Select.CIRCLE).retrieveFromGrid(g);

            if(ent != null){
                float orbCircleRad = new Vector2(
                        (cursor.x-stage.getWidth()/2f+camPos.x)/100f,
                        (cursor.y-stage.getHeight()/2f+camPos.y)/100f)
                        .dst(ent.pos);

                shape.setColor(Color.LIGHT_GRAY);
                for(float i=0; i<360; i+= 360f/(orbCircleRad*80)){
                    shape.circle(LTR*(ent.pos.x + orbCircleRad*(float)Math.cos(i*Math.PI/180)),
                            LTR*(ent.pos.y + orbCircleRad*(float)Math.sin(i*Math.PI/180)),
                            1);
                }
            }
        }

        //draw the move selection line
        if(selections.get(Select.LINE) != null){
            Entity ent = selections.get(Select.LINE).retrieveFromGrid(g);

            if(ent != null){
                Vector2 end = new Vector2(
                        (cursor.x-stage.getWidth()/2f+camPos.x)/LTR,
                        (cursor.y-stage.getHeight()/2f+camPos.y)/LTR);
                float length = end.dst(ent.pos);
                float angle = (float) Math.atan2(end.y-ent.pos.y, end.x-ent.pos.x);

                shape.setColor(Color.LIGHT_GRAY);
                for(float i=0; i<length; i+=0.1f){
                    shape.circle(LTR*(ent.pos.x + i*(float)Math.cos(angle)),
                            LTR*(ent.pos.y + i*(float)Math.sin(angle)),
                            1);
                }
            }
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
        float adjX = (x-stage.getWidth()/2f+camPos.x)/LTR;
        float adjY = (y-stage.getHeight()/2f+camPos.y)/LTR;

        //prep the variables that tell what is clicked on
        Entity.Type type = null;
        int shipId = 0;

        //figure out what was clicked on (mobiles ignored)
        if(g.station.polygon.contains(adjX, adjY)){
            type = Entity.Type.Station;
        }
        for(Ship s : g.ships.values()){
            if(s.polygon.contains(adjX, adjY)){
                type = Entity.Type.Ship;
                shipId = s.id;
            }
        }

        //do the correct thing based on the state and what was clicked on
        if(type == Entity.Type.Ship) {
            game.viewportClickEvent(button, new Vector2(x, y), new Vector2(adjX, adjY),
                    Entity.Type.Ship, shipId);
        }
        else if(type == Entity.Type.Station){
            game.viewportClickEvent(button, new Vector2(x, y), new Vector2(adjX, adjY),
                    Entity.Type.Station, game.getGrid());
        }
        else {
            game.viewportClickEvent(button, new Vector2(x, y), new Vector2(adjX, adjY), null, -1);
        }
    }

    /**
     * Updates the entity pointer of a given selection.
     */
    void updateSelection(Select select, boolean toggle, Entity.Type type, int grid, int id){
        if(toggle) selections.put(select, new EntPtr(type, id, grid));
        else selections.remove(select);
    }


    /* Updating Methods */

    /**
     * Updates the grids of any selections that match this entity.
     */
    void updateSelectionGridsAsNeeded(Entity entity, int newGrid){
        for(EntPtr ptr : selections.values()){
            if(ptr.matches(entity)) ptr.grid = newGrid;
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
     * Selection types.
     */
    enum Select {
        BASIC,

        /**
         * Dotted circle around the entity, radius determined by mouse position.
         */
        CIRCLE,

        /**
         * Dotted line from entity to mouse.
         */
        LINE,
    }

}
