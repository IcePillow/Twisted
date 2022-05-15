package com.twisted.net.msg.gameUpdate;

import com.twisted.logic.entities.Ship;

public class MAddShip implements MGameUpdate{

    public int grid;
    public Ship ship;

    public MAddShip(int grid, Ship ship) {
        this.grid = grid;
        this.ship = ship;
    }

}
