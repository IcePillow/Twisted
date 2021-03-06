package com.twisted.logic.entities;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.twisted.logic.descriptors.Gem;
import com.twisted.logic.descriptors.CurrentJob;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.host.GameHost;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;


/**
 * Representation of a station in game (both client and serverside).
 */
public abstract class Station extends Entity implements Serializable {

    /* Graphics (clientside) */

    public Polygon polygon;


    /* Logic (serverside) */

    public GameHost host;


    /* Variables */

    //final descriptive variables
    @Override
    public int getId(){
        return grid;
    }
    public final String gridNick;

    //stage tracking
    public Stage stage;
    public float stageTimer; //seconds

    //lower level state variables
    public final ArrayList<CurrentJob> currentJobs;
    public final int[] resources;
    public final LinkedHashMap<Integer, Ship> dockedShips;

    //health
    public float shieldHealth;
    public float hullHealth;


    /**
     * Constructor
     */
    public Station(int grid, String gridNick, int owner, Stage stage){
        this.grid = grid;
        this.owner = owner;
        this.stage = stage;

        //set nicknames
        this.gridNick = gridNick;

        currentJobs = new ArrayList<>();
        resources = new int[]{0, 0, 0, 0};
        dockedShips = new LinkedHashMap<>();

        //physics
        this.pos = new Vector2(0, 0);
        this.rot = 0;

        //battle
        this.shieldHealth = getMaxShield();
        this.hullHealth = getMaxHull();
    }


    /* Data Methods */

    public abstract Job[] getPossibleJobs();
    public abstract float[] getVertices();
    @Override
    public float getPaddedLogicalRadius(){
        return (1.28f * 1.1f);
    }
    public float getDockingRadius(){
        return 1f;
    }
    public int getMaxShield(){
        return 10;
    }
    public int getMaxHull(){
        return 8;
    }
    public int getArmoredDuration(){
        return 5;
    }
    public int getVulnerableDuration(){
        return 5;
    }
    public int getDeploymentDuration(){
        return 5;
    }


    /* Naming Methods */

    public String getFullName(){
        return this.getType().name() + " " + gridNick;
    }

    @Override
    public String getFleetName(){
        switch(this.getType()){
            case Extractor:
                return "Extrac " + gridNick;
            case Liquidator:
                return "Liquid " + gridNick;
            case Harvester:
                return "Harves " + gridNick;
            default:
                System.out.println("Unexpected type");
                new Exception().printStackTrace();
                return null;
        }
    }


    /* Action Methods */

    @Override
    public void takeDamage(Grid grid, float amount){

        switch(stage){
            case SHIELDED:{
                shieldHealth -= amount;
                break;
            }
            case VULNERABLE:{
                hullHealth -= amount;
                break;
            }
            //cases where nothing happens
            case DEPLOYMENT:
                //TODO change this case
            case ARMORED:
            case RUBBLE:{
                break;
            }
        }
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
    public enum Type implements Subtype {
        Extractor,
        Harvester,
        Liquidator
    }

    /**
     * The stage that the station is currently in.
     */
    public enum Stage {
        DEPLOYMENT,
        SHIELDED,
        ARMORED,
        VULNERABLE,
        RUBBLE,
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
