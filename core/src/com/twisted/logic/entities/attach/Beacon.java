package com.twisted.logic.entities.attach;

import com.twisted.Asset;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.host.game.ServerGameState;

public class Beacon extends Weapon{

    public final Model model;

    public Beacon(Ship attached, Model model) {
        super(attached);

        this.model = model;
    }


    /* Action Methods */

    public void tick(ServerGameState state, Grid grid, Ship ship, Entity target,
                              Ship.Targeting targeting, float delta){
        //disable
        if(ship.vel.len2() > 0 || ship.warpCharge > 0){
            active = false;
        }
    }


    /* Type Methods */

    @Override
    public Weapon.Model subtype() {
        return model;
    }


    /* Data Methods */

    @Override
    public Asset.UiButton getOffButtonAsset() {
        return Asset.UiButton.BEACON_OFF;
    }
    @Override
    public Asset.UiButton getOnButtonAsset() {
        return Asset.UiButton.BEACON_ON;
    }
    @Override
    public float getFullTimer() {
        return 0;
    }


    public enum Model implements Weapon.Model {
        Medium;

        @Override
        public Type getType() {
            return Type.Beacon;
        }
        @Override
        public float getRange() {
            return 0;
        }
    }

}
