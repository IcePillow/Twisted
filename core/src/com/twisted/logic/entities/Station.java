package com.twisted.logic.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.twisted.local.game.util.JobRow;
import com.twisted.logic.descriptors.Gem;
import com.twisted.logic.descriptors.CurrentJob;
import com.twisted.logic.descriptors.Grid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;


/**
 * Representation of a station in game (both client and serverside).
 */
public abstract class Station extends Entity implements Serializable {

    /* Graphics (clientside) */

    public Image minimapSprite;
    public Label minimapLabel;

    public Label[] industryResourceLabels; //{calcite, kernite, pyrene, crystal}

    public Polygon polygon;


    /* Variables */

    //final descriptive variables
    @Override
    public int getId(){
        return grid;
    }
    public final String nickname;
    public final String shortNickname;

    //high level state variables
    public Stage stage;

    //lower level state variables
    public final ArrayList<CurrentJob> currentJobs;
    public final int[] resources;
    public final LinkedHashMap<Integer, Ship> dockedShips;


    /**
     * Constructor
     */
    public Station(int grid, String gridNick, int owner, Stage stage){
        this.grid = grid;
        this.owner = owner;
        this.stage = stage;

        //set nicknames
        this.nickname = this.getType().toString() + " " + gridNick;
        switch(this.getType()){
            case Extractor:
                this.shortNickname = "Extrac " + gridNick;
                break;
            case Harvester:
                this.shortNickname = "Harves " + gridNick;
                break;
            case Liquidator:
                this.shortNickname = "Liquid " + gridNick;
                break;
            default:
                System.out.println("Unexpected Station type in Station()");
                this.shortNickname = "[???]";
        }

        currentJobs = new ArrayList<>();
        resources = new int[]{0, 0, 0, 0};
        dockedShips = new LinkedHashMap<>();

        this.pos = new Vector2(0, 0);
        this.rot = 0;
    }


    /* Data Methods */

    public abstract Job[] getPossibleJobs();
    public abstract float[] getVertices();

    @Override
    public float getPaddedLogicalRadius(){
        return (1.28f * 1.1f);
    }


    /* Action Methods */

    @Override
    public void takeDamage(Grid grid, float amount){
        //TODO
    }


    /* Utility Methods */

    /**
     * Checks if a resource array has enough for this job.
     * @return true if there are enough resources, false otherwise.
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

    /**
     * Returns what kind of station this is.
     */
    public Type getType(){
        if(this instanceof Extractor) return Type.Extractor;
        else if(this instanceof Harvester) return Type.Harvester;
        else return Type.Liquidator;
    }


    /* Enums */

    /**
     * The type of station that this is.
     *
     * Lowercase of type is filename.
     */
    public enum Type {
        Extractor,
        Harvester,
        Liquidator
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

        Frigate(10, 2, 0, 0, 2),
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
