package com.twisted.net.msg.gameUpdate;

public class MShipExitWarp implements MGameUpdate  {

    public int shipId;
    public int destGridId;

    public MShipExitWarp(int shipId, int destGridId){
        this.shipId = shipId;
        this.destGridId = destGridId;
    }

}
