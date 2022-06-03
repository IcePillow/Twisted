package com.twisted.local.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.twisted.local.game.state.GameState;

public abstract class Sector {

    /**
     * Setting the state.
     */
    abstract void setState(GameState state);

    /**
     * Result from listening on the viewport. All params will be null if no
     */
    abstract void viewportClickEvent(int button, Vector2 screenPos, Vector2 gamePos, SecViewport.ClickType type, int typeId);

    /**
     * Called upon construction of the Game class.
     */
    abstract Group init();

    /**
     * Called when the game start message is received.
     */
    abstract void load();

    /**
     * Called each frame.
     */
    abstract void render(float delta);

    /**
     * Called to clean up.
     */
    abstract void dispose();

}
