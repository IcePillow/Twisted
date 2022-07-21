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
import com.twisted.local.game.cosmetic.Cosmetic;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.mobs.Mobile;

import java.util.*;

public class SecViewport extends Sector{

    //constants
    private static final float LTR = Game.LTR; //logical to rendered
    private static final Color SPACE = new Color(0x020036ff);
    private static final Color NEUTRAL_COL = Color.GRAY;

    //high level input
    private Vector2 cursor;

    //reference variables
    private final Game game;

    //graphics utilities
    private final Skin skin;
    private final Stage stage;
    OrthographicCamera camera;
    SpriteBatch sprite;
    ShapeRenderer shape;

    //graphics state
    private Vector2 camPos;
    private float offset;

    //cosmetics
    private ArrayList<Cosmetic> cosmetics;

    //selected
    private HashMap<Select, EntPtr> selections;
    private HashMap<Select, Color> selectionColors;
    private HashMap<Select, Float> selectionValues;


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

        cosmetics = new ArrayList<>();

        selections = new HashMap<>();
        selectionColors = new HashMap<>();
        selectionValues = new HashMap<>();

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

        //graphics
        offset += delta;

        //access the grid and start drawing
        Grid g = state.grids[game.getGrid()];
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(SPACE);
        shape.rect(camPos.x-stage.getWidth()/2f, camPos.y-stage.getHeight()/2f, stage.getWidth(), stage.getHeight());
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);

        //draw the cosmetics
        renderCosmetics(delta, g);

        //draw the station
        if(g.station.owner == 0){
            shape.setColor(NEUTRAL_COL);
        }
        else {
            shape.setColor(state.players.get(g.station.owner).getColor());
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
                shape.setColor(NEUTRAL_COL);
            }
            else {
                shape.setColor(state.players.get(s.owner).getColor());
            }

            //draw the ship
            shipDrawable = new Polygon(s.getVertices());
            shipDrawable.scale(LTR);
            shipDrawable.translate(s.pos.x*LTR, s.pos.y*LTR);
            shipDrawable.rotate((float) (s.rot*180/Math.PI)-90 );
            shape.polygon(shipDrawable.getTransformedVertices());
        }

        //draw the selection shapes
        renderSelections(delta, g);

        shape.end();
    }

    @Override
    void dispose() {
        sprite.dispose();
    }


    /* Rendering Utility */

    private void renderCosmetics(float delta, Grid g){
        Set<Cosmetic> cosmeticToRemove = new HashSet<>();
        for(Cosmetic c : cosmetics){
            if(!c.renderShape(delta, shape)) cosmeticToRemove.add(c);
        }
        for(Cosmetic c : cosmeticToRemove){
            cosmetics.remove(c);
        }
    }

    private void renderSelections(float delta, Grid g){

        //draw the basic selection circle
        if(selections.get(Select.BASE_SELECT) != null){
            EntPtr sel = selections.get(Select.BASE_SELECT);

            if(sel.type == Entity.Type.Ship && sel.grid == game.getGrid() && !sel.docked){
                Ship s = state.grids[sel.grid].ships.get(sel.id);

                if(s != null){
                    shape.setColor(selectionColors.get(Select.BASE_SELECT));
                    shape.circle(s.pos.x*LTR, s.pos.y*LTR,
                            s.getPaddedLogicalRadius()*LTR);
                }
            }
            else{
                // TODO add case for station
            }
        }

        //set range selection circle
        if(selections.get(Select.CIRCLE_RANGE_IND_ROT) != null){
            Entity sel = selections.get(Select.CIRCLE_RANGE_IND_ROT).retrieveFromGrid(g);

            if(sel != null && sel.grid == game.getGrid()) {
                float radius = selectionValues.get(Select.CIRCLE_RANGE_IND_ROT);
                float off = 3 * (offset%360);

                shape.setColor(selectionColors.get(Select.CIRCLE_RANGE_IND_ROT));
                for (float i=0; i<360; i+=2*360f/(radius*80f)) {
                    shape.circle(LTR*(sel.pos.x + radius*(float)Math.cos((off+i) * Math.PI/180)),
                            LTR*(sel.pos.y + radius*(float)Math.sin((off+i) * Math.PI/180)),
                            1);
                }
            }
        }

        //draw the orbit selection circle
        if(selections.get(Select.BASE_MOUSE_CIRCLE) != null){
            Entity sel = selections.get(Select.BASE_MOUSE_CIRCLE).retrieveFromGrid(g);

            if(sel != null &&  sel.grid == game.getGrid()){
                float orbCircleRad = new Vector2(
                        (cursor.x-stage.getWidth()/2f+camPos.x)/100f,
                        (cursor.y-stage.getHeight()/2f+camPos.y)/100f)
                        .dst(sel.pos);

                shape.setColor(selectionColors.get(Select.BASE_MOUSE_CIRCLE));
                for(float i=0; i<360; i+= 360f/(orbCircleRad*80)){
                    shape.circle(LTR*(sel.pos.x + orbCircleRad*(float)Math.cos(i*Math.PI/180)),
                            LTR*(sel.pos.y + orbCircleRad*(float)Math.sin(i*Math.PI/180)),
                            1);
                }
            }
        }

        //draw the move selection line
        if(selections.get(Select.BASE_MOUSE_LINE) != null){
            Entity sel = selections.get(Select.BASE_MOUSE_LINE).retrieveFromGrid(g);

            if(sel != null && sel.grid == game.getGrid()){
                Vector2 end = new Vector2(
                        (cursor.x-stage.getWidth()/2f+camPos.x)/LTR,
                        (cursor.y-stage.getHeight()/2f+camPos.y)/LTR);
                float length = end.dst(sel.pos);
                float angle = (float) Math.atan2(end.y-sel.pos.y, end.x-sel.pos.x);

                shape.setColor(selectionColors.get(Select.BASE_MOUSE_LINE));
                for(float i=0; i<length; i+=0.1f){
                    shape.circle(LTR*(sel.pos.x + i*(float)Math.cos(angle)),
                            LTR*(sel.pos.y + i*(float)Math.sin(angle)),
                            1);
                }
            }
        }

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
        EntPtr ptr = null;

        //figure out what was clicked on (mobiles ignored)
        if(g.station.polygon.contains(adjX, adjY)){
            ptr = new EntPtr(Entity.Type.Station, g.id, g.id, false);
        }
        for(Ship s : g.ships.values()){
            //create slightly larger polygon
            Polygon sPoly = new Polygon(s.polygon.getVertices());
            sPoly.translate(s.pos.x, s.pos.y);
            sPoly.scale(1.35f);
            //check if it contains
            if(sPoly.contains(adjX, adjY)){
                ptr = new EntPtr(Entity.Type.Ship, s.id, g.id, false);
            }
        }

        //send the viewport click event
        game.viewportClickEvent(button, new Vector2(x, y), new Vector2(adjX, adjY), ptr);
    }

    /**
     * Updates the entity pointer of a given selection.
     * @param value A value that may be used in the drawing of the selection (such as a circle radius).
     *              May or may not be used for a given select.
     */
    void updateSelection(Select select, boolean toggle, EntPtr ptr, Color color, float value){
        if(toggle) {
            selections.put(select, ptr);
            selectionColors.put(select, color);
            selectionValues.put(select, value);
        }
        else {
            selections.remove(select);
        }
    }

    /**
     * Remove all selections with the provided entity.
     */
    void removeSelectionsOfEntity(EntPtr ptr){
        Set<Select> toBeRemoved = new HashSet<>();

        for(Select s : selections.keySet()){
            if(selections.get(s).matches(ptr)) toBeRemoved.add(s);
        }
        for(Select s : toBeRemoved){
            selections.remove(s);
            selectionColors.remove(s);
            selectionValues.remove(s);
        }
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


    /* Cosmetic Methods */

    void addCosmetic(Cosmetic cosmetic){
        cosmetics.add(cosmetic);
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
    public enum Select {
        /**
         * The basic solid circle surrounding an entity.
         */
        BASE_SELECT,
        /**
         * Dotted circle around the entity, radius determined by mouse position.
         */
        BASE_MOUSE_CIRCLE,
        /**
         * Dotted line from entity to mouse.
         */
        BASE_MOUSE_LINE,

        /**
         * Range indicator that rotates. Set radius.
         */
        CIRCLE_RANGE_IND_ROT,
    }

}
