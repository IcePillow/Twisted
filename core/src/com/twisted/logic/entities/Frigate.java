package com.twisted.logic.entities;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;

public class Frigate extends Ship{

    /* Data */

    private static final Vector2 size = new Vector2(0.16f, 0.16f);
    public final static float[] vertices = new float[]{-0.08f,-0.08f,  0.08f,-0.08f,  0,0.08f};

    /**
     * Constructor
     */
    public Frigate(int shipId, int owner, boolean graphics){
        super(shipId, owner);

        if(graphics){
            polygon = new Polygon(vertices);
        }
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
    @Override
    public float[] getVertices(){
        return vertices;
    }

}
