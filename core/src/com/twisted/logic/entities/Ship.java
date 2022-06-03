package com.twisted.logic.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;
import java.util.HashMap;

public abstract class Ship extends Entity implements Serializable {

    /* Graphics (clientside) */

    public static HashMap<String, Texture> viewportSprites = new HashMap<>();
    public Polygon polygon; //used for click detection

    /* Logic (serverside) */

    public Vector2 trajectoryVel;

    //command movement description
    public Movement movement;
    public Vector2 targetPos;
    public Entity targetEntity;

    /* State */

    public int owner;
    public int id;

    public String moveCommand;


    /**
     * Constructor
     * @param clientside True if on the clientside, so graphics should be loaded. False if on the
     *                   serverside, so logic should be loaded.
     */
    protected Ship(int id, int owner, Vector2 position, Vector2 velocity, float rotation,
                   boolean clientside){
        //meta data
        this.id = id;
        this.owner = owner;

        //physics
        this.position = position;
        this.velocity = velocity;
        this.rotation = rotation;

        //command data
        this.moveCommand = "Stationary";

        //graphics stuff
        if(clientside){
            polygon = new Polygon(this.getVertices());
        }
        //logic stuff
        else {
            this.trajectoryVel = new Vector2(0, 0);

            this.movement = Movement.STATIONARY;
            this.targetPos = null;
            this.targetEntity = null;
        }
    }

    /* Data Methods */

    public abstract String getFilename();
    public abstract Vector2 getSize();
    public abstract float[] getVertices();
    public abstract float getMaxSpeed();
    public abstract float getMaxAccel();



    /* Utility Methods */

    public Type getType(){
        if(this instanceof Frigate) return Type.Frigate;
        else return null;
    }

    /**
     * Updates the graphics polygon based on current values.
     */
    public void updatePolygon(){
        polygon.setPosition(position.x, position.y);
        polygon.setRotation(rotation);
    }


    /* Enums */

    public enum Type {
        Frigate
    }

    /**
     * The type of movement this ship is attempting to do.
     */
    public enum Movement {
        STATIONARY,

        MOVE_TO_POS,
        ALIGN_TO_ANG,

        ORBIT_STATION,
        ORBIT_SHIP
    }

}
