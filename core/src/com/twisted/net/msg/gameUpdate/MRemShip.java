package com.twisted.net.msg.gameUpdate;

import com.twisted.local.game.cosmetic.Cosmetic;
import com.twisted.local.game.cosmetic.CosExplosion;
import com.twisted.local.game.state.ClientGameState;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.net.msg.CosmeticCheckable;

import java.util.ArrayList;
import java.util.List;

/**
 * Removing a ship.
 */
public class MRemShip implements MGameUpd, CosmeticCheckable {

    public int shipId;
    public int grid;
    public boolean docked;

    public Ship.Removal removal;

    public MRemShip(int shipId, int grid, Ship.Removal removal, boolean docked){
        this.shipId = shipId;
        this.grid = grid;
        this.docked = docked;

        this.removal = removal;
    }

    @Override
    public List<Cosmetic> createNewCosmetics(ClientGameState state) {
        List<Cosmetic> list = null;

        if(this.grid != -1 && !this.docked){
            list = new ArrayList<>();
            Ship s = (Ship) state.findEntity(grid, Entity.Type.Ship, shipId, docked);

            CosExplosion c = new CosExplosion(this.grid, s.entityModel().getPaddedLogicalRadius(),
                    1f, s.pos, state.findBaseColorForOwner(s.owner));
            list.add(c);
        }

        return list;
    }
}
