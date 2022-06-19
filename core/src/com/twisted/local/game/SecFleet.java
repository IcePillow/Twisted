package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.twisted.local.game.state.GameState;

public class SecFleet extends Sector {

    //reference variables
    private Game game;

    //graphics utilities
    private Skin skin;

    //tree
    private Group parent;


    /**
     * Constructor
     */
    public SecFleet(Game game, Skin skin){
        this.game = game;
        this.skin = skin;
    }

    @Override
    Group init() {
        //initialize the top level group
        parent = super.init();
        parent.setBounds(0, 230, 300, 450);

        //create the decoration
        Group decoration = new Group();
        decoration.setSize(parent.getWidth(), parent.getHeight());
        parent.addActor(decoration);

        Image ribbon = new Image(new Texture(Gdx.files.internal("images/pixels/darkpurple.png")));
        ribbon.setSize(decoration.getWidth(), decoration.getHeight());
        decoration.addActor(ribbon);
        Image embedded = new Image(new Texture(Gdx.files.internal("images/pixels/black.png")));
        embedded.setBounds(3, 3, parent.getWidth()-6, parent.getHeight()-6);
        decoration.addActor(embedded);

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
