package com.twisted.logic.entities.attach;

import com.badlogic.gdx.math.Vector2;
import com.twisted.Asset;
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
    public final Vector2 sourcePoint;

    //active state
    protected boolean active;
    public boolean isActive(){
        return active;
    }
    public float timer; //counts down to 0


    /* Constructor */

    protected Weapon(Ship attached){
        this.attached = attached;
        this.sourcePoint = new Vector2(0, 0);
    }


    /* Action Methods */

    /**
     * This is where the checks for firing are done and firing is done if appropriate.
     * Child classes should call super.tick() at the beginning of their tick().
     * @param ship The ship that is firing this weapon.
     * @param delta The amount of time that has passed since the last game loop.
     */
    public abstract void tick(ServerGameState state, Grid grid, Ship ship, float delta);

    public void activate(Entity entity){
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
    public abstract boolean requiresTarget();


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
        Beacon
    }

    public interface Model {
        Type getType();
        float getRange();
        float getScanRes();
    }

}
