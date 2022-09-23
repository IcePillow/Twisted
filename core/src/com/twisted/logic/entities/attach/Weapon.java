package com.twisted.logic.entities.attach;

import com.badlogic.gdx.math.Vector2;
import com.twisted.Asset;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.host.game.ServerGameState;

public abstract class Weapon {

    //meta
    public final Ship attached;

    //graphics
    public final Vector2 sourcePoint;

    //state
    public boolean active;
    public float timer;

    /* Constructor */

    protected Weapon(Ship attached){
        this.attached = attached;
        this.sourcePoint = new Vector2(0, 0);
    }


    /* Action Methods */

    /**
     * This is where the checks for firing are done and firing is done if appropriate.
     * @param ship The ship that is firing this weapon.
     * @param target The targeted entity, nullable.
     * @param delta The amount of time that has passed since the last game loop.
     */
    public abstract void tick(ServerGameState state, Grid grid, Ship ship, Entity target,
                              Ship.Targeting targeting, float delta);

    public abstract void putOnFullCooldown();


    /* Type Methods */

    public Type getType(){
        if(this instanceof Blaster) return Type.Blaster;
        else if(this instanceof Laser) return Type.Laser;
        else if(this instanceof StationTrans) return Type.StationTrans;
        else return null;
    }
    public abstract Model subtype();


    /* Data Methods */

    public abstract Asset.UiButton getOffButtonAsset();
    public abstract Asset.UiButton getOnButtonAsset();

    public Asset.UiButton getCurrentButtonAsset(){
        if(active) return getOnButtonAsset();
        else return getOffButtonAsset();
    }

    public abstract float getFullTimer();


    /* Enums */

    public enum Type {
        Blaster,
        Laser,
        StationTrans,
    }

    public interface Model {
        Type getType();
        float getRange();
    }

}
