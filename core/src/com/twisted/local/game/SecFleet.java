package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.twisted.local.game.state.GameState;

public class SecFleet extends Sector {

    //reference variables
    private Game game;
    private GameState state;
    @Override
    public void setState(GameState state) {
        this.state = state;
    }

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
    public Group init() {
        //initialize the top level group
        parent = new Group();
        parent.setBounds(0, 230, 300, 550);

        //create the subgroups
        Group windowGroup = new Group();
        windowGroup.setSize(300, 420);
        parent.addActor(windowGroup);

        Image windowMain = new Image(new Texture(Gdx.files.internal("images/pixels/darkpurple.png")));
        windowMain.setSize(windowGroup.getWidth(), windowGroup.getHeight());
        windowGroup.addActor(windowMain);

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
