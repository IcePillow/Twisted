package com.twisted.logic.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.descriptors.Grid;

import java.io.Serializable;
import java.util.HashMap;

public abstract class Ship extends Entity implements Serializable {

    /* Graphics (clientside) */

    public Polygon polygon; //used for click detection

    /* Logic (serverside) */

    //trajectory determining
    public Vector2 trajectoryVel;

    //command movement description
    public Movement movement;
    public Vector2 targetPos;
    public int warpTargetGridId; //used if movement = ALIGN_FOR_WARP

    /* State */

    public int owner;
    public int id;

    public float warpTimeToLand; //0 if not in warp

    public float health;

    public String moveCommand;


    /**
     * Constructor
     */
    protected Ship(int id, int owner, Vector2 position, Vector2 velocity, float rotation,
                   float warpTimeToLand){
        //meta data
        this.id = id;
        this.owner = owner;

        //physics
        this.pos = position;
        this.vel = velocity;
        this.rot = rotation;

        //command data
        this.moveCommand = "Stationary";

        //warping
        this.warpTimeToLand = warpTimeToLand;

        //battle
        this.health = getMaxHealth();

        //graphics stuff
        polygon = new Polygon(this.getVertices());

        //logic stuff
        this.trajectoryVel = new Vector2(0, 0);
        this.movement = Movement.STOPPING;
        this.targetPos = null;
    }

    /* Data Methods */

    public abstract float getMaxSpeed();
    public abstract float getMaxAccel();
    public abstract int getMaxHealth();



    /* Utility Methods */

    public Type getType(){
        if(this instanceof Frigate) return Type.Frigate;
        else return null;
    }

    /**
     * Updates the graphics polygon based on current values.
     */
    public void updatePolygon(){
        polygon.setPosition(pos.x, pos.y);
        polygon.setRotation(rot);
    }

    /**
     * Checks if the ship is able to enter warp based on its velocity.
     */
    public boolean alignedForWarp(Grid originGrid, Grid destGrid){

        //check if speed is high enough
        if(vel.len() < 0.75f*getMaxSpeed()){
            return false;
        }

        //get the ship's angle
        float gridAngle = (float) Math.atan2(destGrid.pos.y-originGrid.pos.y,
                destGrid.pos.x-originGrid.pos.x);

        //compare the two angles
        return Math.abs(rot - gridAngle) <= 0.01f;
    }

    /**
     * Produces a formatted string for display with this ship's position.
     * @param places Number of places after the decimal to include.
     */
    public float[] roundedPosition(int places){
        float p = (float) Math.pow(10, places);

        return new float[]{
                Math.round(pos.x * p) / p,
                Math.round(pos.y * p) / p,
        };
    }

    /**
     * Gets the rounded speed and angle.
     * @param places Places after the decimal to round to for the speed.
     * @return Angle is in nautical degrees (0 is at N and goes CW).
     */
    public float[] roundedBearing(int places){
        float p = (float) Math.pow(10, places);

        float angle = 90-Math.round(rot * 180/Math.PI);
        if(angle < 0) angle += 360;

        return new float[]{
                Math.round(vel.len() * p) / p,
                angle,
        };
    }


    /* Enums */

    public enum Type {
        Frigate
    }

    /**
     * The type of movement this ship is attempting to do.
     */
    public enum Movement {
        STOPPING,

        MOVE_TO_POS,
        ALIGN_TO_ANG,

        ALIGN_FOR_WARP,
        WARPING,

        ORBIT_STATION,
        ORBIT_SHIP
    }

}
