package com.twisted.net.msg.gameUpdate;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.entities.Frigate;
import com.twisted.logic.entities.Ship;

public class MAddShip implements MGameUpd {

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
    private boolean docked;

    /**
     * Constructor
     */
    private MAddShip(Ship.Type type, int shipId){
        this.type = type;
        this.shipId = shipId;
    }

    /**
     * Creates an MAddShip from a ship with a body.
     */
    public static MAddShip createFromShipBody(Ship s){
        MAddShip m = new MAddShip(s.getType(), s.id);

        m.grid = s.grid;
        m.ownerId = s.owner;
        m.position = s.pos.cpy();
        m.velocity = s.vel.cpy();
        m.rotation = s.rot;
        m.warpTimeToLand = s.warpTimeToLand;
        m.docked = s.docked;

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
                s = new Frigate(shipId, grid, ownerId, position, velocity, rotation, warpTimeToLand, docked);
                break;
            default:
                System.out.println("Unexpected ship type in MAddShip.createDrawableShip()");
        }

        return s;
    }

}
