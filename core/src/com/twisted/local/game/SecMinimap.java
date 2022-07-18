package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.twisted.Main;
import com.twisted.logic.descriptors.Grid;

class SecMinimap extends Sector {

    //reference variables
    private Game game;

    //graphics utilities
    private Skin skin;

    //tree
    private Group parent;


    /**
     * Constructor
     */
    SecMinimap(Game game){
        this.game = game;
        this.skin = game.skin;
    }


    /* Standard Methods */

    @Override
    Group init() {
        parent = super.init();
        parent.setBounds(Main.WIDTH-256, 0, 256, 256);

        Image main = new Image(new Texture(Gdx.files.internal("images/pixels/darkpurple.png")));
        main.setSize(parent.getWidth(), parent.getHeight());
        parent.addActor(main);

        Image embedded = new Image(new Texture(Gdx.files.internal("images/pixels/black.png")));
        embedded.setBounds(3, 3, parent.getWidth()-6, parent.getHeight()-6);
        parent.addActor(embedded);

        Image activeSquare = new Image(new Texture(Gdx.files.internal("images/ui/white-square-1.png")));
        activeSquare.setPosition(parent.getWidth()/2, parent.getHeight()/2);
        activeSquare.setSize(20, 20);
        parent.addActor(activeSquare);
        
        return parent;
    }

    @Override
    void load() {

        //load the grids
        for(Grid g : state.grids){
            //load the minimap icon
            if(g.station.owner == 0){
                g.station.minimapSprite = new Image(new Texture(Gdx.files.internal("images/circles/gray.png")));
            }
            else {
                g.station.minimapSprite = new Image(new Texture(Gdx.files.internal("images/circles/"
                        + state.players.get(g.station.owner).getFileCode() + ".png")));
            }

            //position is (indent + scaled positioning - half the width)
            g.station.minimapSprite.setPosition(3 + g.pos.x*250f/1000f - 5, 3 + g.pos.y*250f/1000f - 5);
            g.station.minimapSprite.setSize(10, 10);

            //load the minimap label
            Label label = new Label(g.station.nickname, skin, "small", Color.GRAY);
            g.station.minimapLabel = label;
            label.setVisible(false);
            if(g.station.minimapSprite.getX() < 3+label.getWidth()/2f){
                label.setPosition((g.pos.x*250f/1000f-label.getWidth()/2f) + (3+label.getWidth()/2f) - (g.station.minimapSprite.getX()), g.pos.y*250f/1000f + 6);
            }
            else if(g.station.minimapSprite.getX() + label.getWidth()/2f > 248){
                label.setPosition((g.pos.x*250f/1000f-label.getWidth()/2f) - (g.station.minimapSprite.getX()+label.getWidth()/2f) + (248), g.pos.y*250f/1000f + 6);
            }
            else {
                label.setPosition((g.pos.x*250f/1000f-label.getWidth()/2f), g.pos.y*250f/1000f + 6);
            }

            //add to the minimap group
            parent.addActor(g.station.minimapSprite);
            parent.addActor(g.station.minimapLabel);

            //listeners
            g.station.minimapSprite.addListener(event -> {
                //entering and exiting
                if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.enter){
                    g.station.minimapLabel.setVisible(true);
                }
                else if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.exit){
                    g.station.minimapLabel.setVisible(false);
                }

                return true;
            });

            g.station.minimapSprite.addListener(new ClickListener(Input.Buttons.LEFT){
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
    void render(float delta) {

    }

    @Override
    void dispose() {

    }


    /* Event Methods */

    /**
     * This method changes where the focus square on the minimap is.
     */
    void switchFocusedGrid(int newGrid){
        parent.getChild(2).setPosition(3 + state.grids[newGrid].pos.x*250f/1000f - 10,
                3 + state.grids[newGrid].pos.y*250f/1000f - 10);
    }
}
