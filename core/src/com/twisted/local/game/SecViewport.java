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
import com.twisted.util.Config;
import com.twisted.Main;
import com.twisted.util.Paint;
import com.twisted.local.game.cosmetic.Cosmetic;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.mobs.Mobile;

import java.util.*;

public class SecViewport extends Sector {

    //constants
    private static final float LTR = Game.LTR; //logical to rendered
    private static final Color NEUTRAL_COL = Color.GRAY;
    private static final float MAX_ZOOM=1.8f, MIN_ZOOM=0.6f, PAN_SPD=6;
    private static final float SQRT_2 = (float) Math.sqrt(2);
    private static final float PARALLAX = 0.4f;

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

    //graphics state
    private float offset;
    private Vector2[] gridCamPos;
    private float[] gridCamZoom;

    //cosmetics
    private List<Cosmetic> cosmetics;

    //selected
    private Map<Select, EntPtr> selections;
    private Map<Select, Color> selectionColors;
    private Map<Select, Float> selectionValues;

    //background
    private float[][][] stars; //NumGrids x NumStars x 4 (x,y,si,col)

    //scratch
    private final Color innerBackColorCircle;


    /**
     * Constructor
     */
    SecViewport(Game game, Stage stage){
        this.game = game;
        this.stage = stage;

        this.innerBackColorCircle = new Color();
    }

