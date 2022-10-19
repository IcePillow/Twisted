package com.twisted.logic.entities.attach;

import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.host.game.ServerGameState;

public abstract class TargetlessWeapon extends Weapon {

    protected TargetlessWeapon(Ship attached) {
        super(attached);
    }

    /* Action Methods */

    @Override
    public void tick(ServerGameState state, Grid grid, Ship ship, float delta){

    }


    /* Targeting Stuff */

    @Override
    public boolean requiresTarget() {
        return false;
    }
    @Override
    public EntPtr getTarget() {
        return null;
    }
    @Override
    public float getLockTimer() {
        return -1;
    }
    @Override
    public boolean isLocked() {
        return false;
    }
    @Override
    public void setTarget(EntPtr target) {}
    @Override
    public void setLockTimer(float lockTimer) {}
}
