package com.twisted.net.msg.gameUpdate;

import com.badlogic.gdx.math.Vector2;
import com.twisted.local.game.cosmetic.CosEnteringWarp;
import com.twisted.local.game.cosmetic.CosExitingWarp;
import com.twisted.local.game.cosmetic.Cosmetic;
import com.twisted.local.game.state.ClientGameState;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.net.msg.CosmeticCheckable;

import java.util.ArrayList;
import java.util.List;

public class MShipEnterWarp implements MGameUpd, CosmeticCheckable {

    public int shipId;
    public int originGridId;
    public int destGridId;
    public Vector2 warpLandPos;


    /**
     * Constructor
     */
    public MShipEnterWarp(int shipId, int originGridId, int destGridId, Vector2 warpLandPos){
        this.shipId = shipId;
        this.originGridId = originGridId;
        this.destGridId = destGridId;
        this.warpLandPos = warpLandPos.cpy();
    }


    @Override
    public List<Cosmetic> createNewCosmetics(ClientGameState state) {
        List<Cosmetic> list = new ArrayList<>();
        Ship s = (Ship) state.findEntity(originGridId, Entity.Type.Ship, shipId, false);

        //warp direction
        Vector2 warpDir = new Vector2(
                state.grids[destGridId].loc.x-state.grids[originGridId].loc.x,
                state.grids[destGridId].loc.y-state.grids[originGridId].loc.y
        ).nor();

        //entering warp
        list.add(new CosEnteringWarp(originGridId, state.grids[originGridId].radius, state.grids[originGridId].loc,
                s, warpDir, state.findPaintCollectForOwner(s.owner).dimmed.c));
        //exiting warp
        list.add(new CosExitingWarp(destGridId, state.grids[destGridId].radius, state.grids[destGridId].loc,
                s, warpDir, state.findPaintCollectForOwner(s.owner).dimmed.c));

        return list;
    }
}
