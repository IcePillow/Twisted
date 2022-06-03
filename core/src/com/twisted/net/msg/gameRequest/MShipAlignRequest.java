package com.twisted.net.msg.gameRequest;

public class MShipAlignRequest implements MGameRequest {

    public int grid;
    public int shipId;
    /**
     * Angle in radians (from east going ccw).
     */
    public float angle;

    public MShipAlignRequest(int grid, int shipId, float angle){
        this.grid = grid;
        this.shipId = shipId;
        this.angle = angle;
    }

}
