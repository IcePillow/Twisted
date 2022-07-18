package com.twisted.net.msg.gameReq;

public class MShipStopReq implements MGameReq {

    public final int shipId;
    public final int grid;

    public MShipStopReq(int shipId, int grid){
        this.shipId = shipId;
        this.grid = grid;
    }

}
