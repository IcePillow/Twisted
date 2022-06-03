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
    /**
     * Stored in degrees.
     */
    public float rotation;

    //command data
    public String moveCommand;


    /**
     * Constructor
     * @param rotation in degrees
     */
    public MShipUpd(int shipId, int grid, Vector2 position, Vector2 velocity, float rotation,
                    String moveCommand){
        this.shipId = shipId;
        this.grid = grid;

        this.position = position.cpy();
        this.velocity = velocity.cpy();
        this.rotation = rotation;
        this.moveCommand = moveCommand;
    }


    /* Utility */

    /**
     * Copies non-meta data to the passed in ship.
     */
    public void copyDataToShip(Ship ship){
        ship.position = position;
        ship.velocity = velocity;
        ship.rotation = rotation;
        ship.moveCommand = moveCommand;
    }

    /**
     * Creates a filled out MShipUpd from the passed in ship.
     */
    public static MShipUpd createFromShip(Ship s, int grid){
        return new MShipUpd(s.id, grid, s.position, s.velocity, s.rotation, s.moveCommand);
    }

}
