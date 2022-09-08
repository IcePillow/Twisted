package com.twisted.logic.entities;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.twisted.Asset;
import com.twisted.logic.descriptors.Gem;
import com.twisted.logic.descriptors.CurrentJob;
import com.twisted.logic.descriptors.Grid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;


/**
 * Representation of a station in game (both client and serverside).
 */
public abstract class Station extends Entity implements Serializable {

    /* Constants */

    public final static int PACKED_STATION_SLOTS = 3;


    /* Graphics (clientside) */

    public Polygon polygon;


    /* Variables */

    //final descriptive variables
    @Override
    public int getId(){
        return grid;
    }
    public final String gridNick;
    public final Model model;

    //stage tracking
    public Stage stage;
    public float stageTimer; //seconds

    //lower level state variables
    public final ArrayList<CurrentJob> currentJobs;
    public final int[] resources;
    public final LinkedHashMap<Integer, Ship> dockedShips;
    public final Model[] packedStations;

    //health
    public float shieldHealth;
    public float hullHealth;


    /**
     * Constructor
     */
    public Station(Model model, int grid, String gridNick, int owner, Stage stage){
        this.model = model;
        this.grid = grid;
        this.owner = owner;
        this.stage = stage;

        //set nicknames
        this.gridNick = gridNick;

        currentJobs = new ArrayList<>();
        resources = new int[]{0, 0, 0, 0};
        dockedShips = new LinkedHashMap<>();
        packedStations = new Model[PACKED_STATION_SLOTS];

        //physics
        this.pos = new Vector2(0, 0);
        this.rot = 0;
        this.polygon = new Polygon(this.model.vertices); //TODO make clientside only

        //battle
        this.shieldHealth = model.getMaxShield();
        this.hullHealth = model.getMaxHull();
    }


    /* Data Methods */

    public static Asset.UiIcon getStageIcon(Stage stage){
        switch (stage){
            case SHIELDED:
                return Asset.UiIcon.STATION_SHIELDED;
            case ARMORED:
                return Asset.UiIcon.STATION_ARMORED;
            case VULNERABLE:
                return Asset.UiIcon.STATION_VULNERABLE;
            case RUBBLE:
                return null;
            default:
                System.out.println("Unexpected station stage");
                new Exception().printStackTrace();
                return null;
        }
    }


    /* Naming Methods */

    @Override
    public String getFullName(){
        return this.subtype().name() + " " + gridNick;
    }
    @Override
    public String getFleetName(){
        switch(this.subtype()){
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
    public void takeDamage(Grid grid, int owner, float amount){
        super.takeDamage(grid, owner, amount);

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
    @Override
    public Model subtype(){
        if(this instanceof Extractor) return Model.Extractor;
        else if(this instanceof Harvester) return Model.Harvester;
        else return Model.Liquidator;
    }


    /* Enums */

    /**
     * The type of station that this is.
     *
     * Lowercase of type is filename.
     */
    public enum Model implements Subtype {
        Extractor(
                new float[]{-0.64f,0,  -0.32f,0.64f,   0.32f,0.64f,  0.64f,0,  0.32f,-0.64f,  -0.32f,-0.64f},
                new Job[]{Job.Frigate, Job.Cruiser, Job.Battleship, Job.Barge, Job.Extractor, Job.Harvester, Job.Liquidator},
                4
        ),
        Harvester(
                new float[]{-0.64f,0,  -0.32f,0.64f,   0.32f,0.64f,  0.64f,0,  0.32f,-0.64f,  -0.32f,-0.64f},
                new Job[]{Job.Frigate, Job.Cruiser, Job.Battleship, Job.Extractor},
                4
        ),
        Liquidator(
                new float[]{-0.64f,0,  -0.32f,0.64f,   0.32f,0.64f,  0.64f,0,  0.32f,-0.64f,  -0.32f,-0.64f},
                new Job[]{Job.Frigate, Job.Cruiser, Job.Extractor, Job.Titan},
                4
        );

        //data methods from entity
        @Override
        public String getFilename(){
            return this.name().toLowerCase();
        }
        @Override
        public float[] getVertices() {
            return vertices;
        }
        @Override
        public float getPaddedLogicalRadius(){
            return (1.28f * 1.1f);
        }

        //data methods for station
        public float getDeployTime(){
            return deployTime;
        }
        public Job[] getPossibleJobs(){
            return possibleJobs;
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
            return 30;
        }


        //data storage
        private final float deployTime;
        private final float[] vertices;
        private final Job[] possibleJobs;


        /**
         * Constructor
         */
        Model(float[] vertices, Job[] possibleJobs, float deployTime){
            this.vertices = vertices;
            this.deployTime = deployTime;
            this.possibleJobs = possibleJobs;
        }
    }

    /**
     * The stage that the station is currently in.
     */
    public enum Stage {
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
        Barge(1, 1, 0, 0, 1),
//        Barge(150, 50, 0, 0, 120, JobType.SHIP),
        Titan(500, 80, 100, 50, 600),
        Extractor(1, 1, 0, 0, 1),
//        Extractor(300, 15, 0, 0, 180, JobType.PACKED_STATION),
        Harvester(200, 20, 5, 0, 180),
        Liquidator(1, 1, 1, 0, 1);
//        Liquidator(200, 25, 10, 0, 180);

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

        /**
         * Returns the type of station that this job creates.
         */
        public Model getPackedStationType(){
            switch(this){
                case Extractor:
                    return Model.Extractor;
                case Harvester:
                    return Model.Harvester;
                case Liquidator:
                    return Model.Liquidator;
                default:
                    System.out.println("Unexpected request for station type");
                    new Exception().printStackTrace();
                    return null;
            }
        }
    }

}
