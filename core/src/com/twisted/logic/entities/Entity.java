package com.twisted.logic.entities;

import com.badlogic.gdx.math.Vector2;

public abstract class Entity {

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

    //data methods
    public abstract float[] getVertices();
    /**
     * Returns the logical radius (i.e. not in visual coords) padded a little. Currently used
     * to display the selection circle on the viewport.
     */
    public abstract float getPaddedLogicalRadius();

    public enum Type {
        STATION,
        SHIP
    }

}
