package com.twisted.net.msg.gameReq;

public class MShipWarpReq implements MGameReq {

    public int grid;
    public int shipId;

    public int targetGridId; //the grid to be warped to


    public MShipWarpReq(int grid, int shipId, int targetGridId){
        this.grid = grid;
        this.shipId = shipId;
        this.targetGridId = targetGridId;
    }

}
