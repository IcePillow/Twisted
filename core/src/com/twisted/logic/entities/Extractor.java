package com.twisted.logic.entities;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.twisted.local.game.Game;

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
    public Extractor(int grid, String name, int owner, Stage stage, boolean graphics) {
        super(grid, name, owner, stage);

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
