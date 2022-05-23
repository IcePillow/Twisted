package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.twisted.local.game.state.GameState;

public class SecOptions extends Sector{

    //reference variables
    private Game game;
    private GameState state;
    @Override
    public void setState(GameState state) {
        this.state = state;
    }

    //graphics utilities
    private Stage stage;
    private Skin skin;

    //tree
    private Group parent;


    /**
     * Constructor
     */
    public SecOptions(Game game, Skin skin, Stage stage){
        this.game = game;
        this.skin = skin;
        this.stage = stage;
    }


    /* Standard Methods */

    @Override
    public Group init() {
        //create the group
        parent = new Group();
        parent.setBounds(420, 150, 600, 500);

        //set the background
        Image main = new Image(new Texture(Gdx.files.internal("images/pixels/black.png")));
        main.setSize(parent.getWidth(), parent.getHeight());
        parent.addActor(main);

        //toggle visibility of the options menu
        stage.addListener(new InputListener() {
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == 111){
                    parent.setVisible(! parent.isVisible());
                }
                return true;
            }
        });

        //make invisible and return
        parent.setVisible(false);
        return parent;
    }

    @Override
    public void load() {

    }

    @Override
    public void render() {

    }

    @Override
    public void dispose() {

    }
}
