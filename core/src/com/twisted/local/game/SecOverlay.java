package com.twisted.local.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.twisted.Main;
import com.twisted.local.game.state.GameState;

public class SecOverlay extends Sector {

    //reference variables
    private Game game;

    //graphics utilities
    private Skin skin;

    //actors
    private Label bottomActionLabel;


    /**
     * Constructor
     */
    public SecOverlay(Game game, Skin skin){
        this.game = game;
        this.skin = skin;
    }

    @Override
    Group init() {

        Group parent = new Group();

        bottomActionLabel = new Label("", skin, "small", Color.DARK_GRAY);
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
        bottomActionLabel.setPosition(4, 8);
    }


}
