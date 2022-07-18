package com.twisted.net.msg.gameUpdate;

public class MUndockShip implements MGameUpdate{

    public int shipId;
    public int grid;

    public MShipUpd update;


    public MUndockShip(int shipId, int grid, MShipUpd update){
        this.shipId = shipId;
        this.grid = grid;
        this.update = update;
    }

}
