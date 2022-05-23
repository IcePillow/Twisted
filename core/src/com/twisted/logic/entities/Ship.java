package com.twisted.logic.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import java.io.Serializable;
import java.util.HashMap;

public abstract class Ship implements Serializable {

    /* Graphics (clientside) */

    public static HashMap<String, Texture> viewportSprites = new HashMap<>();

    public Polygon polygon;
    public Vector2 position;
    public Vector2 velocity;
    public float rotation;


    /* Logic (serverside) */

    public Body body;


    /* State */

    public int owner;
    public int shipId;


    /**
     * Constructor
     */
    protected Ship(int shipId, int owner){
        this.shipId = shipId;
        this.owner = owner;
    }

    /* Data Methods */

    public abstract String getFilename();
    public abstract Vector2 getSize();
    public abstract float[] getVertices();


    /* Utility Methods */

    public Type getType(){
        if(this instanceof Frigate) return Type.Frigate;
        else return null;
    }


    /* Enums */

    public enum Type {
        Frigate
    }

}
