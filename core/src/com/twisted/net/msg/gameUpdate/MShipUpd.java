package com.twisted.net.msg.gameUpdate;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.Station;
import com.twisted.logic.entities.attach.StationTransport;

public class MShipUpd implements MGameUpd {

    //metadata
    public int shipId;

    //state data
    public int grid; //-1 if in warp
    public boolean docked;
    private Vector2 position;
    private Vector2 velocity;
    private float rotation; //stored in degrees

    //command data
    public String moveCommand;

    //targeting
    private Ship.Targeting targetingState;
    private Entity.Type targetEntityType;
    private float targetTimeToLock;
    private int targetEntityId;
    private int targetEntityGrid;

    //weapons
    private boolean[] weaponsActive;
    private float[] weaponsTimers;
    private Station.Model[] weaponsCargo; //only used if a weapon StationTransport

    //warping
    private float warpTimeToLand;

    //combat
    private float health;


    /* Exterior Facing Methods */

    /**
     * Copies non-meta data to the passed in ship.
     */
    public void copyDataToShip(Ship s){
        //physics
        s.grid = grid;
        s.docked = docked;
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
            s.targetEntity = new EntPtr(targetEntityType, targetEntityId, targetEntityGrid, false);
        }
        else {
            s.targetEntity = null;
            s.targetingState = null;
        }

        //weapons
        for(int i=0; i<s.weapons.length; i++){
            s.weapons[i].active = weaponsActive[i];
            s.weapons[i].timer = weaponsTimers[i];

            if(s.weapons[i] instanceof StationTransport){
                ((StationTransport) s.weapons[i]).cargo = weaponsCargo[i];
            }
        }
    }

    /**
     * Creates a filled out MShipUpd from the passed in ship.
     */
    public static MShipUpd createFromShip(Ship s){
        MShipUpd upd = new MShipUpd();

        //id
        upd.shipId = s.id;

        //physics
        upd.grid = s.grid;
        upd.docked = s.docked;
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
        upd.weaponsTimers = new float[s.weapons.length];
        upd.weaponsCargo = new Station.Model[s.weapons.length];
        for(int i=0; i<s.weapons.length; i++){
            upd.weaponsActive[i] = s.weapons[i].active;
            upd.weaponsTimers[i] = s.weapons[i].timer;

            if(s.weapons[i] instanceof StationTransport) {
                upd.weaponsCargo[i] = ((StationTransport) s.weapons[i]).cargo;
            }
        }

        return upd;
    }

}
