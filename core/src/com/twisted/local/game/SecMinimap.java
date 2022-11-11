package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.twisted.util.Asset;
import com.twisted.Main;
import com.twisted.util.Paint;
import com.twisted.local.lib.Ribbon;
import com.twisted.local.lib.TogImgButton;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.entities.station.Station;

import java.util.HashMap;

class SecMinimap extends Sector {

    //constants
    private final static Rectangle CON_RECT = new Rectangle(-125, -125, 250, 250); //the content rectangle

    //reference variables
    private final Game game;

    //tree
    private Group parent, systemGroup, clusterGroup;
    private Image activeSquare;
    private TogImgButton clusterIcon, systemIcon;
    private final HashMap<Integer, Image> stationSprites;

    //rendering
    private OrthographicCamera camera;

    //state
    private float sysScale; //conversion logical to this visual (not rendered to this visual)


    /**
     * Constructor
     */
    SecMinimap(Game game){
        this.game = game;

        stationSprites = new HashMap<>();

        this.sysScale = 8;
    }


    /* Standard Methods */

    @Override
    Group init(){
        parent = super.init();
        parent.setBounds(Main.WIDTH-256, 0, 256, 256);

        //rendering prep
        camera = new OrthographicCamera(Main.WIDTH, Main.HEIGHT);
        camera.translate(-(Main.WIDTH-parent.getWidth())/2f, (Main.HEIGHT-parent.getHeight())/2f);

        //create children groups
        parent.addActor(initDecorGroup());

        systemGroup = initSystemGroup();
        parent.addActor(systemGroup);

        clusterGroup = initClusterGroup();
        parent.addActor(clusterGroup);

        return parent;
    }
    @Override
    void load(){
        //load the large group
        for(Grid g : state.grids){
            //load the minimap icon
            Image image;

            if(state.players.get(g.station.owner) == null){
                image = new Image(Asset.retrieve(Asset.Circle.CIRCLE_GRAY));
            }
            else {
                image = new Image(Asset.retrieve(state.players.get(g.station.owner).getMinimapShapeAsset()));
            }
            stationSprites.put(g.station.getId(), image);

            //position is (indent + scaled positioning - half the width)
            image.setPosition(3 + g.loc.x*250f/1000f - 6, 3 + g.loc.y*250f/1000f - 6);
            image.setSize(12, 12);

            //load the minimap label
            Label label = new Label(g.station.getFullName(), Asset.labelStyle(Asset.Avenir.MEDIUM_14));
            label.setColor(Color.GRAY);
            label.setVisible(false);
            if(image.getX() < 3+label.getWidth()/2f){
                label.setPosition(
                        (g.loc.x*250f/1000f-label.getWidth()/2f) + (3+label.getWidth()/2f) - (image.getX()),
                        g.loc.y*250f/1000f + 8);
            }
            else if(image.getX() + label.getWidth()/2f > 248){
                label.setPosition(
                        (g.loc.x*250f/1000f-label.getWidth()/2f) - (image.getX()+label.getWidth()/2f) + (248),
                        g.loc.y*250f/1000f + 8);
            }
            else {
                label.setPosition((g.loc.x*250f/1000f-label.getWidth()/2f), g.loc.y*250f/1000f + 6);
            }

            //add to the minimap group
            clusterGroup.addActor(image);
            clusterGroup.addActor(label);

            //listeners
            image.addListener(event -> {
                //entering and exiting
                if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.enter){
                    label.setVisible(true);
                }
                else if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.exit){
                    label.setVisible(false);
                }

                return true;
            });
            image.addListener(new ClickListener(Input.Buttons.LEFT){
                @Override
                public void clicked(InputEvent event, float x, float y){
                    if(event.isHandled()) return;
                    game.minimapClusterClickEvent(Input.Buttons.LEFT, g.id);
                    event.handle();
                }
            });
        }

        //move the active square
        Grid g = state.grids[game.getGrid()];
        activeSquare.setPosition(3 + g.loc.x*250f/1000f - 10, 3 + g.loc.y*250f/1000f - 10);
    }
    @Override
    void render(float delta, ShapeRenderer shape, SpriteBatch sprite){
        //must be at the beginning
        camera.update();
        shape.setProjectionMatrix(camera.combined);

        //draw
        if(systemGroup.isVisible()){
            Grid grid = state.grids[game.getGrid()];

            //background
            renderSystemBground(grid, shape);
            //draw the entities
            renderEntities(grid, shape);
            //draw the viewport's box
            renderViewportBox(shape);
        }
        else if(clusterGroup.isVisible()){
                //draw embedded rectangle
                shape.begin(ShapeRenderer.ShapeType.Filled);

                shape.setColor(Color.BLACK);
                shape.rect(CON_RECT.x, CON_RECT.y, CON_RECT.width, CON_RECT.height);

                for(Ship s : state.inWarp.values()){
                    float radius = (s.model.tier==Ship.Tier.Battleship || s.model.tier==Ship.Tier.Titan) ? 1.6f : 1;

                    shape.setColor(state.findBaseColorForOwner(s.owner));
                    shape.circle(s.warpPos.x*CON_RECT.width/1000f + CON_RECT.x,
                            s.warpPos.y*CON_RECT.height/1000f + CON_RECT.y,
                            radius);
                }

                shape.end();
            }

    }
    @Override
    void dispose(){

    }

    private Group initDecorGroup(){
        Group group = new Group();

        //main window
        Ribbon ribbon = new Ribbon(Asset.retrieve(Asset.Pixel.DARKPURLE), 3);
        ribbon.setSize(parent.getWidth(), parent.getHeight());
        group.addActor(ribbon);

        //side window
        Ribbon sideRibbon = new Ribbon(Asset.retrieve(Asset.Pixel.DARKPURLE), 3);
        sideRibbon.setBounds(-33, 0, 36, 63);
        group.addActor(sideRibbon);
        Image sideEmbedded = new Image(Asset.retrieve(Asset.Pixel.BLACK));
        sideEmbedded.setBounds(-30, 3, 30, 57);
        group.addActor(sideEmbedded);

        //icons in side window
        clusterIcon = new TogImgButton(Asset.retrieve(Asset.UiButton.MINI_CLUSTER_OFF), Asset.retrieve(Asset.UiButton.MINI_CLUSTER_ON));
        clusterIcon.setPosition(-27, 6);
        clusterIcon.updateVisible(false);
        group.addActor(clusterIcon);
        systemIcon = new TogImgButton(Asset.retrieve(Asset.UiButton.MINI_GRID_OFF), Asset.retrieve(Asset.UiButton.MINI_GRID_ON));
        systemIcon.setPosition(-27, 6+27);
        group.addActor(systemIcon);

        //listeners
        clusterIcon.changeClickListener(new ClickListener(Input.Buttons.LEFT){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(event.isHandled()) return;
                clusterIcon.updateVisible(false);
                systemIcon.updateVisible(true);

                systemGroup.setVisible(false);
                clusterGroup.setVisible(true);
                event.handle();
            }
        });
        systemIcon.changeClickListener(new ClickListener(Input.Buttons.LEFT){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(event.isHandled()) return;
                clusterIcon.updateVisible(true);
                systemIcon.updateVisible(false);

                systemGroup.setVisible(true);
                clusterGroup.setVisible(false);
                event.handle();
            }
        });

        return group;
    }
    private Group initClusterGroup(){
        Group group = new Group();
        parent.addActor(group);

        activeSquare = new Image(Asset.retrieve(Asset.UiBasic.WHITE_SQUARE_1));
        activeSquare.setPosition(parent.getWidth()/2, parent.getHeight()/2);
        activeSquare.setSize(20, 20);
        group.addActor(activeSquare);

        return group;
    }
    private Group initSystemGroup(){
        Group group = new Group();
        group.setVisible(false);

        //main listening actor
        Actor actor = new Actor();
        actor.setBounds(3, 3, 250, 250);
        group.addActor(actor);
        //click listener
        actor.addListener(new ClickListener(Input.Buttons.LEFT){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(event.isHandled()) return;
                game.minimapSystemMouseDownEvent(Input.Buttons.LEFT,
                        (x-125)/ sysScale, (y-125)/ sysScale);
                event.handle();
            }
        });
        //general listener
        actor.addListener(new InputListener(){
            //dragging
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
                if(button == Input.Buttons.LEFT){
                    game.minimapSystemMouseDownEvent(Input.Buttons.LEFT,
                            (x-125)/ sysScale, (y-125)/ sysScale);
                    return true;
                }
                else {
                    return false;
                }
            }
            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer){
                if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
                    game.minimapSystemMouseDownEvent(Input.Buttons.LEFT,
                            (x-125)/ sysScale, (y-125)/ sysScale);
                }
            }
        });

        return group;
    }

    private void renderSystemBground(Grid grid, ShapeRenderer shape){
        //draw embedded rectangle
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(Color.BLACK);
        shape.rect(CON_RECT.x, CON_RECT.y, CON_RECT.width, CON_RECT.height);

        //draw the system circle
        if(systemGroup.isVisible()){
            shape.setColor(Paint.SPACE.c);
            shape.circle(0, 0, 125);

            if(grid.fogTimer == 0){
                shape.setColor(Paint.DEEP_SPACE.c);
                shape.circle(0, 0, 123);
            }
        }

        shape.end();
    }
    private void renderEntities(Grid grid, ShapeRenderer shape){
        Vector2 pos = new Vector2();

        //draw the station
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(state.findBaseColorForOwner(grid.station.owner));
        Polygon stationDrawable = new Polygon(grid.station.polygon.getVertices());
        stationDrawable.scale(sysScale);
        shape.polygon(stationDrawable.getTransformedVertices());
        shape.end();

        //draw the ships
        shape.begin(ShapeRenderer.ShapeType.Filled);
        for(Ship s : grid.ships.values()){
            //fog check
            if(grid.fogTimer > 0 || s.isShowingThroughFog()){
                calcSysPos(s.pos, pos);
                //position check
                if(!(Math.abs(s.pos.x) > parent.getWidth()/(2*sysScale)
                        || Math.abs(s.pos.y) > parent.getWidth()/(2*sysScale))){
                    float radius;
                    switch(s.model.tier){
                        case Titan:
                            radius = 2f;
                            break;
                        case Barge:
                        case Battleship:
                            radius = 1.4f;
                            break;
                        case Cruiser:
                            radius = 1.1f;
                            break;
                        case Frigate:
                        default:
                            radius = 1;
                    }

                    shape.setColor(state.findBaseColorForOwner(s.owner));
                    shape.circle(pos.x, pos.y, radius);
                }
            }
        }
        shape.end();
    }
    private void renderViewportBox(ShapeRenderer shape){
        //draw the viewport rectangle
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(Color.WHITE);

        //get the camera info
        float[] cInfo = game.findViewportCamInfo();
        //calculate the points
        float x1 = Math.min(Math.max((cInfo[0] - cInfo[2]*Main.WIDTH/2f) * sysScale/Game.LTR, CON_RECT.x-1), CON_RECT.x+CON_RECT.width+1);
        float y1 = Math.min(Math.max((cInfo[1] - cInfo[2]*Main.HEIGHT/2f) * sysScale/Game.LTR, CON_RECT.y-1), CON_RECT.y+CON_RECT.height+1);
        float x2 = Math.min(Math.max((cInfo[0] - cInfo[2]*Main.WIDTH/2f + Main.WIDTH*cInfo[2]) * sysScale /Game.LTR, CON_RECT.x-1), CON_RECT.x+CON_RECT.width+1);
        float y2 = Math.min(Math.max((cInfo[1] - cInfo[2]*Main.HEIGHT/2f + Main.HEIGHT*cInfo[2]) * sysScale /Game.LTR, CON_RECT.y-1), CON_RECT.y+CON_RECT.height+1);
        //draw the lines
        shape.line(x1, y1, x1, y2);
        shape.line(x1, y2, x2, y2);
        shape.line(x2, y2, x2, y1);
        shape.line(x2, y1, x1, y1);

        shape.end();
    }


    /* Event Methods */

    /**
     * This method changes where the focus square on the minimap is.
     */
    void switchFocusedGrid(int newGrid){
        activeSquare.setPosition(3 + state.grids[newGrid].loc.x*250f/1000f - 10,
                3 + state.grids[newGrid].loc.y*250f/1000f - 10);

        sysScale = CON_RECT.width / (2*state.grids[newGrid].radius);
    }

    /**
     * Updates the station image. Does not check if the update is needed.
     */
    void updateStation(Station station){
        TextureRegionDrawable drawable;
        if(state.players.get(station.owner) == null){
            drawable = Asset.retrieve(Asset.Circle.CIRCLE_GRAY);
        }
        else {
            drawable = Asset.retrieve(state.players.get(station.owner).getMinimapShapeAsset());
        }

        stationSprites.get(station.getId()).setDrawable(drawable);
    }


    /* Utility Methods */

    private void calcSysPos(float x, float y, Vector2 output){
        output.set(sysScale*x, sysScale*y);
    }
    private void calcSysPos(Vector2 input, Vector2 output){
        calcSysPos(input.x, input.y, output);
    }
}
