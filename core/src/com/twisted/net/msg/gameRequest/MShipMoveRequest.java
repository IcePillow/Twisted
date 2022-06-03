package com.twisted.net.msg.gameRequest;

import com.badlogic.gdx.math.Vector2;

public class MShipMoveRequest implements MGameRequest {

    public int grid;
    public int shipId;
    public Vector2 location;

    public MShipMoveRequest(int grid, int shipId, Vector2 location){
        this.grid = grid;
        this.shipId = shipId;
        this.location = location.cpy();
    }


}
