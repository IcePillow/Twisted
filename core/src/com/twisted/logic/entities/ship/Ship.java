package com.twisted.logic.entities.ship;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Faction;
import com.twisted.logic.entities.attach.*;
import com.twisted.net.msg.gameUpdate.MAddShip;
import com.twisted.util.Quirk;

public abstract class Ship extends Entity {

    /* Graphics (clientside) */

    public Polygon polygon; //used for click detection

    public boolean exitingWarpCosmeticExists;

    /* Logic (serverside) */

    //trajectory determining
    public Vector2 trajectoryVel;

    //command movement description
    public Movement movement;
    public Vector2 moveTargetPos;
    public EntPtr moveTargetEnt; //used for orbiting
    public float moveRelativeDist; //used for orbit radius

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

    //warping details
    public int warpSourceGrid;
    public Vector2 warpLandPos;
    public int warpDestGrid;

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
        this.polygon = new Polygon(this.model.vertices);

        //command data
        this.moveDescription = "Stationary";

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
                    this.weapons[i] = new Blaster(this,
                            new Vector2(model.weaponMounts[2*i], model.weaponMounts[2*i+1]),
                            (Blaster.Model) model.weapons[i]);
                    break;
                case Laser:
                    this.weapons[i] = new Laser(this,
                            new Vector2(model.weaponMounts[2*i], model.weaponMounts[2*i+1]),
                            (Laser.Model) model.weapons[i]);
                    break;
                case StationTrans:
                    this.weapons[i] = new StationTrans(this,
                            new Vector2(model.weaponMounts[2*i], model.weaponMounts[2*i+1]),
                            (StationTrans.Model) model.weapons[i]);
                    break;
                case Beacon:
                    this.weapons[i] = new Beacon(this,
                            new Vector2(model.weaponMounts[2*i], model.weaponMounts[2*i+1]),
                            (Beacon.Model) model.weapons[i]);
                    break;
                case Doomsday:
                    this.weapons[i] = new Doomsday(this,
                            new Vector2(model.weaponMounts[2*i], model.weaponMounts[2*i+1]),
                            (Doomsday.Model) model.weapons[i]);
                    break;
                default:
                    new Quirk(Quirk.Q.UnknownGameData).print();
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
                new Quirk(Quirk.Q.UnknownGameData).print();
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
        //TODO see if this needs to be changed for any of the ship names
        return this.model.name();
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
                angle
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


    /* State Methods */

    @Override
    public boolean isDocked(){
        return docked;
    }
    @Override
    public boolean isValidBeacon(){
        for(Weapon w : weapons){
            if(w.getType() == Weapon.Type.Beacon && w.isActive()) return true;
        }
        return false;
    }
    @Override
    public float getSigRadius(){
        return model.tier.sigRadius;
    }
    @Override
    public boolean isShowingThroughFog(){
        return isValidBeacon();
    }


    /* Enums */

    public enum Tier implements Entity.Tier {
        Frigate(4, 4),
        Cruiser(6, 2),
        Battleship(11, 1),
        Barge(12, 0.05f),
        Titan(20, 0.1f);

        //data methods
        @Override
        public String getFilename(){
            return this.name().toLowerCase();
        }

        //data storage
        public final float sigRadius;
        public final float dockedRegenMult;

