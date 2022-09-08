package com.twisted.local.lobby;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Sector class for the Lobby.
 */
abstract class Sector {

    protected final Lobby lobby;
    protected final Skin skin;

    public Sector(Lobby lobby){
        this.lobby = lobby;
        this.skin = lobby.skin;
    }

    /* Graphics Methods */

    /**
     * Should be called if overridden.
     */
    protected Group init(){
        return new Group();
    }

    /**
     * Called each frame.
     */
    abstract void render(float delta);

    /**
     * Called to clean up.
     */
    abstract void dispose();

}
