package com.twisted.logic.entities;

public class Harvester extends Station{

    private static final Job[] possibleJobs = new Job[]{
            Job.Frigate, Job.Cruiser, Job.Battleship, Job.Titan,
            Job.Extractor};


    /**
     * Constructor
     */
    public Harvester(int grid, String name, int owner, Stage stage) {
        super(grid, name, owner, stage);
    }


    /* Data Methods */

    @Override
    public String getFilename() {
        return "harvester";
    }

    @Override
    public Job[] getPossibleJobs() {
        return possibleJobs;
    }
}