    @Override
    Group init() {
        parent = super.init();

        //prepare camera
        camera = new OrthographicCamera(stage.getWidth(), stage.getHeight());

        //prepare for input
        cursor = new Vector2(0, 0);

        //prepare graphics storage
        cosmetics = Collections.synchronizedList(new ArrayList<>());
        selections = Collections.synchronizedMap(new HashMap<>());
        selectionColors = Collections.synchronizedMap(new HashMap<>());
        selectionValues = Collections.synchronizedMap(new HashMap<>());

        return parent;
    }
    @Override
    void load() {
        //prepare cameras
        gridCamPos = new Vector2[state.grids.length];
        gridCamZoom = new float[state.grids.length];
        for(int i=0; i<state.grids.length; i++) {
            gridCamPos[i] = new Vector2();
            gridCamZoom[i] = 1;
        }

        //prepare stars
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
    void render(float delta, ShapeRenderer shape, SpriteBatch sprite) {
        //get the grid
        offset += delta;
        Grid g = state.grids[game.getGrid()];

        //camera prep
        camera.position.x = gridCamPos[g.id].x;
        camera.position.y = gridCamPos[g.id].y;
        camera.zoom = Config.isZoomGridSpecific() ? gridCamZoom[g.id] : gridCamZoom[0];

        //graphics prep
        camera.update();
        sprite.setProjectionMatrix(camera.combined);
        shape.setProjectionMatrix(camera.combined);

        //draw rest of viewport
        renderBackground(delta, shape, g);
        renderStations(delta, shape, g);
        renderCosmetics(delta, shape, g);
        renderMobiles(delta, shape, g);
        renderShips(delta, shape, g);
        renderSelections(delta, shape, g);
    }
    @Override
    void dispose() {}


    /* Rendering */

    private void renderBackground(float delta, ShapeRenderer shape, Grid g){
        shape.begin(ShapeRenderer.ShapeType.Filled);

        //deep space background
        shape.setColor(Paint.DEEP_SPACE.c);
        shape.rect(camera.position.x-camera.zoom*stage.getWidth()/2f,
                camera.position.y-camera.zoom*stage.getHeight()/2f,
                camera.zoom*stage.getWidth(), camera.zoom*stage.getHeight());

        //draw circle of shallow space
        shape.setColor(Paint.SPACE.c);
        shape.circle(0, 0, g.radius*LTR);

        //draw fog of war circle
        if(g.fogTimer < 3){
            innerBackColorCircle.set(Paint.DEEP_SPACE.c).mul(1 + g.fogTimer/3);
            shape.setColor(innerBackColorCircle);
            shape.circle(0, 0, g.radius*LTR-4);
        }

        //draw stars
        for(float[] s : stars[g.id]){
            shape.setColor(s[3], s[3], s[3], 1f);

            //0.4 is the parallax effect
            float rawX = s[0]+camera.position.x - camera.position.x*PARALLAX;
            float rawY = s[1]+camera.position.y - camera.position.y*PARALLAX;

            shape.circle(rawX + MAX_ZOOM*Main.WIDTH * (float)Math.round((camera.position.x-rawX)/(MAX_ZOOM*Main.WIDTH)),
                    rawY + MAX_ZOOM*Main.HEIGHT * (float)Math.round((camera.position.y-rawY)/(MAX_ZOOM*Main.HEIGHT)),
                    s[2] * (float)Math.sqrt(camera.zoom));
        }

        shape.end();
    }

    private void renderStations(float delta, ShapeRenderer shape, Grid g){
        if(g.station.isShowingThroughFog() || g.fogTimer > 0){
            shape.begin(ShapeRenderer.ShapeType.Line);

            if(g.station.owner == 0){
                shape.setColor(NEUTRAL_COL);
            }
            else {
                shape.setColor(state.players.get(g.station.owner).getCollect().base.c);
            }
            Polygon stationDrawable = new Polygon(g.station.polygon.getVertices());
            stationDrawable.scale(LTR);
            shape.polygon(stationDrawable.getTransformedVertices());

            shape.end();
        }
    }

    private void renderCosmetics(float delta, ShapeRenderer shape, Grid g){
        //draw the cosmetics
        Set<Cosmetic> cosmeticToRemove = new HashSet<>();
        for(Cosmetic c : cosmetics){
            if(!c.tick(delta)) cosmeticToRemove.add(c);
            else if(c.gridId == g.id && (g.fogTimer > 0 || c.showsThroughFog(state))) c.draw(shape, g);
        }
        for(Cosmetic c : cosmeticToRemove){
            cosmetics.remove(c);
        }
    }

    private void renderMobiles(float delta, ShapeRenderer shape, Grid g){
        if(g.fogTimer > 0){
            for(Mobile m : g.mobiles.values()){
                m.draw(shape);
            }
        }
    }

    private void renderShips(float delta, ShapeRenderer shape, Grid g){
        shape.begin(ShapeRenderer.ShapeType.Line);

        Polygon shipDrawable;
        for(Ship s : g.ships.values()){
            if(g.fogTimer > 0 || s.isShowingThroughFog()){
                if(s.owner == 0){
                    shape.setColor(NEUTRAL_COL);
                }
                else {
                    shape.setColor(state.players.get(s.owner).getCollect().base.c);
                }

                //draw the ship
                shipDrawable = new Polygon(s.entityModel().getVertices());
                shipDrawable.scale(LTR);
                shipDrawable.translate(s.pos.x*LTR, s.pos.y*LTR);
                shipDrawable.rotate((float) (s.rot*180/Math.PI)-90 );
                shape.polygon(shipDrawable.getTransformedVertices());
            }
        }

        shape.end();
    }

    private void renderSelections(float delta, ShapeRenderer shape, Grid g){
        shape.begin(ShapeRenderer.ShapeType.Line);

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

        //draw the aoe circle
        if(selections.get(Select.AOE_MOUSE_CIRCLE_1) != null){
            Entity sel;

            if(game.currentFleetHover() == null){
                sel = selections.get(Select.AOE_MOUSE_CIRCLE_1).retrieveFromGrid(g);

                if(sel != null && sel.grid == game.getGrid()){
                    Vector2 center = new Vector2(
                            camera.zoom * (cursor.x-stage.getWidth()/2f+camera.position.x)/100f,
                            camera.zoom * (cursor.y-stage.getHeight()/2f+camera.position.y)/100f);
                    float radius = camera.zoom * selectionValues.get(Select.AOE_MOUSE_CIRCLE_1);
                    float off = 3 * (offset%360);

                    shape.setColor(selectionColors.get(Select.AOE_MOUSE_CIRCLE_1));
                    for(float i=0; i<360; i += 360f/(radius*60*camera.zoom)){
                        shape.circle(LTR * (center.x + radius*(float)Math.cos((off+i)*Math.PI/180)),
                                LTR * (center.y + radius*(float)Math.sin((off+i)*Math.PI/180)),
                                1);
                    }
                }
            }
            else {
                sel = game.currentFleetHover();

                if(sel.grid == game.getGrid()){
                    float radius = camera.zoom * selectionValues.get(Select.AOE_MOUSE_CIRCLE_1);
                    float off = 3 * (offset%360);

                    shape.setColor(selectionColors.get(Select.AOE_MOUSE_CIRCLE_1));
                    for(float i=0; i<360; i += 360f/(radius*60*camera.zoom)){
                        shape.circle(LTR * (sel.pos.x + radius*(float)Math.cos((off+i)*Math.PI/180)),
                                LTR * (sel.pos.y + radius*(float)Math.sin((off+i)*Math.PI/180)),
                                1);
                    }
                }
            }
        }
        if(selections.get(Select.AOE_MOUSE_CIRCLE_2) != null){
            Entity sel;

            if(game.currentFleetHover() == null){
                sel = selections.get(Select.AOE_MOUSE_CIRCLE_2).retrieveFromGrid(g);

                if(sel != null && sel.grid == game.getGrid()){
                    Vector2 center = new Vector2(
                            camera.zoom * (cursor.x-stage.getWidth()/2f+camera.position.x)/100f,
                            camera.zoom * (cursor.y-stage.getHeight()/2f+camera.position.y)/100f);
                    float radius = camera.zoom * selectionValues.get(Select.AOE_MOUSE_CIRCLE_2);
                    float off = 3 * (offset%360);

                    shape.setColor(selectionColors.get(Select.AOE_MOUSE_CIRCLE_2));
                    for(float i=0; i<360; i += 360f/(radius*60*camera.zoom)){
                        shape.circle(LTR * (center.x + radius*(float)Math.cos((off+i)*Math.PI/180)),
                                LTR * (center.y + radius*(float)Math.sin((off+i)*Math.PI/180)),
                                1);
                    }
                }
            }
            else {
                sel = game.currentFleetHover();

                if(sel.grid == game.getGrid()){
                    float radius = camera.zoom * selectionValues.get(Select.AOE_MOUSE_CIRCLE_2);
                    float off = 3 * (offset%360);

                    shape.setColor(selectionColors.get(Select.AOE_MOUSE_CIRCLE_2));
                    for(float i=0; i<360; i += 360f/(radius*60*camera.zoom)){
                        shape.circle(LTR * (sel.pos.x + radius*(float)Math.cos((off+i)*Math.PI/180)),
                                LTR * (sel.pos.y + radius*(float)Math.sin((off+i)*Math.PI/180)),
                                1);
                    }
                }
            }
        }

        shape.end();
    }


    /* Event Methods */

    void switchFocusedGrid(int oldGrid, int newGrid){
        if(oldGrid == newGrid){
            gridCamPos[newGrid].set(0, 0);
        }
    }

    /**
     * Pan the camera in 1-2 directions. Camera is clamped based on grid radius.
     */
    void panCamera(Direction horDir, Direction verDir){
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
        Grid g = state.grids[game.getGrid()];
        gridCamPos[g.id].add(amtX, amtY);
        clampCamera(g.id, g.radius);
    }

    /**
     * Move camera to the logical position (x, y).
     */
    void moveCameraTo(float x, float y){
        gridCamPos[game.getGrid()].set(LTR*x, LTR*y);
        clampCamera(game.getGrid(), state.grids[game.getGrid()].radius);
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
        if(g.station.polygon.contains(adjX, adjY) &&
                (g.fogTimer > 0 || g.station.isShowingThroughFog())){
            ptr = new EntPtr(Entity.Type.Station, g.id, g.id, false);
        }
        for(Ship s : g.ships.values()){
            if(g.fogTimer > 0 || s.isShowingThroughFog()){
                //create slightly larger polygon
                Polygon sPoly = new Polygon(s.polygon.getVertices());
                sPoly.translate(s.pos.x, s.pos.y);
                //check if it contains
                if(sPoly.contains(adjX, adjY)){
                    ptr = new EntPtr(Entity.Type.Ship, s.id, g.id, false);
                }
            }
        }

        //otherwise, check if a ship was near to being clicked on
        if(ptr == null){
            for(Ship s : g.ships.values()){
                if(g.fogTimer > 0 || s.isShowingThroughFog()) {
                    if (s.pos.dst(adjX, adjY) < s.model.getPaddedLogicalRadius()) {
                        ptr = new EntPtr(Entity.Type.Ship, s.id, g.id, false);
                    }
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
        int idx = Config.isZoomGridSpecific() ? game.getGrid(): 0;

        gridCamZoom[idx] += 0.02 * amount;
        if(gridCamZoom[idx] < MIN_ZOOM) gridCamZoom[idx] = MIN_ZOOM;
        else if(gridCamZoom[idx] > MAX_ZOOM) gridCamZoom[idx] = MAX_ZOOM;
    }


    /* Updating Methods */

    /**
     * Updates the grids of any selections that match this entity.
     */
    void updateSelectionGridsAsNeeded(Entity entity, int newGrid){
        for(EntPtr ptr : selections.values()){
            if(ptr != null && ptr.matches(entity)) ptr.grid = newGrid;
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

        if(horDir != null || vertDir != null) panCamera(horDir, vertDir);
    }


    /* Cosmetic Methods */

    void addCosmetic(Cosmetic cosmetic){
        cosmetics.add(cosmetic);
    }


    /* Utility Methods */

    private void clampCamera(int gridId, float gridRadius){
        if(gridCamPos[gridId].len() > gridRadius*LTR){
            gridCamPos[gridId].nor().scl(gridRadius*LTR);
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
        /**
         * A circle of constant radius around the mouse.
         */
        AOE_MOUSE_CIRCLE_1,
        AOE_MOUSE_CIRCLE_2,
    }

}
