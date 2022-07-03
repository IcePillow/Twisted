package com.twisted.net.msg.gameUpdate;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;

public class MShipUpd implements MGameUpdate {

    //metadata
    public final int shipId;
    public final int grid; //-1 if in warp

    //state data
    public Vector2 position;
    public Vector2 velocity;
    /**
     * Stored in degrees.
     */
    public float rotation;

    //command data
    public String moveCommand;
    public boolean aggro;

    //targeting
    public float targetTimeToLock;
    public Ship.Targeting targetingState;
    public Entity.Type targetingType;
    public int targetingId;

    //warping
    public float warpTimeToLand;

    //combat
    public float health;


    /**
     * Constructor
     */
    private MShipUpd(int shipId, int grid){
        this.shipId = shipId;
        this.grid = grid;
    }


    /* Utility */

    /**
     * Copies non-meta data to the passed in ship.
     */
    public void copyDataToShip(Ship s){
        s.pos = position;
        s.vel = velocity;
        s.rot = rotation;

        s.moveCommand = moveCommand;
        s.aggro = aggro;

        s.warpTimeToLand = warpTimeToLand;

        s.targetTimeToLock = targetTimeToLock;
        s.targetingState = targetingState;
        s.targetingType = targetingType;
        s.targetingId = targetingId;

        s.health = health;
    }

    /**
     * Creates a filled out MShipUpd from the passed in ship.
     */
    public static MShipUpd createFromShip(Ship s, int grid){
        MShipUpd upd = new MShipUpd(s.id, grid);

        upd.position = s.pos.cpy();
        upd.velocity = s.vel.cpy();
        upd.rotation = s.rot;

        upd.moveCommand = s.moveCommand;
        upd.aggro = s.aggro;

        upd.warpTimeToLand = s.warpTimeToLand;

        upd.targetTimeToLock = s.targetTimeToLock;
        upd.targetingState = s.targetingState;
        upd.targetingType = s.targetingType;
        upd.targetingId = s.targetingId;

        upd.health = s.health;

        return upd;
    }

}
