package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.twisted.Asset;
import com.twisted.Main;
import com.twisted.local.lib.Ribbon;
import com.twisted.local.lib.TogImgButton;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.entities.station.Station;

import java.util.HashMap;

class SecMinimap extends Sector {

    //constants
    private final static float SYS_SCALE = 10f; //conversion logical to this visual (not rendered to this visual)
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
    private ShapeRenderer shape;

    /**
     * Constructor
     */
    SecMinimap(Game game){
        this.game = game;

        stationSprites = new HashMap<>();
    }


    /* Standard Methods */

    @Override
    Group init(){
        parent = super.init();
        parent.setBounds(Main.WIDTH-256, 0, 256, 256);

        //rendering prep
        camera = new OrthographicCamera(Main.WIDTH, Main.HEIGHT);
        camera.translate(-(Main.WIDTH-parent.getWidth())/2f, (Main.HEIGHT-parent.getHeight())/2f);
        shape = new ShapeRenderer();

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
            image.setPosition(3 + g.pos.x*250f/1000f - 6, 3 + g.pos.y*250f/1000f - 6);
            image.setSize(12, 12);

            //load the minimap label
            Label label = new Label(g.station.getFullName(), Asset.labelStyle(Asset.Avenir.MEDIUM_14));
            label.setColor(Color.GRAY);
            label.setVisible(false);
            if(image.getX() < 3+label.getWidth()/2f){
                label.setPosition(
                        (g.pos.x*250f/1000f-label.getWidth()/2f) + (3+label.getWidth()/2f) - (image.getX()),
                        g.pos.y*250f/1000f + 8);
            }
            else if(image.getX() + label.getWidth()/2f > 248){
                label.setPosition(
                        (g.pos.x*250f/1000f-label.getWidth()/2f) - (image.getX()+label.getWidth()/2f) + (248),
                        g.pos.y*250f/1000f + 8);
            }
            else {
                label.setPosition((g.pos.x*250f/1000f-label.getWidth()/2f), g.pos.y*250f/1000f + 6);
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
        activeSquare.setPosition(3 + g.pos.x*250f/1000f - 10, 3 + g.pos.y*250f/1000f - 10);
    }
    @Override
    void render(float delta){
        //must be at the beginning
        camera.update();
        shape.setProjectionMatrix(camera.combined);

        if(systemGroup.isVisible()){
            Vector2 pos = new Vector2();
            Grid grid = state.grids[game.getGrid()];

            //draw embedded rectangle
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(Color.BLACK);
            shape.rect(CON_RECT.x, CON_RECT.y, CON_RECT.width, CON_RECT.height);
            shape.end();

            //draw the station
            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.setColor(state.findColorForOwner(grid.station.owner));
            Polygon stationDrawable = new Polygon(grid.station.polygon.getVertices());
            stationDrawable.scale(SYS_SCALE);
            shape.polygon(stationDrawable.getTransformedVertices());
            shape.end();

            //draw the ships
            shape.begin(ShapeRenderer.ShapeType.Filled);
            for(Ship s : grid.ships.values()){
                calcSysPos(s.pos, pos);
                if(!(Math.abs(s.pos.x) > parent.getWidth()/(2*SYS_SCALE)
                        || Math.abs(s.pos.y) > parent.getWidth()/(2*SYS_SCALE))){
                    float radius;
                    switch(s.model.tier){
                        case Titan:
                            radius = 3;
                            break;
                        case Barge:
                        case Battleship:
                            radius = 2;
                            break;
                        case Cruiser:
                        case Frigate:
                        default:
                            radius = 1;
                    }

                    shape.setColor(state.findColorForOwner(s.owner));
                    shape.circle(pos.x, pos.y, radius);
                }
            }
            shape.end();

            //draw the viewport rectangle
            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.setColor(Color.WHITE);

            //get the camera info
            float[] cInfo = game.findViewportCamInfo();
            //calculate the points
            float x1 = Math.min(Math.max((cInfo[0] - cInfo[2]*Main.WIDTH/2f) * SYS_SCALE/Game.LTR, CON_RECT.x), CON_RECT.x+CON_RECT.width);
            float y1 = Math.min(Math.max((cInfo[1] - cInfo[2]*Main.HEIGHT/2f) * SYS_SCALE/Game.LTR, CON_RECT.y), CON_RECT.y+CON_RECT.height);
            float x2 = Math.min(Math.max((cInfo[0] - cInfo[2]*Main.WIDTH/2f + Main.WIDTH*cInfo[2]) * SYS_SCALE/Game.LTR, CON_RECT.x), CON_RECT.x+CON_RECT.width);
            float y2 = Math.min(Math.max((cInfo[1] - cInfo[2]*Main.HEIGHT/2f + Main.HEIGHT*cInfo[2]) * SYS_SCALE/Game.LTR, CON_RECT.y), CON_RECT.y+CON_RECT.height);
            //draw the lines
            shape.line(x1, y1, x1, y2);
            shape.line(x1, y2, x2, y2);
            shape.line(x2, y2, x2, y1);
            shape.line(x2, y1, x1, y1);

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

        Image embedded = new Image(Asset.retrieve(Asset.Pixel.BLACK));
        embedded.setBounds(3, 3, parent.getWidth()-6, parent.getHeight()-6);
        group.addActor(embedded);

        activeSquare = new Image(Asset.retrieve(Asset.UiBasic.WHITE_SQUARE_1));
        activeSquare.setPosition(parent.getWidth()/2, parent.getHeight()/2);
        activeSquare.setSize(20, 20);
        group.addActor(activeSquare);

        return group;
    }
    private Group initSystemGroup(){
        Group group = new Group();
        group.setVisible(false);

        Actor actor = new Actor();
        actor.setBounds(3, 3, 250, 250);
        actor.addListener(new ClickListener(Input.Buttons.LEFT){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(event.isHandled()) return;
                game.minimapSystemMouseDownEvent(Input.Buttons.LEFT,
                        (x-125)/SYS_SCALE, (y-125)/SYS_SCALE);
                event.handle();
            }
        });
        actor.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
                if(button == Input.Buttons.LEFT){
                    game.minimapSystemMouseDownEvent(Input.Buttons.LEFT,
                            (x-125)/SYS_SCALE, (y-125)/SYS_SCALE);
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
                            (x-125)/SYS_SCALE, (y-125)/SYS_SCALE);
                }
            }
        });
        group.addActor(actor);

        return group;
    }


    /* Event Methods */

    /**
     * This method changes where the focus square on the minimap is.
     */
    void switchFocusedGrid(int newGrid){
        activeSquare.setPosition(3 + state.grids[newGrid].pos.x*250f/1000f - 10,
                3 + state.grids[newGrid].pos.y*250f/1000f - 10);
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
        output.set(SYS_SCALE*x, SYS_SCALE*y);
    }
    private void calcSysPos(Vector2 input, Vector2 output){
        calcSysPos(input.x, input.y, output);
    }

}
