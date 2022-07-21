package com.twisted.net.msg.gameUpdate;

import com.twisted.logic.entities.Ship;

/**
 * Removing a ship.
 */
public class MRemShip implements MGameUpdate {

    public int shipId;
    public int grid;
    public boolean docked;

    public Ship.Removal removal;

    public MRemShip(int shipId, int grid, Ship.Removal removal, boolean docked){
        this.shipId = shipId;
        this.grid = grid;
        this.docked = docked;

        this.removal = removal;
    }

}
