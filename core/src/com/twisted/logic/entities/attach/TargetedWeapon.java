package com.twisted.logic.entities.attach;

import com.twisted.Util;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.host.game.ServerGameState;

public abstract class TargetedWeapon extends Weapon {

    //target state
    protected EntPtr target;
    public EntPtr getTarget() {
        return target;
    }
    protected float lockTimer; //0 means locked on
    public float getLockTimer(){
        return lockTimer;
    }


    protected TargetedWeapon(Ship attached) {
        super(attached);
    }


    /* Action Methods */

    @Override
    public void tick(ServerGameState state, Grid grid, Ship ship, float delta){
        //not locked on yet
        if(lockTimer > 0){
            lockTimer -= delta;
            if(lockTimer < 0) lockTimer = 0;
        }
    }
    @Override
    public void activate(Entity entity){
        super.activate(entity);
        target = EntPtr.createFromEntity(entity);

        lockTimer = 100f/(subtype().getScanRes() * (float)Math.pow(Util.asinh(entity.getSigRadius()), 2));
    }
    @Override
    public void deactivate(){
        super.deactivate();
        target = null;
        lockTimer = -1;
    }


    /* State Methods */

    @Override
    public boolean isLocked() {
        return (active && target != null && lockTimer == 0);
    }


    /* Data Methods */

    @Override
    public boolean requiresTarget() {
        return true;
    }


    /* Server Data Copy */

    @Override
    public void setTarget(EntPtr target) {
        this.target = target;
    }
    @Override
    public void setLockTimer(float lockTimer) {
        this.lockTimer = lockTimer;
    }

}
