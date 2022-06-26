package com.twisted.logic.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.twisted.local.game.SecFleet;
import com.twisted.local.game.state.GameState;
import com.twisted.local.game.state.PlayColor;
import com.twisted.local.game.util.FleetRow;

public abstract class Entity {

    //meta
    /**
     * Owner. Use 0 for none.
     */
    public int owner;

    //physics
    /**
     * Position.
     */
    public Vector2 pos;
    /**
     * Velocity.
     */
    public Vector2 vel;
    /**
     * Rotation.
     */
    public float rot;

    //graphics
    public FleetRow fleetRow;

    //graphics loading methods
    public void createFleetRow(Skin skin, GameState state, SecFleet sector){
        Color color;
        if(this.owner > 0){
            color = state.players.get(this.owner).color.object;
        }
        else {
            color = PlayColor.GRAY.object;
        }

        fleetRow = new FleetRow(this, skin, color, sector);
    }

    //data methods
    public abstract float[] getVertices();
    /**
     * Returns the logical radius (i.e. not in visual coords) padded a little. Currently used
     * to display the selection circle on the viewport.
     */
    public abstract float getPaddedLogicalRadius();

    //enums
    public enum Type {
        STATION,
        SHIP
    }

}
