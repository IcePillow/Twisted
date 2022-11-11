package com.twisted.logic.entities.attach;

import com.badlogic.gdx.math.Vector2;
import com.twisted.util.Asset;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.host.game.ServerGameState;

/**
 * This class should not be directly inherited. Instead, weapon classes should inherit from
 * TargetlessWeapon or TargetedWeapon.
 */
public abstract class Weapon {

    //meta
    public final Ship attached;

    //graphics
    public final Vector2 mountPoint;

    //active state
    protected boolean active;
    public boolean isActive(){
        return active;
    }
    public float timer; //counts down to 0
    public float cooldown; //counts down to 0


    /* Constructor */

    protected Weapon(Ship attached, Vector2 mountPoint){
        this.attached = attached;
        this.mountPoint = mountPoint;
    }


    /* Action Methods */

    /**
     * This is called each server tick when the ship is in a valid firing state.
     * Child classes should call super.tick() at the beginning of their tick().
     * @param frac The amount of time that has passed since the last game loop.
     */
    public abstract void tick(ServerGameState state, Grid grid, float frac);
    /**
     * This is called each server tick when the ship is not in a valid firing state (such as docked
     * or warping).
     */
    public abstract void invalidTick(float frac);

    public void activate(Entity entity, Vector2 location){
        active = true;
    }
    public void deactivate(){
        active = false;
    }


    /* Type Methods */

    public Type getType(){
        if(this instanceof Blaster) return Type.Blaster;
        else if(this instanceof Laser) return Type.Laser;
        else if(this instanceof StationTrans) return Type.StationTrans;
        else if(this instanceof Beacon) return Type.Beacon;
        else if(this instanceof Doomsday) return Type.Doomsday;
        else return null;
    }
    public abstract Model subtype();


    /* Utility Methods */

    public void stopTargetingEntity(Entity entity){
        if(this.getTarget().matches(entity)) deactivate();
    }
    public boolean checkEntityInRange(Entity target){
        return (target.pos.dst(attached.pos) <= this.subtype().getRange());
    }

    public String getLockTimerText(){
        float t = getLockTimer();

        if(t > 1){
            return Integer.toString(Math.round(t));
        }
        else {
            return Float.toString(Math.round(t*10)/10f);
        }
    }


    /* State Methods */

    public abstract EntPtr getTarget();
    public abstract float getLockTimer();
    public abstract boolean isLocked();


    /* Data Methods */

    public abstract Asset.UiButton getOffButtonAsset();
    public abstract Asset.UiButton getOnButtonAsset();
    public Asset.UiButton getCurrentButtonAsset(){
        if(active) return getOnButtonAsset();
        else return getOffButtonAsset();
    }

    public abstract float getFullTimer();
    public float getFullCooldown(){
        return 0;
    }
    public abstract boolean requiresTarget();
    public abstract boolean requiresLocation();


    /* Server Data Copy Methods */

    public void setActive(boolean active){
        this.active = active;
    }
    public abstract void setTarget(EntPtr target);
    public abstract void setLockTimer(float lockTimer);


    /* Enums */

    public enum Type {
        Blaster,
        Laser,
        StationTrans,
        Beacon,
        Doomsday
    }

    public interface Model {
        Type getType();
        float getRange();
        float getScanRes();
    }

}
