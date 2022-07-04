package com.twisted.net.msg.gameReq;

public class MShipAggroReq implements MGameReq {

    public int grid;
    public int shipId;
    public boolean aggro;

    public MShipAggroReq(int grid, int shipId, boolean aggro){
        this.grid = grid;
        this.shipId = shipId;
        this.aggro = aggro;
    }

}
