package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.twisted.Main;
import com.twisted.Paint;
import com.twisted.local.game.cosmetic.Cosmetic;
import com.twisted.local.game.cosmetic.LaserBeam;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.attach.Laser;
import com.twisted.logic.entities.attach.Weapon;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.mobs.Mobile;

import java.util.*;

public class SecViewport extends Sector {

    //constants
    private static final float LTR = Game.LTR; //logical to rendered
    private static final Color NEUTRAL_COL = Color.GRAY;
    private static final float MAX_ZOOM=2, MIN_ZOOM=0.5f, PAN_SPD=6;
    private static final float SQRT_2 = (float) Math.sqrt(2);

    //high level input
    private Vector2 cursor;
    private Group parent;
    public Group getParent(){
        return parent;
    }

    //reference variables
    private final Game game;

    //graphics utilities
    private final Stage stage;
    OrthographicCamera camera;
    private SpriteBatch sprite;
    private ShapeRenderer shape;

    //graphics state
    private float offset;

    //cosmetics
    private ArrayList<Cosmetic> cosmetics;

    //selected
    private Map<Select, EntPtr> selections;
    private Map<Select, Color> selectionColors;
    private Map<Select, Float> selectionValues;

    //background
    private float[][][] stars; //NumGrids x NumStars x 4 (x,y,si,col)


    /**
     * Constructor
     */
    SecViewport(Game game, Stage stage){
        this.game = game;
        this.stage = stage;
    }

