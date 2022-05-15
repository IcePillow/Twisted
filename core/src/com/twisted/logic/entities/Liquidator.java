package com.twisted.logic.entities;

import com.badlogic.gdx.math.Vector2;

public class Liquidator extends Station {

    private static final Job[] possibleJobs = new Job[]{
            Job.Frigate, Job.Cruiser,
            Job.Extractor
    };
    private static final Vector2 size = new Vector2(128, 128);


    /**
     * Constructor
     */
    public Liquidator(int grid, String name, int owner, Stage stage) {
        super(grid, name, owner, stage);
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
}
