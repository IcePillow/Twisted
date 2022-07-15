package com.twisted.net.msg.gameReq;

import com.twisted.logic.entities.Entity;

public class MTargetReq implements MGameReq {

    public int grid;
    public int shipId;
    public Entity.Type targetType;
    public int targetId;

    public MTargetReq(int grid, int shipId, Entity.Type targetType, int targetId){
        //TODO see if this can use EntPtr
        this.grid = grid;
        this.shipId = shipId;
        this.targetType = targetType;
        this.targetId = targetId;
    }

}
