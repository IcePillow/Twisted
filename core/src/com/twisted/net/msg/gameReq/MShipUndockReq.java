package com.twisted.net.msg.gameReq;

public class MShipUndockReq implements MGameReq{

    public int shipId;
    public int stationId;

    public MShipUndockReq(int shipId, int stationId){
        this.shipId = shipId;
        this.stationId = stationId;
    }

}
