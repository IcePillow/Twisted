package com.twisted.logic.mobs;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.descriptors.Grid;

public abstract class Mobile {

    //meta
    public int id;

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

    //update
    /**
     * @return True if this entity should fizzle. False otherwise.
     */
    public abstract boolean update(float delta, Grid grid);

    //typing
    public Type getType(){
        if(this instanceof BlasterBolt) return Type.BlasterBolt;
        else return null;
    }

    //data
    public abstract float[] getVertices();

    //enum
    public enum Type {
        BlasterBolt
    }

}
