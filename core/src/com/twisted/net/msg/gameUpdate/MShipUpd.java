package com.twisted.net.msg.gameUpdate;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.attach.Weapon;

public class MShipUpd implements MGameUpdate {

    //metadata
    public final int shipId;
    public final int grid; //-1 if in warp

    //state data
    private Vector2 position;
    private Vector2 velocity;
    private float rotation; //stored in degrees

    //command data
    public String moveCommand;

    //targeting
    private float targetTimeToLock;
    private Ship.Targeting targetingState;
    private Entity.Type targetEntityType;
    private int targetEntityId;
    private int targetEntityGrid;

    //weapons
    private boolean[] weaponsActive;

    //warping
    private float warpTimeToLand;

    //combat
    private float health;


    /**
     * Constructor
     */
    private MShipUpd(int shipId, int grid){
        this.shipId = shipId;
        this.grid = grid;
    }


    /* Exterior Facing Methods */

    /**
     * Copies non-meta data to the passed in ship.
     */
    public void copyDataToShip(Ship s){
        //physics
        s.pos = position;
        s.vel = velocity;
        s.rot = rotation;

        s.moveCommand = moveCommand;

        s.warpTimeToLand = warpTimeToLand;

        s.health = health;

        //targeting
        if(targetEntityType != null){
            s.targetTimeToLock = targetTimeToLock;
            s.targetingState = targetingState;
            s.targetEntity = new EntPtr(targetEntityType, targetEntityId, targetEntityGrid);
        }
        else {
            s.targetEntity = null;
            s.targetingState = null;
        }

        //weapons
        for(int i=0; i<s.weapons.length; i++){
            s.weapons[i].active = weaponsActive[i];
        }
    }

    /**
     * Creates a filled out MShipUpd from the passed in ship.
     */
    public static MShipUpd createFromShip(Ship s, int grid){
        MShipUpd upd = new MShipUpd(s.id, grid);

        //physics
        upd.position = s.pos.cpy();
        upd.velocity = s.vel.cpy();
        upd.rotation = s.rot;

        upd.moveCommand = s.moveCommand;

        upd.warpTimeToLand = s.warpTimeToLand;

        upd.health = s.health;

        //targeting
        upd.targetTimeToLock = s.targetTimeToLock;
        upd.targetingState = s.targetingState;
        if(s.targetEntity != null){
            upd.targetEntityType = s.targetEntity.type;
            upd.targetEntityId = s.targetEntity.id;
            upd.targetEntityGrid = s.targetEntity.grid;
        }
        else {
            upd.targetEntityType = null;
            upd.targetingState = null;
        }

        //weapons
        upd.weaponsActive = new boolean[s.weapons.length];
        for(int i=0; i<s.weapons.length; i++){
            upd.weaponsActive[i] = s.weapons[i].active;
        }

        return upd;
    }

}
