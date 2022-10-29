package com.twisted.logic.entities.attach;

import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.host.game.ServerGameState;

public abstract class TargetGrdWeapon extends Weapon {

    protected TargetGrdWeapon(Ship attached) {
        super(attached);
    }


    /* Action Methods */

    @Override
    public void tick(ServerGameState state, Grid grid, float frac){
        //reduce cooldown
        if(cooldown > 0){
            cooldown -= frac;
            if(cooldown < 0) cooldown = 0;
        }
    }
    @Override
    public void invalidTick(float frac){ }


    /* Targeting Stuff */

    @Override
    public boolean requiresTarget() {
        return false;
    }
    @Override
    public boolean requiresLocation(){
        return true;
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
