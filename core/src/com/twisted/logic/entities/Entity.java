package com.twisted.logic.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.twisted.local.game.SecFleet;
import com.twisted.local.game.state.GameState;
import com.twisted.local.game.state.PlayColor;
import com.twisted.local.game.util.FleetRow;
import com.twisted.logic.descriptors.Grid;

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
    public boolean fleetRowDisplayingWarp = false;

    //typing methods
    public Type getEntityType(){
        if(this instanceof Ship) return Type.Ship;
        else if(this instanceof Station) return Type.Station;
        else return null;
    }
    /**
     * These ids are not unique across entities, but they should be unique across each subclass
     * of entities.
     */
    public abstract int getId();

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

    /**
     * These vertices are used for drawing.
     * TODO see if these can be combined with the Polygon in the subclasses and used on serverside
     */
    public abstract float[] getVertices();
    /**
     * Returns the logical radius (i.e. not in visual coords) padded a little. Currently used
     * to display the selection circle on the viewport.
     */
    public abstract float getPaddedLogicalRadius();

    //action items
    public abstract void takeDamage(Grid grid, float amount);

    //enums
    public enum Type {
        Station,
        Ship,
    }

}
