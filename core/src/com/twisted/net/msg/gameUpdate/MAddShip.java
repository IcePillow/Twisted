package com.twisted.net.msg.gameUpdate;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.entities.Frigate;
import com.twisted.logic.entities.Ship;

public class MAddShip implements MGameUpdate{

    public Ship.Type type;
    public int grid;
    public int shipId;
    public int ownerId;

    public Vector2 position;
    public Vector2 velocity;
    public float rotation;
    public float warpTimeToLand;


    /**
     * Constructor
     */
    public MAddShip(Ship.Type type, int grid, int shipId, int ownerId, Vector2 position,
                    Vector2 velocity, float rotation, float warpTimeToLand) {
        this.type = type;

        this.grid = grid;
        this.shipId = shipId;
        this.ownerId = ownerId;

        this.position = position.cpy();
        this.velocity = velocity.cpy();
        this.rotation = rotation;
        this.warpTimeToLand = warpTimeToLand;
    }

    /**
     * Creates an MAddShip from a ship with a body.
     */
    public static MAddShip createFromShipBody(int grid, Ship s){
        return new MAddShip(s.getType(), grid, s.id, s.owner, s.pos,
                s.vel, s.rot, s.warpTimeToLand);
    }

    /**
     * Creates a ship based on the contained data.
     */
    public Ship createDrawableShip(){

        //create the object
        Ship s = null;
        switch(type){
            case Frigate:
                s = new Frigate(shipId, ownerId, position, velocity, rotation, warpTimeToLand);
                break;
            default:
                System.out.println("Unexpected ship type in MAddShip.createDrawableShip()");
        }

        return s;
    }

}
