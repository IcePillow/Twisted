package com.twisted.logic.entities;

public class Extractor extends Station {

    private static final Job[] possibleJobs = new Job[]{
            Job.Frigate, Job.Cruiser, Job.Battleship, Job.Barge,
            Job.Extractor, Job.Harvester, Job.Liquidator
    };


    /**
     * Constructor
     */
    public Extractor(int grid, String name, int owner, Stage stage) {
        super(grid, name, owner, stage);
    }


    /* Data Methods */

    @Override
    public String getFilename() {
        return "extractor";
    }

    @Override
    public Job[] getPossibleJobs() {
        return possibleJobs;
    }
}
