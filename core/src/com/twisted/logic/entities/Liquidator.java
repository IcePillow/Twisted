package com.twisted.logic.entities;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;

public class Liquidator extends Station {

    private static final Job[] possibleJobs = new Job[]{
            Job.Frigate, Job.Cruiser,
            Job.Extractor
    };
    private static final Vector2 size = new Vector2(1.28f, 1.28f);
    public final static float[] vertices = new float[]{
            -0.64f, 0,  -0.32f, 0.64f,   0.32f, 0.64f,
            0.64f, 0,  0.32f, -0.64f, -0.32f, -0.64f
    };


    /**
     * Constructor
     */
    public Liquidator(int grid, String name, int owner, Stage stage, boolean graphics) {
        super(grid, name, owner, stage);

        if(graphics){
            this.polygon = new Polygon(vertices);
        }
    }


    /* Data Methods */

    @Override
    public String getFilename() {
        return "liquidator";
    }
    @Override
    public Vector2 getSize(){
        return size;
    }
    @Override
    public Job[] getPossibleJobs() {
        return possibleJobs;
    }
    @Override
    public float[] getVertices() {
        return vertices;
    }
}
