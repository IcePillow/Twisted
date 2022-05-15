package com.twisted.logic.entities;

import com.badlogic.gdx.math.Vector2;

public class Frigate extends Ship{

    private static final Vector2 size = new Vector2(16, 16);

    /**
     * Constructor
     */
    public Frigate(int shipId, int owner, float xpos, float ypos){
        super(shipId, owner, xpos, ypos);
    }


    /* Data Methods */

    @Override
    public String getFilename(){
        return "frigate";
    }
    @Override
    public Vector2 getSize() {
        return size;
    }

}
