package com.twisted.local.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.twisted.Asset;
import com.twisted.Main;
import com.twisted.local.lib.Ribbon;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Station;

import java.util.HashMap;

class SecMinimap extends Sector {

    //reference variables
    private Game game;

    //graphics utilities
    private Skin skin;

    //tree
    private Group parent;
    private final HashMap<Integer, Image> stationSprites;


    /**
     * Constructor
     */
    SecMinimap(Game game){
        this.game = game;
        this.skin = game.skin;

        stationSprites = new HashMap<>();
    }


    /* Standard Methods */

    @Override
    Group init(){
        parent = super.init();
        parent.setBounds(Main.WIDTH-256, 0, 256, 256);

        Ribbon ribbon = new Ribbon(Asset.retrieve(Asset.Shape.PIXEL_DARKPURPLE), 3);
        ribbon.setSize(parent.getWidth(), parent.getHeight());
        parent.addActor(ribbon);

        Image embedded = new Image(Asset.retrieve(Asset.Shape.PIXEL_BLACK));
        embedded.setBounds(3, 3, parent.getWidth()-6, parent.getHeight()-6);
        parent.addActor(embedded);

        Image activeSquare = new Image(Asset.retrieve(Asset.UiBasic.WHITE_SQUARE_1));
        activeSquare.setPosition(parent.getWidth()/2, parent.getHeight()/2);
        activeSquare.setSize(20, 20);
        parent.addActor(activeSquare);
        
        return parent;
    }

    @Override
    void load(){
        //load the grids
        for(Grid g : state.grids){
            //load the minimap icon
            Image image;

            if(state.players.get(g.station.owner) == null){
                image = new Image(Asset.retrieve(Asset.Shape.CIRCLE_GRAY));
            }
            else {
                image = new Image(Asset.retrieve(state.players.get(g.station.owner).getMinimapShapeAsset()));
            }
            stationSprites.put(g.station.getId(), image);

            //position is (indent + scaled positioning - half the width)
            image.setPosition(3 + g.pos.x*250f/1000f - 5, 3 + g.pos.y*250f/1000f - 5);
            image.setSize(10, 10);

            //load the minimap label
            Label label = new Label(g.station.getFullName(), Asset.labelStyle(Asset.Avenir.MEDIUM_14));
            label.setColor(Color.GRAY);
            label.setVisible(false);
            if(image.getX() < 3+label.getWidth()/2f){
                label.setPosition(
                        (g.pos.x*250f/1000f-label.getWidth()/2f) + (3+label.getWidth()/2f) - (image.getX()),
                        g.pos.y*250f/1000f + 6);
            }
            else if(image.getX() + label.getWidth()/2f > 248){
                label.setPosition(
                        (g.pos.x*250f/1000f-label.getWidth()/2f) - (image.getX()+label.getWidth()/2f) + (248),
                        g.pos.y*250f/1000f + 6);
            }
            else {
                label.setPosition((g.pos.x*250f/1000f-label.getWidth()/2f), g.pos.y*250f/1000f + 6);
            }

            //add to the minimap group
            parent.addActor(image);
            parent.addActor(label);

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
                    game.minimapClickEvent(Input.Buttons.LEFT, g.id);
                    event.handle();
                }
            });
        }

        //move the active square
        Grid g = state.grids[game.getGrid()];
        parent.getChild(2).setPosition(3 + g.pos.x*250f/1000f - 10, 3 + g.pos.y*250f/1000f - 10);
    }

    @Override
    void render(float delta){

    }

    @Override
    void dispose(){

    }


    /* Event Methods */

    /**
     * This method changes where the focus square on the minimap is.
     */
    void switchFocusedGrid(int newGrid){
        parent.getChild(2).setPosition(3 + state.grids[newGrid].pos.x*250f/1000f - 10,
                3 + state.grids[newGrid].pos.y*250f/1000f - 10);
    }

    /**
     * Updates the station image. Does not check if the update is needed.
     */
    void updateStation(Station station){
        TextureRegionDrawable drawable;
        if(state.players.get(station.owner) == null){
            drawable = Asset.retrieve(Asset.Shape.CIRCLE_GRAY);
        }
        else {
            drawable = Asset.retrieve(state.players.get(station.owner).getMinimapShapeAsset());
        }

        stationSprites.get(station.getId()).setDrawable(drawable);
    }
}
