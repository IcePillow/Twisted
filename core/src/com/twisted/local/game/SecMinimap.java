package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.twisted.Main;
import com.twisted.local.game.state.GamePlayer;
import com.twisted.logic.descriptors.EntPtr;
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

    //assets
    private HashMap<Integer, TextureRegionDrawable> ownerToIcon;


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
    void load(){
        loadAssets();

        //load the grids
        for(Grid g : state.grids){
            //load the minimap icon
            Image image = new Image(ownerToIcon.get(g.station.owner));
            stationSprites.put(g.station.getId(), image);

            //position is (indent + scaled positioning - half the width)
            image.setPosition(3 + g.pos.x*250f/1000f - 5, 3 + g.pos.y*250f/1000f - 5);
            image.setSize(10, 10);

            //load the minimap label
            Label label = new Label(g.station.getFullName(), skin, "small", Color.GRAY);
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

    void loadAssets(){
        //station icons
        ownerToIcon = new HashMap<>();
        ownerToIcon.put(0, new TextureRegionDrawable(new Texture(Gdx.files.internal("images/circles/gray.png"))));
        for(GamePlayer p : state.players.values()){
            ownerToIcon.put(p.getId(), new TextureRegionDrawable(new Texture(
                    Gdx.files.internal("images/circles/" + p.getFileCode() + ".png"))));
        }
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
     * Updates the station image.
     */
    void updateStation(Station station){
        stationSprites.get(station.getId());

        if(!ownerToIcon.get(station.owner).equals(
                stationSprites.get(station.getId()).getDrawable())){

            stationSprites.get(station.getId()).setDrawable(ownerToIcon.get(station.owner));
        }
    }
}
