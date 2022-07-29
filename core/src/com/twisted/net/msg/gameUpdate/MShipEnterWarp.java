package com.twisted.net.msg.gameUpdate;

public class MShipEnterWarp implements MGameUpd {

    public int shipId;
    public int originGridId;
    public int destGridId;


    /**
     * Constructor
     */
    public MShipEnterWarp(int shipId, int originGridId, int destGridId){
        this.shipId = shipId;
        this.originGridId = originGridId;
        this.destGridId = destGridId;
    }

}
