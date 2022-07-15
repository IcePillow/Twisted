package com.twisted.net.msg.gameUpdate;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.entities.Frigate;
import com.twisted.logic.entities.Ship;

public class MAddShip implements MGameUpdate{

    //meta data
    public Ship.Type type;
    public int grid;
    public int shipId;

    //data
    private int ownerId;
    private Vector2 position;
    private Vector2 velocity;
    private float rotation;
    private float warpTimeToLand;

    /**
     * Constructor
     */
    private MAddShip(Ship.Type type, int grid, int shipId){
        this.type = type;
        this.grid = grid;
        this.shipId = shipId;
    }

    /**
     * Creates an MAddShip from a ship with a body.
     */
    public static MAddShip createFromShipBody(int grid, Ship s){
        MAddShip m = new MAddShip(s.getType(), grid, s.id);

        m.ownerId = s.owner;
        m.position = s.pos.cpy();
        m.velocity = s.vel.cpy();
        m.rotation = s.rot;
        m.warpTimeToLand = s.warpTimeToLand;

        return m;
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
