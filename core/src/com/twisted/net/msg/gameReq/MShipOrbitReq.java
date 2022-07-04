package com.twisted.net.msg.gameReq;

import com.twisted.logic.entities.Entity;

public class MShipOrbitReq implements MGameReq {

    public int grid;
    public int shipId;

    public Entity.Type targetType;
    public int targetId;
    public float radius;

    public MShipOrbitReq(int grid, int shipId, Entity.Type targetType, int targetId, float radius){
        this.grid = grid;
        this.shipId = shipId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.radius = radius;
    }


}
