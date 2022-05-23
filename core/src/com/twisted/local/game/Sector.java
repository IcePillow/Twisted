package com.twisted.local.game;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.twisted.local.game.state.GameState;

public abstract class Sector {

    /**
     * Setting the state.
     */
    public abstract void setState(GameState state);

    /**
     * Called upon construction of the Game class.
     */
    public abstract Group init();

    /**
     * Called when the game start message is received.
     */
    public abstract void load();

    /**
     * Called each frame.
     */
    public abstract void render();

    /**
     * Called to clean up.
     */
    public abstract void dispose();

}
