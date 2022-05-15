package com.twisted.logic.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;
import java.util.HashMap;

public abstract class Ship implements Serializable {

    /* Graphics (clientside) */

    public static HashMap<String, Texture> viewportSprites = new HashMap<>();

    /* State */

    public Vector2 position;
    public Vector2 velocity;
    public float rotation;

    public int owner;
    public int shipId;


    /**
     * Constructor
     */
    public Ship(int shipId, int owner, float xpos, float ypos){
        this.shipId = shipId;
        this.position = new Vector2(xpos, ypos);
        this.velocity = new Vector2(0, 0);
        this.owner = owner;
    }

    /* Data Methods */

    public abstract String getFilename();
    public abstract Vector2 getSize();


    /* Enums */

    public enum Type {
        FRIGATE
    }

}
