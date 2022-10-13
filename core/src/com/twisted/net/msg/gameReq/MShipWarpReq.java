package com.twisted.net.msg.gameReq;

import com.twisted.logic.descriptors.EntPtr;

public class MShipWarpReq implements MGameReq {

    public int grid;
    public int shipId;

    public EntPtr beacon;


    public MShipWarpReq(int grid, int shipId, EntPtr beacon){
        this.grid = grid;
        this.shipId = shipId;
        this.beacon = beacon;
    }

}
