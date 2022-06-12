package com.twisted.net.msg.gameRequest;

public class MShipWarpRequest implements MGameRequest{

    public int grid;
    public int shipId;

    public int targetGridId; //the grid to be warped to


    public MShipWarpRequest(int grid, int shipId, int targetGridId){
        this.grid = grid;
        this.shipId = shipId;
        this.targetGridId = targetGridId;
    }

}
