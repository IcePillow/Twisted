package com.twisted.local.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.twisted.local.game.state.GameState;

public abstract class Sector {

    protected GameState state;
    void setState(GameState state){
        this.state = state;
    }

    /**
     * Result from listening on the viewport.
     */
    void viewportClickEvent(Vector2 screenPos, Vector2 gamePos,
                                   SecViewport.ClickType type, int typeId){}

    /**
     * Result from listening on the minimap.
     */
    void minimapClickEvent(int grid){}

    /**
     * Called when external listening is cancelled.
     */
    void crossSectorListeningCancelled(){}


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


    enum Type {
        VIEWPORT,
        MINIMAP,
        INDUSTRY,
        FLEET,
        DETAILS,
        OVERLAY,
        OPTIONS,
    }

}
