package com.twisted.net.msg.gameReq;

public class MShipDockReq implements MGameReq{

    public int shipId;
    public int grid;

    public MShipDockReq(int shipId, int grid){

        this.shipId = shipId;
        this.grid = grid;

    }

}
