package com.twisted.logic.entities.station;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.twisted.Asset;
import com.twisted.logic.descriptors.*;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;

import java.util.ArrayList;
import java.util.LinkedHashMap;


/**
 * Representation of a station in game (both client and serverside).
 */
public abstract class Station extends Entity {

    /* Constants */

    public final static int PACKED_STATION_SLOTS = 3;


    /* Graphics (clientside) */

    public Polygon polygon;


    /* Logic (serverside) */

    public float[] chargeResource;


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
        this.shieldHealth = model.maxShield;
        this.hullHealth = model.maxHull;
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
        return this.entityModel().name() + " " + gridNick;
    }
    @Override
    public String getFleetName(){
        switch(this.entityModel()){
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
    public boolean enoughForJob(JobType job){
        for(int i=0; i<Gem.orderedGems.length; i++){
            if(job.getGemCost(Gem.orderedGems[i]) > resources[i]) return false;
        }
        return true;
    }
    /**
     * Removes resources from this station for the current job. Does no checks for whether the
     * resources are available.
     */
    public void removeResourcesForJob(JobType job){
        for(int i=0; i<Gem.orderedGems.length; i++){
            resources[i] -= job.getGemCost(Gem.orderedGems[i]);
        }
    }
    /**
     * Returns what kind of station this is.
     */
    @Override
    public Model entityModel(){
        if(this instanceof Extractor) return Station.Model.Extractor;
        else if(this instanceof Harvester) return Station.Model.Harvester;
        else return Station.Model.Liquidator;
    }


    /* State Methods */

    public boolean isDocked(){
        return false;
    }
    public boolean isValidBeacon(){
        return true;
    }
    public float getSigRadius(){
        return 30;
    }


    /* Enums */

    public enum Tier implements Entity.Tier {
        Station;

        @Override
        public String getFilename(){
            return this.name().toLowerCase();
        }
    }
    public enum Model implements Entity.Model {
        Extractor(Tier.Station,
                new float[]{-0.64f,0,  -0.32f,0.64f,   0.32f,0.64f,  0.64f,0,  0.32f,-0.64f,  -0.32f,-0.64f},
                new JobType[]{JobType.Alke, JobType.Helios, JobType.Themis, JobType.Heron, JobType.Extractor, JobType.Harvester, JobType.Liquidator},
                4, 100, 80, 5, 30,
                1f, 1, 0.2f
        ),
        Harvester(Tier.Station,
                new float[]{-0.64f,0,  -0.32f,0.64f,   0.32f,0.64f,  0.64f,0,  0.32f,-0.64f,  -0.32f,-0.64f},
                new JobType[]{JobType.Alke, JobType.Helios, JobType.Themis, JobType.Extractor},
                4, 10, 8, 5, 30,
                1f, 1, 0.2f
        ),
        Liquidator(Tier.Station,
                new float[]{-0.64f,0,  -0.32f,0.64f,   0.32f,0.64f,  0.64f,0,  0.32f,-0.64f,  -0.32f,-0.64f},
                new JobType[]{JobType.Alke, JobType.Helios, JobType.Extractor, JobType.Nyx},
                4, 10, 8, 5, 30,
                1f, 1, 0.2f
        );

        //data methods from entity
        @Override
        public String getFilename(){
            return this.name().toLowerCase();
        }
        @Override
        public Entity.Tier getTier(){
            return tier;
        }
        @Override
        public float[] getVertices() {
            return vertices;
        }
        @Override
        public float getPaddedLogicalRadius(){
            return (1.28f * 1.1f);
        }

        //data storage
        public final Tier tier;
        public final float deployTime;
        public final float[] vertices;
        public final JobType[] possibleJobs;
        public final int maxShield, maxHull;
        public final float armoredDuration, vulnerableDuration;
        public final float dockingRadius;
        public final float shieldRegen, hullRegen;


        /**
         * Constructor
         */
        Model(Tier tier, float[] vertices, JobType[] possibleJobs, float deployTime, int maxShield,
              int maxHull, float armoredDuration, float vulnerableDuration, float dockingRadius,
              float shieldRegen, float hullRegen){
            this.tier = tier;
            this.vertices = vertices;
            this.deployTime = deployTime;
            this.possibleJobs = possibleJobs;
            this.maxShield = maxShield;
            this.maxHull = maxHull;
            this.armoredDuration = armoredDuration;
            this.vulnerableDuration = vulnerableDuration;
            this.dockingRadius = dockingRadius;
            this.shieldRegen = shieldRegen;
            this.hullRegen = hullRegen;
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

}
