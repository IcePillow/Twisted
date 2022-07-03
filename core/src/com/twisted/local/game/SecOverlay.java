package com.twisted.local.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

class SecOverlay extends Sector {

    //reference variables
    private Game game;

    //graphics utilities
    private Skin skin;

    //actors
    private Label bottomActionLabel;


    /**
     * Constructor
     */
    SecOverlay(Game game){
        this.game = game;
        this.skin = game.skin;
    }

    @Override
    Group init() {

        //don't use the super method because this shouldn't handle input events
        Group parent = new Group();

        //make the action label
        bottomActionLabel = new Label("", skin, "small", Color.DARK_GRAY);
        bottomActionLabel.setPosition(4, 8);
        parent.addActor(bottomActionLabel);

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


    /* Event Methods */

    void updateActionLabel(String text){
        bottomActionLabel.setText(text);
    }


}
