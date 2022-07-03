package com.twisted.net.msg.gameRequest;

import com.twisted.logic.entities.Entity;

public class MTargetRequest implements MGameRequest {

    public int grid;
    public int shipId;
    public Entity.Type targetType;
    public int targetId;

    public MTargetRequest(int grid, int shipId, Entity.Type targetType, int targetId){
        this.grid = grid;
        this.shipId = shipId;
        this.targetType = targetType;
        this.targetId = targetId;
    }

}
