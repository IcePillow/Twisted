package com.twisted.net.msg.gameUpdate;

import com.twisted.logic.entities.ship.Barge;
import com.twisted.logic.entities.ship.Frigate;
import com.twisted.logic.entities.ship.Ship;

public class MAddShip implements MGameUpd {

    //meta data
    public Ship.Model model;
    public int grid;
    public int shipId;

    //data
    public int ownerId;
    public boolean docked;

    /**
     * Constructor
     */
    private MAddShip(Ship.Model model, int shipId){
        this.model = model;
        this.shipId = shipId;
    }

    /**
     * Creates an MAddShip from a ship with a body.
     */
    public static MAddShip createFromShipBody(Ship s){
        MAddShip m = new MAddShip(s.model, s.id);

        m.grid = s.grid;
        m.ownerId = s.owner;
        m.docked = s.docked;

        return m;
    }

}