        /**
         * Constructor
         */
        Tier(float sigRadius, float dockedRegenMult){
            this.sigRadius = sigRadius;
            this.dockedRegenMult = dockedRegenMult;
        }
    }
    public enum Model implements Entity.Model {
        Sparrow(Tier.Frigate, Faction.Federation,
                new float[]{-0.06f,-0.06f,  0,-0.03f,  0.06f,-0.06f,  0,0.06f},
                1.4f*0.0849f, 0.9f, 0.4f, 3,
                new Weapon.Model[]{Blaster.Model.Small, Blaster.Model.Small},
                new float[]{-0.03f,0, 0.03f,0},
                200, 2, new Vector2(0, -0.06f)
        ),
        Alke(Tier.Frigate, Faction.Republic,
                new float[]{-0.06f,-0.06f,  0,-0.03f,  0.06f,-0.06f,  0,0.06f},
                1.4f*0.0849f, 0.8f, 0.4f, 6,
                new Weapon.Model[]{Blaster.Model.Small, Blaster.Model.Small},
                new float[]{-0.03f,0, 0.03f,0},
                200, 2, new Vector2(0, -0.06f)
        ),
        Helios(Tier.Cruiser, Faction.Republic,
                new float[]{0,0.15f,  0.12f,-0.02f,  0,-0.12f,  -0.12f,-0.02f},
                1.3f*0.15f, 0.4f, 0.2f, 10,
                new Weapon.Model[]{Blaster.Model.Medium, Blaster.Model.Medium, Blaster.Model.Medium},
                new float[]{-0.6f,0.65f, 0,0.15f, 0.6f,0.65f},
                100, 4, new Vector2(0, -0.16f)
        ),
        Themis(Tier.Battleship, Faction.Republic,
                new float[]{0,0.252f,  0.144f,-0.036f,  0.096f,-0.096f,  0.144f,-0.144f,  0.048f,-0.144f,
                            0,-0.204f,  -0.048f,-0.144f,  -0.144f,-0.144f,  -0.096f,-0.096f,  -0.144f,-0.036f},
                1.2f*0.25f, 0.18f, 0.1f, 16,
                new Weapon.Model[]{Blaster.Model.Large, Laser.Model.Large, Beacon.Model.Medium},
                new float[]{-0.072f,0.108f, 0,0.252f, 0,0.252f},
                50, 10, new Vector2(0, -0.234f)
        ),
        Heron(Tier.Barge, Faction.Federation,
                new float[]{-0.16f,-0.2f,  0f,-0.12f,  0.16f,-0.2f,
                            0.16f,0.14f,  0.12f,0.18f,  -0.12f,0.18f,  -0.16f,0.14f},
                1.2f*0.25f, 0.1f, 0.02f, 10,
                new Weapon.Model[]{StationTrans.Model.Medium},
                new float[]{0,0.18f},
                40, 20, new Vector2(0, -0.16f)
        ),
        Nyx(Tier.Titan, Faction.Republic,
                new float[]{-0.2f,-0.45f,  -0.075f,-0.45f,  0f,-0.5f,  0.075f,-0.45f,  0.2f,-0.45f,  //base
                            0.4f,-0.2f,  0.4f,0.25f,  0.15f,0.4f,  0.25f,0.2f,  0.25f,-0.05f,  //right
                            0.1f,-0.2f, 0.1f,0.15f,  0,0.25f,  -0.1f,0.15f,  -0.1f,-0.2f,  //center
                            -0.25f,-0.05f,  -0.25f,0.2f,  -0.15f,0.4f,  -0.4f,0.25f,  -0.4f,-0.2f, //left
                },
                1.1f*0.5f, 0.06f, 0.02f,20,
                new Weapon.Model[]{Doomsday.Model.Capital},
                new float[]{0,0.34f},
                10, 60, new Vector2(0, -0.53f)
        );

        //data methods from entity
        @Override
        public float[] getVertices(){
            return vertices;
        }
        @Override
        public float getLogicalRadius(){
            return logicalRadius;
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
        public final float logicalRadius, paddedLogicalRadius;
        public final float maxSpeed, maxAccel;
        public final int maxHealth;
        public final Weapon.Model[] weapons;
        private final float[] weaponMounts;
        public final float warpSpeed, warpChargeTime;
        public final Vector2 warpSource;

        //tier reflection
        public float getDockedRegenMult(){
            return this.tier.dockedRegenMult;
        }

        /**
         * Constructor
         * @param vertices This is in visual coordinates where 0 is +y.
         * @param weaponMounts This is in visual coordinates where 0 is +y.
         */
        Model(Tier tier, Faction faction, float[] vertices, float paddedLogicalRadius, float maxSpeed,
              float maxAccel, int maxHealth, Weapon.Model[] weapons, float[] weaponMounts,
              float warpSpeed, float warpChargeTime, Vector2 warpSource){
            //copy
            this.tier = tier;
            this.faction = faction;
            this.vertices = vertices;
            this.paddedLogicalRadius = paddedLogicalRadius;
            this.maxSpeed = maxSpeed;
            this.maxAccel = maxAccel;
            this.maxHealth = maxHealth;
            this.weapons = weapons;
            this.weaponMounts = weaponMounts;
            this.warpSpeed = warpSpeed;
            this.warpChargeTime = warpChargeTime;
            this.warpSource = warpSource;

            //calculate
            float r=0;
            float t;
            for(int i=0; i<vertices.length; i+=2){
                t = (float) Math.sqrt(vertices[i]*vertices[i] + vertices[i+1]*vertices[i+1]);
                if (t > r) r = t;
            }
            this.logicalRadius = r;
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
