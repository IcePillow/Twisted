package com.twisted.logic.entities.ship;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Faction;
import com.twisted.logic.entities.attach.*;
import com.twisted.net.msg.gameUpdate.MAddShip;

public abstract class Ship extends Entity {

    /* Graphics (clientside) */

    public Polygon polygon; //used for click detection

    /* Logic (serverside) */

    //trajectory determining
    public Vector2 trajectoryVel;

    //command movement description
    public Movement movement;
    public Vector2 moveTargetPos;
    public EntPtr moveTargetEnt; //used for orbiting
    public float moveRelativeDist; //used for orbit radius

    //warping
    public int warpSourceGrid;
    public int warpDestGrid;
    public Vector2 warpLandPos;

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
    public String moveDescription;
    public boolean docked;

    //warping
    public Warping warping;
    public float warpCharge; //meaningless unless warping==Charging, should be [0, 1]
    public Vector2 warpPos; //meaningless unless warping==InWarp
    public EntPtr warpTarget;

    //targeting
    public Targeting targetingState;
    public EntPtr targetEntity; //only valid if targetingState != null
    public float targetTimeToLock; //only valid if targetingState != null

    //attachments
    public Weapon[] weapons;


    /* Constructing */

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
        this.moveDescription = "Stationary";
        this.targetingState = null;
        this.targetEntity = null;

        //warping
        this.warping = Warping.None;
        this.warpCharge = 0;
        this.warpPos = new Vector2(-1, -1);
        this.warpTarget = null;
        this.warpLandPos = new Vector2();

        //battle
        this.health = this.model.maxHealth;

        //weapons
        this.weapons = new Weapon[model.weapons.length];
        for(int i=0; i<this.weapons.length; i++){
            switch(model.weapons[i].getType()){
                case Blaster:
                    this.weapons[i] = new Blaster(this, (Blaster.Model) model.weapons[i]);
                    break;
                case Laser:
                    this.weapons[i] = new Laser(this, (Laser.Model) model.weapons[i]);
                    break;
                case StationTrans:
                    this.weapons[i] = new StationTrans(this, (StationTrans.Model) model.weapons[i]);
                    break;
                case Beacon:
                    this.weapons[i] = new Beacon(this, (Beacon.Model) model.weapons[i]);
                    break;
                default:
                    System.out.println("Unknown weapon type");
                    new Exception().printStackTrace();
            }
        }

        //graphics stuff
        polygon = new Polygon(entityModel().getVertices());

