package com.twisted.net.msg.gameUpdate;

import com.badlogic.gdx.math.Vector2;
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

    //warping
    public float warpTimeToLand;


    /**
     * Constructor
     * @param rotation in degrees
     */
    public MShipUpd(int shipId, int grid, Vector2 position, Vector2 velocity, float rotation,
                    String moveCommand, float warpTimeToLand){
        this.shipId = shipId;
        this.grid = grid;

        this.position = position.cpy();
        this.velocity = velocity.cpy();
        this.rotation = rotation;
        this.moveCommand = moveCommand;
        this.warpTimeToLand = warpTimeToLand;
    }


    /* Utility */

    /**
     * Copies non-meta data to the passed in ship.
     */
    public void copyDataToShip(Ship ship){
        ship.pos = position;
        ship.vel = velocity;
        ship.rot = rotation;
        ship.moveCommand = moveCommand;
        ship.warpTimeToLand = warpTimeToLand;
    }

    /**
     * Creates a filled out MShipUpd from the passed in ship.
     */
    public static MShipUpd createFromShip(Ship s, int grid){
        return new MShipUpd(s.id, grid, s.pos, s.vel, s.rot, s.moveCommand,
                s.warpTimeToLand);
    }

}
