package com.twisted.logic.entities;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.twisted.Asset;

public class Harvester extends Station {

    private static final Job[] possibleJobs = new Job[]{
            Job.Frigate, Job.Cruiser, Job.Battleship, Job.Titan,
            Job.Extractor};
    public final static float[] vertices = new float[]{
            -0.64f, 0,  -0.32f, 0.64f,   0.32f, 0.64f,
            0.64f, 0,  0.32f, -0.64f, -0.32f, -0.64f
    };

    /**
     * Constructor
     */
    public Harvester(int grid, String gridNick, int owner, Stage stage, boolean graphics) {
        super(grid, gridNick, owner, stage);

        if(graphics){
            this.polygon = new Polygon(vertices);
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
