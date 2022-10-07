package com.twisted.local.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.twisted.local.game.state.ClientGameState;
import com.twisted.logic.descriptors.EntPtr;

abstract class Sector {

    protected ClientGameState state;
    void setState(ClientGameState state){
        this.state = state;
    }


    /* Cross Sector Events */

    /**
     * Result from listening on the viewport.
     */
    void viewportClickEvent(Vector2 screenPos, Vector2 gamePos, EntPtr entity){}

    /**
     * Result from listening on the minimap.
     */
    void minimapClickEvent(int grid){}

    /**
     * Result from listening on the fleet window.
     */
    void fleetClickEvent(EntPtr ptr){}

    /**
     * Called when external listening is cancelled.
     */
    void crossSectorListeningCancelled(){}


    /* Standard Graphics Methods */

    /**
     * Called upon construction of the Game class.
     *
     * The default method (here) creates a group that should be used as the parent group. This
     * group will automatically handle all click events used on it.
     */
    Group init(){
        Group group = new Group();

        group.addListener(new ClickListener(Input.Buttons.LEFT){
            @Override
            public void clicked(InputEvent event, float x, float y){
                event.handle();
            }
        });
        group.addListener(new ClickListener(Input.Buttons.RIGHT){
            @Override
            public void clicked(InputEvent event, float x, float y){
                event.handle();
            }
        });

        return group;
    }

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
