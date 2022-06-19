package com.twisted.logic.entities;

import com.badlogic.gdx.math.Vector2;

public class Frigate extends Ship {

    /* Data */

    public static final float[] vertices = new float[]{-0.08f,-0.08f,  0.08f,-0.08f,  0,0.08f};

    /**
     * Constructor
     */
    public Frigate(int shipId, int owner, Vector2 position, Vector2 velocity, float rotation,
                   float warpTimeToLand){
        super(shipId, owner, position, velocity, rotation, warpTimeToLand);
    }


    /* Data Methods */

    @Override
    public float[] getVertices(){
        return vertices;
    }
    @Override
    public float getMaxSpeed() {
        return 0.8f;
    }
    @Override
    public float getMaxAccel() {
        return 0.4f;
    }
    @Override
    public int getMaxHealth(){
        return 100;
    }
    @Override
    public float getPaddedLogicalRadius() {
        return 0.17f;
    }

}
