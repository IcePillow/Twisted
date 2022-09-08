package com.twisted.logic.entities;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.attach.Weapon;

import java.io.Serializable;

public abstract class Ship extends Entity implements Serializable {

    /* Graphics (clientside) */

    public Polygon polygon; //used for click detection


    /* Logic (serverside) */

    //trajectory determining
    public Vector2 trajectoryVel;

    //command movement description
    public Movement movement;
    public Vector2 moveTargetPos;
    public Entity.Type moveTargetEntType; //used for orbiting TODO refactor to EntPtr
    public int moveTargetEntId; //used for orbiting
    public float moveRelativeDist; //used for orbit radius
    public int warpTargetGridId; //used if movement = ALIGN_FOR_WARP
    public int dockStationId; //used if movement = MOVE_FOR_DOCK TODO refactor to EntPtr

    /* State */

    //meta data
    public int id;
    @Override
    public int getId(){
        return id;
    }
    public final Model model;


    //combat
    public float health;

    //ui and movement
    public String moveCommand;
    public float warpTimeToLand; //0 if not in warp
    public boolean docked;

    //targeting
    public Targeting targetingState;
    public EntPtr targetEntity; //only valid if targetingState != null
    public float targetTimeToLock; //only valid if targetingState != null

    //attachments
    public Weapon[] weapons;


    /**
     * Constructor
     */
    protected Ship(Model model, int id, int grid, int owner, boolean docked){
        //meta data
        this.model = model;
        this.id = id;
        this.grid = grid;
        this.owner = owner;

        //physics
        this.pos = new Vector2(0, 0);
        this.vel = new Vector2(0, 0);
        this.rot = 0;
        this.docked = docked;
        this.polygon = new Polygon(this.model.vertices); //TODO make clientside only

        //command data
        this.moveCommand = "Stationary";
        this.targetingState = null;
        this.targetEntity = null;

        //warping
        this.warpTimeToLand = 0;

        //battle
        this.health = this.model.getMaxHealth();

        //weapons
        this.weapons = new Weapon[model.getWeaponSlots().length];

        //graphics stuff
        polygon = new Polygon(subtype().getVertices());

        //logic stuff
        this.trajectoryVel = new Vector2(0, 0);
        this.movement = Movement.STOPPING;
        this.moveTargetPos = null;
    }


    /* Naming Methods */

    @Override
    public String getFullName(){
        return this.model.name();
    }
    @Override
    public String getFleetName(){
        switch(this.model){
            case Frigate:
                return this.model.name();
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

        health -= amount;

        if(health <= 0){
            health = 0;
        }
    }


    /* Utility Methods */

    @Override
    public Subtype subtype(){
        return model;
    }

    /**
     * Updates the graphics polygon based on current values.
     */
    public void updatePolygon(){
        polygon.setPosition(pos.x, pos.y);
        polygon.setRotation(rot);
    }

    /**
     * Checks if the ship is able to enter warp based on its velocity.
     */
    public boolean alignedForWarp(Grid originGrid, Grid destGrid){

        //check if speed is high enough
        if(vel.len() < 0.75f* model.getMaxSpeed()){
            return false;
        }

        //get the ship's angle
        float gridAngle = (float) Math.atan2(destGrid.pos.y-originGrid.pos.y,
                destGrid.pos.x-originGrid.pos.x);

        //compare the two angles
        return Math.abs(rot - gridAngle) <= 0.01f;
    }

    /**
     * Produces a formatted string for display with this ship's position.
     * @param places Number of places after the decimal to include.
     */
    public float[] roundedPosition(int places){
        float p = (float) Math.pow(10, places);

        return new float[]{
                Math.round(pos.x * p) / p,
                Math.round(pos.y * p) / p,
        };
    }

    /**
     * Gets the rounded speed and angle.
     * @param places Places after the decimal to round to for the speed.
     * @return Angle is in nautical degrees (0 is at N and goes CW).
     */
    public float[] roundedBearing(int places){
        float p = (float) Math.pow(10, places);

        float angle = 90-Math.round(rot * 180/Math.PI);
        if(angle < 0) angle += 360;

        return new float[]{
                Math.round(vel.len() * p) / p,
                angle,
        };
    }


    /* Enums */

    /**
     * Type of ship.
     */
    public enum Model implements Subtype {
        //TODO refactor weapon specs into this enum


        Frigate(
                new float[]{-0.08f,-0.08f,  0.08f,-0.08f,  0,0.08f},
                1.4f*0.113f, 0.8f, 0.4f,
                3.5f, 6,
                new Weapon.Type[]{Weapon.Type.Blaster, Weapon.Type.Blaster}
        ),
        //TODO cruiser specs
        Cruiser(
                null,
                0f, 0f, 0f,
                0f, 0,
                null
        ),
        //TODO battleship specs
        Battleship(
                null,
                0f, 0f, 0f,
                0f, 0,
                null
        ),
        Barge(
                new float[]{-0.1f,-0.12f, 0f,-0.08f,  0.1f,-0.12f,  0.1f,0.12f, -0.1f,0.12f},
                1.4f*0.156f, 0.1f, 0.02f,
                1.5f, 10,
                new Weapon.Type[]{Weapon.Type.StationTransport}
        ),
        //TODO titan specs
        Titan(
                null,
                0f, 0f, 0f,
                0f, 0,
                null
        );

        //data methods from entity
        @Override
        public float[] getVertices(){
            return vertices;
        }
        @Override
        public float getPaddedLogicalRadius() {
            return paddedLogicalRadius;
        }
        @Override
        public String getFilename(){
            return this.name().toLowerCase();
        }

        //data methods for ships
        public float getMaxSpeed() {
            return maxSpeed;
        }
        public float getMaxAccel() {
            return maxAccel;
        }
        public float getTargetRange(){
            return targetRange;
        }
        public int getMaxHealth(){
            return maxHealth;
        }
        public Weapon.Type[] getWeaponSlots(){
            return weaponSlots;
        }

        //data storage
        private final float[] vertices;
        private final float paddedLogicalRadius;
        private final float maxSpeed;
        private final float maxAccel;
        private final float targetRange;
        private final int maxHealth;
        private final Weapon.Type[] weaponSlots;


        /**
         * Constructor
         */
        Model(float[] vertices, float paddedLogicalRadius, float maxSpeed, float maxAccel,
              float targetRange, int maxHealth, Weapon.Type[] weaponSlots){
            this.vertices = vertices;
            this.paddedLogicalRadius = paddedLogicalRadius;
            this.maxSpeed = maxSpeed;
            this.maxAccel = maxAccel;
            this.targetRange = targetRange;
            this.maxHealth = maxHealth;
            this.weaponSlots = weaponSlots;
        }
    }

    /**
     * The type of movement this ship is attempting to do.
     */
    public enum Movement {
        STOPPING,

        MOVE_TO_POS,
        ALIGN_TO_ANG,

        ALIGN_FOR_WARP,
        WARPING,

        ORBIT_ENT,

        MOVE_FOR_DOCK,
    }

    /**
     * The targeting state.
     */
    public enum Targeting {
        Locking,
        Locked,
    }

    /**
     * Kinds of removal.
     */
    public enum Removal {
        EXPLOSION
    }

}
