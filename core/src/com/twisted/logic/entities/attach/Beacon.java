package com.twisted.logic.entities.attach;

import com.badlogic.gdx.math.Vector2;
import com.twisted.util.Asset;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.host.game.ServerGameState;

public class Beacon extends TargetlessWeapon{

    public final Model model;

    public Beacon(Ship attached, Vector2 sourcePoint, Model model) {
        super(attached, sourcePoint);

        this.model = model;
    }


    /* Action Methods */

    @Override
    public void tick(ServerGameState state, Grid grid, float frac){
        super.tick(state, grid, frac);

        //disable
        if(attached.vel.len2() > 0 || attached.warpCharge > 0 || attached.countActiveWeapons() > 1){
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
    @Override
    public boolean requiresTarget() {
        return false;
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
        @Override
        public float getScanRes(){
            return 1;
        }
    }

}
