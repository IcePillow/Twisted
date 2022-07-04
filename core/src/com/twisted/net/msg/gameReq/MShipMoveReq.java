package com.twisted.net.msg.gameReq;

import com.badlogic.gdx.math.Vector2;

public class MShipMoveReq implements MGameReq {

    public int grid;
    public int shipId;
    public Vector2 location;

    public MShipMoveReq(int grid, int shipId, Vector2 location){
        this.grid = grid;
        this.shipId = shipId;
        this.location = location.cpy();
    }


}
