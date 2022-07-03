package com.twisted.logic.entities;

import com.badlogic.gdx.math.Polygon;

public class Extractor extends Station {

    /* Data */

    private static final Job[] possibleJobs = new Job[]{
            Job.Frigate, Job.Cruiser, Job.Battleship, Job.Barge,
            Job.Extractor, Job.Harvester, Job.Liquidator
    };
    public final static float[] vertices = new float[]{
            -0.64f,0,  -0.32f,0.64f,   0.32f,0.64f,
            0.64f,0,  0.32f,-0.64f,  -0.32f,-0.64f
    };


    /**
     * Constructor
     */
    public Extractor(int grid, String gridNick, int owner, Stage stage, boolean graphics) {
        super(grid, gridNick, owner, stage);

        if(graphics){
            polygon = new Polygon(vertices);
        }
    }


    /* Data Methods */

    @Override
    public Job[] getPossibleJobs() {
        return possibleJobs;
    }
    @Override
    public float[] getVertices() {
        return vertices;
    }
}
