package com.twisted.net.msg.gameUpdate;

public class MShipDockingChange implements MGameUpd {

    public int shipId;
    public int grid;
    public boolean docked;

    public MShipUpd update;


    public MShipDockingChange(int shipId, int grid, boolean docked, MShipUpd update){
        this.shipId = shipId;
        this.grid = grid;
        this.docked = docked;
        this.update = update;
    }

}
