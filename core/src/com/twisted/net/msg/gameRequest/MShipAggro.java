package com.twisted.net.msg.gameRequest;

public class MShipAggro implements MGameRequest{

    public int grid;
    public int shipId;
    public boolean aggro;

    public MShipAggro(int grid, int shipId, boolean aggro){
        this.grid = grid;
        this.shipId = shipId;
        this.aggro = aggro;
    }

}
