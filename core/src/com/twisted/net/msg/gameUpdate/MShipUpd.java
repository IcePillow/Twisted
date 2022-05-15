package com.twisted.net.msg.gameUpdate;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.entities.Ship;

public class MShipUpd implements MGameUpdate {

    //metadata
    public final int shipId;
    public final int grid;

    //state data
    public Vector2 position;
    public Vector2 velocity;
    public float rotation;

    /**
     * Constructor
     */
    public MShipUpd(int shipId, int grid, Vector2 position, Vector2 velocity, float rotation){
        this.shipId = shipId;
        this.grid = grid;

        this.position = position;
        this.velocity = velocity;
        this.rotation = rotation;
    }


    /* Utility */

    /**
     * Copies non-meta data to the passed in ship.
     */
    public void copyDataToShip(Ship ship){
        ship.position = position;
        ship.velocity = velocity;
        ship.rotation = rotation;
    }

    /**
     * Creates a filled out MShipUpd from the passed in ship.
     */
    public static MShipUpd createFromShip(Ship s, int grid){
        return new MShipUpd(s.shipId, grid, s.position, s.velocity, s.rotation);
    }

}
