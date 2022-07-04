package com.twisted.net.msg.gameReq;

public class MShipAlignReq implements MGameReq {

    public int grid;
    public int shipId;
    /**
     * Angle in radians (from east going ccw).
     */
    public float angle;

    public MShipAlignReq(int grid, int shipId, float angle){
        this.grid = grid;
        this.shipId = shipId;
        this.angle = angle;
    }

}