    @Override
    Group init() {
        parent = super.init();

        //prepare camera
        camera = new OrthographicCamera(stage.getWidth(), stage.getHeight());

        //prepare drawing objects
        sprite = new SpriteBatch();
        shape = new ShapeRenderer();

        //prepare for input
        cursor = new Vector2(0, 0);

        //prepare graphics storage
        cosmetics = new ArrayList<>();
        selections = Collections.synchronizedMap(new HashMap<>());
        selectionColors = Collections.synchronizedMap(new HashMap<>());
        selectionValues = Collections.synchronizedMap(new HashMap<>());

        return parent;
    }
    @Override
    void load() {
        stars = new float[state.grids.length][200][4];
        for(float[][] gr : stars){
            for(float[] arr : gr){
                arr[0] = (float) Math.random()*MAX_ZOOM*Main.WIDTH - Main.WIDTH; //x
                arr[1] = (float) Math.random()*MAX_ZOOM*Main.HEIGHT - Main.HEIGHT; //y
                arr[2] = (float) Math.random() + 1; //size
                arr[3] = (float) Math.random()*0.3f + 0.1f; //color
            }
        }

        //position listener
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
                if(!event.isHandled()) clickHandler(event.getButton(), event, x, y);
            }
        });
        stage.addListener(new ClickListener(Input.Buttons.RIGHT){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(!event.isHandled()) clickHandler(event.getButton(), event, x, y);
            }
        });
        //scroll listener
        parent.addListener(new InputListener(){
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                viewportScroll(amountY);
                return true;
            }
        });
    }
    @Override
    void render(float delta) {
        //must be at the beginning
        camera.update();
        sprite.setProjectionMatrix(camera.combined);
        shape.setProjectionMatrix(camera.combined);

        //graphics prep
        offset += delta;
        Grid g = state.grids[game.getGrid()];

        //draw background
        shape.begin(ShapeRenderer.ShapeType.Filled);
        renderBackground(delta, g);
        shape.end();

        //draw rest of viewport
        shape.begin(ShapeRenderer.ShapeType.Line);
        renderStations(delta, g);
        renderCosmetics(delta, g);
        renderMobiles(delta, g);
        renderShips(delta, g);
        renderSelections(delta, g);
        shape.end();
    }
    @Override
    void dispose() {
        sprite.dispose();
        shape.dispose();
    }


    /* Rendering */

    /**
     * Expects ShapeType.Filled
     */
    private void renderBackground(float delta, Grid g){
        shape.setColor(Paint.SPACE.col);
        shape.rect(camera.position.x-camera.zoom*stage.getWidth()/2f,
                camera.position.y-camera.zoom*stage.getHeight()/2f,
                camera.zoom*stage.getWidth(), camera.zoom*stage.getHeight());

        for(float[] s : stars[g.id]){
            shape.setColor(s[3], s[3], s[3], 1f);

            float rawX = s[0]+camera.position.x - camera.position.x*0.4f;
            float rawY = s[1]+camera.position.y - camera.position.y*0.4f;
            shape.circle(rawX + MAX_ZOOM*Main.WIDTH * (float)Math.round((camera.position.x-rawX)/(MAX_ZOOM*Main.WIDTH)),
                    rawY + MAX_ZOOM*Main.HEIGHT * (float)Math.round((camera.position.y-rawY)/(MAX_ZOOM*Main.HEIGHT)),
                    s[2] * (float)Math.sqrt(camera.zoom));
        }
    }

    private void renderStations(float delta, Grid g){
        if(g.station.owner == 0){
            shape.setColor(NEUTRAL_COL);
        }
        else {
            shape.setColor(state.players.get(g.station.owner).getPaint().col);
        }
        Polygon stationDrawable = new Polygon(g.station.polygon.getVertices());
        stationDrawable.scale(LTR);
        shape.polygon(stationDrawable.getTransformedVertices());
    }

    private void renderCosmetics(float delta, Grid g){
        //check for new cosmetics
        for(Ship s : g.ships.values()){
            for(Weapon w : s.weapons){
                //check for laser beams
                if(w instanceof Laser && w.isActive() && !((Laser) w).cosmeticBeamExists){
                    LaserBeam c = new LaserBeam(g.id, (Laser) w);
                    cosmetics.add(c);
                }
            }
        }

        //draw the cosmetics
        Set<Cosmetic> cosmeticToRemove = new HashSet<>();
        for(Cosmetic c : cosmetics){
            if(!c.tick(delta)) cosmeticToRemove.add(c);
            else if(c.gridId == g.id) c.draw(shape, g);
        }
        for(Cosmetic c : cosmeticToRemove){
            cosmetics.remove(c);
        }
    }

    private void renderMobiles(float delta, Grid g){
        Polygon mobDrawable;
        shape.setColor(Color.LIGHT_GRAY); //TODO color based on the particular mobile
        for(Mobile m : g.mobiles.values()){
            mobDrawable = new Polygon(m.getVertices());
            mobDrawable.scale(LTR);
            mobDrawable.translate(m.pos.x*LTR, m.pos.y*LTR);
            mobDrawable.rotate((float) (m.rot*180/Math.PI)-90);
            shape.polygon(mobDrawable.getTransformedVertices());
        }
    }

    private void renderShips(float delta, Grid g){
        Polygon shipDrawable;
        for(Ship s : g.ships.values()){
            if(s.owner == 0){
                shape.setColor(NEUTRAL_COL);
            }
            else {
                shape.setColor(state.players.get(s.owner).getPaint().col);
            }

            //draw the ship
            shipDrawable = new Polygon(s.entityModel().getVertices());
            shipDrawable.scale(LTR);
            shipDrawable.translate(s.pos.x*LTR, s.pos.y*LTR);
            shipDrawable.rotate((float) (s.rot*180/Math.PI)-90 );
            shape.polygon(shipDrawable.getTransformedVertices());
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
                            s.entityModel().getPaddedLogicalRadius()*LTR);
                }
            }
        }

        //set range selection circle
        if(selections.get(Select.CIRCLE_RANGE_IND_ROT) != null){
            Entity sel = selections.get(Select.CIRCLE_RANGE_IND_ROT).retrieveFromGrid(g);

            if(sel != null && sel.grid == game.getGrid()) {
                float radius = selectionValues.get(Select.CIRCLE_RANGE_IND_ROT);
                float off = 3 * (offset%360);

                if(radius > 0){
                    shape.setColor(selectionColors.get(Select.CIRCLE_RANGE_IND_ROT));
                    for (float i=0; i<360; i+=2*360f/(radius*80f)) {
                        shape.circle(LTR*(sel.pos.x + radius*(float)Math.cos((off+i) * Math.PI/180)),
                                LTR*(sel.pos.y + radius*(float)Math.sin((off+i) * Math.PI/180)),
                                1);
                    }
                }
            }
        }

        //draw the orbit selection circle
        if(selections.get(Select.BASE_MOUSE_CIRCLE) != null){
            Entity sel = selections.get(Select.BASE_MOUSE_CIRCLE).retrieveFromGrid(g);

            if(sel != null &&  sel.grid == game.getGrid()){
                float orbCircleRad = new Vector2(
                        (cursor.x-stage.getWidth()/2f+camera.position.x)/100f,
                        (cursor.y-stage.getHeight()/2f+camera.position.y)/100f)
                        .dst(sel.pos) * camera.zoom;

                shape.setColor(selectionColors.get(Select.BASE_MOUSE_CIRCLE));
                for(float i=0; i<360; i+= 360f/(orbCircleRad*80*camera.zoom)){
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
                        (camera.zoom*cursor.x-camera.zoom*stage.getWidth()/2f+camera.position.x)/LTR,
                        (camera.zoom*cursor.y-camera.zoom*stage.getHeight()/2f+camera.position.y)/LTR);
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
        camera.translate(-camera.position.x, -camera.position.y);
    }

    /**
     * Pan the camera in 1-2 directions.
     */
    void moveCamera(Direction horDir, Direction verDir){
        float amtX = PAN_SPD * camera.zoom;
        float amtY = PAN_SPD * camera.zoom;

        //move along one axis
        if(horDir != null && verDir == null){
            if(horDir == Direction.LEFT) amtX *= -1;
            amtY = 0;
        }
        else if(horDir == null && verDir != null){
            if(verDir == Direction.DOWN) amtY *= -1;
            amtX = 0;
        }
        //move along two axes
        else if(horDir != null){
            amtX = amtX/SQRT_2;
            amtY = amtY/SQRT_2;

            if(horDir == Direction.LEFT) amtX *= -1;
            if(verDir == Direction.DOWN) amtY *= -1;
        }
        //don't move
        else {
            return;
        }

        //move the camera
        camera.translate(amtX, amtY);
    }

    /**
     * Move camera to the logical position (x, y).
     */
    void moveCameraTo(float x, float y){
        camera.translate(LTR*x - camera.position.x, LTR*y - camera.position.y);
    }

    /**
     * Handles clicks on the stage. Necessary to be able to easily accept clicks from multiple
     * buttons.
     */
    private void clickHandler(int button, InputEvent event, float x, float y){
        //keyboard input
        game.keyboardFocus(parent);

        //get the current grid and convert coords
        Grid g = state.grids[game.getGrid()];
        float adjX = (camera.zoom*(x-stage.getWidth()/2f) + camera.position.x)/LTR;
        float adjY = (camera.zoom*(y-stage.getHeight()/2f) + camera.position.y)/LTR;

        //prep the variables that tell what is clicked on
        EntPtr ptr = null;

        //check if something was directly clicked on
        if(g.station.polygon.contains(adjX, adjY)){
            ptr = new EntPtr(Entity.Type.Station, g.id, g.id, false);
        }
        for(Ship s : g.ships.values()){
            //create slightly larger polygon
            Polygon sPoly = new Polygon(s.polygon.getVertices());
            sPoly.translate(s.pos.x, s.pos.y);
            //check if it contains
            if(sPoly.contains(adjX, adjY)){
                ptr = new EntPtr(Entity.Type.Ship, s.id, g.id, false);
            }
        }

        //otherwise, check if a ship was near to being clicked on
        if(ptr == null){
            for(Ship s : g.ships.values()){
                if(s.pos.dst(adjX, adjY) < s.model.getPaddedLogicalRadius()){
                    ptr = new EntPtr(Entity.Type.Ship, s.id, g.id, false);
                }
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

    /**
     * Scrolls the camera in or out by amount.
     */
    private void viewportScroll(float amount){
        camera.zoom += 0.02 * amount;

        if(camera.zoom < MIN_ZOOM) camera.zoom = 0.5f;
        else if(camera.zoom > MAX_ZOOM) camera.zoom = 2f;
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


    /* User Input */

    void continuousKeyboard(){
        Direction horDir=null, vertDir=null;

        //move the camera around
        if(Gdx.input.isKeyPressed(Input.Keys.D) && !Gdx.input.isKeyPressed(Input.Keys.A)) {
            horDir = Direction.RIGHT;
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.A)) {
            horDir = Direction.LEFT;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.W) && !Gdx.input.isKeyPressed(Input.Keys.S)) {
            vertDir = Direction.UP;
        }
        else if(Gdx.input.isKeyPressed(Input.Keys.S)) {
            vertDir = Direction.DOWN;
        }

        if(horDir != null || vertDir != null) moveCamera(horDir, vertDir);
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
