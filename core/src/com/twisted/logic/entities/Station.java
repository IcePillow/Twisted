package com.twisted.logic.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.twisted.logic.desiptors.Gem;
import com.twisted.logic.desiptors.CurrentJob;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


/**
 * Representation of a station in game (both client and serverside).
 */
public abstract class Station implements Serializable {

    /* Graphics (clientside) */

    public static HashMap<String, Texture> viewportSprites = new HashMap<>();

    public Image minimapSprite;
    public Label minimapLabel;

    public Label[] industryResourceLabels; //{calcite, kernite, pyrene, crystal}


    /* Variables */

    //final descriptive variables
    public final int grid;
    public final String name;

    //high level state variables
    public int owner; //0 for none
    public Stage stage;

    //lower level state variables
    public final ArrayList<CurrentJob> currentJobs;
    public final int[] resources;


    /**
     * Constructor
     */
    public Station(int grid, String name, int owner, Stage stage){
        this.name = name;
        this.grid = grid;
        this.owner = owner;
        this.stage = stage;

        currentJobs = new ArrayList<>();
        resources = new int[]{0, 0, 0, 0};
    }


    /* Data Methods */

    public abstract String getFilename();

    public abstract Job[] getPossibleJobs();


    /* Utility Methods */

    /**
     * Checks if a resource array has enough for this job.
     */
    public boolean enoughForJob(Job job){
        for(int i=0; i<Gem.orderedGems.length; i++){
            if(job.getGemCost(Gem.orderedGems[i]) > resources[i]) return false;
        }
        return true;
    }

    /**
     * Removes resources from this station for the current job. Does no checks for whether the
     * resources are available.
     */
    public void removeResourcesForJob(Job job){
        for(int i=0; i<Gem.orderedGems.length; i++){
            resources[i] -= job.getGemCost(Gem.orderedGems[i]);
        }
    }


    /* Enums */

    /**
     * The type of station that this is.
     *
     * Lowercase of type is filename.
     */
    public enum Type {
        EXTRACTOR,
        HARVESTER,
        LIQUIDATOR
    }

    /**
     * The stage that the station is currently in.
     */
    public enum Stage {
        NONE,
        DEPLOYMENT,
        ARMORED,
        REINFORCED,
        VULNERABLE
    }

    /**
     * The possible things (ships/stations) that can be constructed.
     */
    public enum Job {

        Frigate(10, 2, 0, 0, 15),
        Cruiser(25, 5, 2, 0, 30),
        Battleship(100, 0, 10, 10, 90),
        Barge(150, 50, 0, 0, 120),
        Titan(500, 80, 100, 50, 600),
        Extractor(300, 15, 0, 0, 180),
        Harvester(200, 20, 5, 0, 180),
        Liquidator(200, 25, 10, 0, 180);

        public final int calcite;
        public final int kernite;
        public final int pyrene;
        public final int crystal;
        public final int duration; //in seconds

        Job(int calcite, int kernite, int pyrene, int crystal, int duration){
            this.calcite = calcite;
            this.kernite = kernite;
            this.pyrene = pyrene;
            this.crystal = crystal;

            this.duration = duration;
        }

        /**
         * Gets the gem cost for this job.
         */
        public int getGemCost(Gem gem){
            switch (gem){
                case Calcite:
                    return this.calcite;
                case Kernite:
                    return this.kernite;
                case Pyrene:
                    return this.pyrene;
                case Crystal:
                    return this.crystal;
                default:
                    System.out.println("[Error] Unexpected gem type.");
                    new Exception().printStackTrace();
                    return 0;
            }
        }
    }

}
