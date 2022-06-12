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

    public enum Type {
        STATION,
        SHIP
    }

}
