package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.twisted.Asset;

class SecOptions extends Sector{

    //reference variables
    private Game game;

    //graphics utilities
    private Stage stage;
    private Skin skin;

    //tree
    private Group parent;


    /**
     * Constructor
     */
    SecOptions(Game game, Stage stage){
        this.game = game;
        this.stage = stage;
        this.skin = game.skin;
    }


    /* Standard Methods */

    @Override
    Group init() {
        //create the group
        parent = super.init();
        parent.setBounds(420, 150, 600, 500);

        //set the background
        Image main = new Image(Asset.retrieve(Asset.Shape.PIXEL_BLACK));
        main.setSize(parent.getWidth(), parent.getHeight());
        parent.addActor(main);

        //toggle visibility of the options menu
        stage.addListener(new InputListener() {
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == 111){
                    parent.setVisible(!parent.isVisible());
                }
                return true;
            }
        });

        //make invisible and return
        parent.setVisible(false);
        return parent;
    }

    @Override
    void load() {

    }

    @Override
    void render(float delta) {

    }

    @Override
    void dispose() {

    }
}
