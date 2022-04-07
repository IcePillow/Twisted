package com.twisted.logic.entities;

public class Liquidator extends Station{

    private static final Job[] possibleJobs = new Job[]{
            Job.Frigate, Job.Cruiser,
            Job.Extractor
    };


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
    public Job[] getPossibleJobs() {
        return possibleJobs;
    }
}