        //logic stuff
        this.trajectoryVel = new Vector2(0, 0);
        this.movement = Movement.STOPPING;
        this.moveTargetPos = null;
    }

    public static Ship createFromMAddShip(MAddShip msg){
        switch(msg.model.tier){
            case Frigate:
                return new Frigate(msg.model, msg.shipId, msg.grid, msg.ownerId, msg.docked);
            case Cruiser:
                return new Cruiser(msg.model, msg.shipId, msg.grid, msg.ownerId, msg.docked);
            case Barge:
                return new Barge(msg.model, msg.shipId, msg.grid, msg.ownerId, msg.docked);
            case Battleship:
                return new Battleship(msg.model, msg.shipId, msg.grid, msg.ownerId, msg.docked);
            case Titan:
                return new Titan(msg.model, msg.shipId, msg.grid, msg.ownerId, msg.docked);
            default:
                System.out.println("Unexpected ship tier");
                new Exception().printStackTrace();
                return null;
        }
    }


    /* Naming Methods */

    @Override
    public String getFullName(){
        return this.model.name();
    }
    @Override
    public String getFleetName(){
        switch(this.model){
            case Alke:
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

    /**
     * Called clientside when a ship is destroyed to set all its values to default ones.
     */
    public void cleanupForClientsideRemoval(){
        targetingState = null;

        for(Weapon w : weapons){
            w.deactivate();
        }
    }


    /* Utility Methods */

    @Override
    public Entity.Model entityModel(){
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
     * Produces a formatted string for display with this ship's position.
     * @param places Number of places after the decimal to include.
     */
    public float[] roundedPos(int places){
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
    public float[] roundedBear(int places){
        float p = (float) Math.pow(10, places);

        float angle = 90-Math.round(rot * 180/Math.PI);
        if(angle < 0) angle += 360;

        return new float[]{
                Math.round(vel.len() * p) / p,
                angle,
        };
    }
    public float[] roundedWarpPos(int places){
        float p = (float) Math.pow(10, places);

        return new float[]{
               Math.round(warpPos.x * p)/p,
               Math.round(warpPos.y * p)/p
        };
    }
    public float[] roundedWarpBear(int places){
        float p = (float) Math.pow(10, places);

        float angle = 90-Math.round(rot * 180/Math.PI);
        if(angle < 0) angle += 360;

        return new float[]{
                Math.round(model.warpSpeed * p) / p,
                angle,
        };
    }

    public int countActiveWeapons(){
        int ct = 0;
        for(Weapon w : weapons){
            if(w.isActive()) ct++;
        }
        return ct;
    }


    /* Enums */

    public enum Tier implements Entity.Tier {
        Frigate,
        Cruiser,
        Battleship,
        Barge,
        Titan;

        @Override
        public String getFilename(){
            return this.name().toLowerCase();
        }
    }
    public enum Model implements Entity.Model {
        Sparrow(Tier.Frigate, Faction.Federation,
                new float[]{-0.06f,-0.06f,  0,-0.03f,  0.06f,-0.06f,  0,0.06f},
                1.4f*0.0849f, 0.9f, 0.4f, 4f, 3,
                new Weapon.Model[]{Blaster.Model.Small, Blaster.Model.Small},
                200, 2
        ),
        Alke(Tier.Frigate, Faction.Republic,
                new float[]{-0.06f,-0.06f,  0,-0.03f,  0.06f,-0.06f,  0,0.06f},
                1.4f*0.0849f, 0.8f, 0.4f, 3.5f, 6,
                new Weapon.Model[]{Blaster.Model.Small, Blaster.Model.Small},
                200, 2
        ),
        Helios(Tier.Cruiser, Faction.Republic,
                new float[]{0,0.15f,  0.12f,-0.02f,  0,-0.12f,  -0.12f,-0.02f},
                1.3f*0.15f, 0.4f, 0.2f, 5f, 10,
                new Weapon.Model[]{Blaster.Model.Medium, Blaster.Model.Medium, Blaster.Model.Medium},
                100, 4
        ),
        Themis(Tier.Battleship, Faction.Republic,
                new float[]{0,0.252f,  0.144f,-0.036f,  0.096f,-0.096f,  0.144f,-0.144f,  0.048f,-0.144f,
                            0,-0.204f,  -0.048f,-0.144f,  -0.144f,-0.144f,  -0.096f,-0.096f,  -0.144f,-0.036f},
                1.2f*0.25f, 0.18f, 0.1f, 8f, 16,
                new Weapon.Model[]{Blaster.Model.Large, Laser.Model.Large, Beacon.Model.Medium},
                50, 10
        ),
        Heron(Tier.Barge, Faction.Federation,
                new float[]{-0.16f,-0.2f,  0f,-0.12f,  0.16f,-0.2f,
                            0.16f,0.14f,  0.12f,0.18f,  -0.12f,0.18f,  -0.16f,0.14f},
                1.2f*0.25f, 0.1f, 0.02f,
                1.5f, 10,
                new Weapon.Model[]{StationTrans.Model.Medium},
                40, 20
        ),
        Nyx(Tier.Titan, Faction.Republic,
                new float[]{-0.2f,-0.45f,  -0.075f,-0.45f,  0f,-0.5f,  0.075f,-0.45f,  0.2f,-0.45f,  //base
                            0.4f,-0.2f,  0.4f,0.25f,  0.15f,0.4f,  0.25f,0.2f,  0.25f,-0.05f,  //right
                            0.1f,-0.2f, 0.1f,0.15f,  0,0.25f,  -0.1f,0.15f,  -0.1f,-0.2f,  //center
                            -0.25f,-0.05f,  -0.25f,0.2f,  -0.15f,0.4f,  -0.4f,0.25f,  -0.4f,-0.2f, //left
                },
                1.1f*0.5f, 0.06f, 0.02f, 12f, 20,
                new Weapon.Model[]{}, //TODO titan weapons
                10, 60
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
        @Override
        public Entity.Tier getTier(){
            return tier;
        }

        //data storage
        public final Tier tier;
        public final Faction faction;
        public final float[] vertices;
        public final float paddedLogicalRadius;
        public final float maxSpeed;
        public final float maxAccel;
        public final float targetRange;
        public final int maxHealth;
        public final Weapon.Model[] weapons;
        public final boolean canLightBeacon;
        public final float warpSpeed;
        public final float warpChargeTime;

        /**
         * Constructor
         */
        Model(Tier tier, Faction faction, float[] vertices, float paddedLogicalRadius, float maxSpeed,
              float maxAccel, float targetRange, int maxHealth, Weapon.Model[] weapons, float warpSpeed,
              float warpChargeTime){
            //copy
            this.tier = tier;
            this.faction = faction;
            this.vertices = vertices;
            this.paddedLogicalRadius = paddedLogicalRadius;
            this.maxSpeed = maxSpeed;
            this.maxAccel = maxAccel;
            this.targetRange = targetRange;
            this.maxHealth = maxHealth;
            this.weapons = weapons;
            this.warpSpeed = warpSpeed;
            this.warpChargeTime = warpChargeTime;

            //determine
            canLightBeacon = (tier==Tier.Cruiser || tier==Tier.Battleship || tier==Tier.Titan);
        }
    }

    /**
     * The type of movement this ship is attempting to do.
     */
    public enum Movement {
        STOPPING,

        MOVE_TO_POS,
        ALIGN_TO_ANG,

        PREP_FOR_WARP,
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

    /**
     * The state of warping.
     */
    public enum Warping {
        None,
        Charging,
        InWarp,
    }

}
