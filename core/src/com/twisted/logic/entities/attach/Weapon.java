package com.twisted.logic.entities.attach;

import com.twisted.Asset;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.host.game.ServerGameState;

public abstract class Weapon {

    //meta
    public final Entity attached;

    //state
    public boolean active;
    public float timer;


    /* Constructor */

    protected Weapon(Entity attached){
        this.attached = attached;
    }


    /* Action Methods */

    /**
     * This is where the checks for firing are done and firing is done if appropriate.
     * @param ship The ship that is firing this blaster.
     * @param target The targeted entity, null is okay.
     * @param delta The amount of time that has passed since the last game loop.
     */
    public abstract void tick(ServerGameState state, Grid grid, Ship ship, Entity target,
                              Ship.Targeting targeting, float delta);

    public abstract void putOnFullCooldown();


    /* Data Methods */

    public abstract float getMaxRange();
    public abstract Asset.UiButton getOffButtonAsset();
    public abstract Asset.UiButton getOnButtonAsset();
    public abstract float getFullTimer();

    public Type getType(){
        if(this instanceof Blaster) return Type.Blaster;
        else if(this instanceof StationTransport) return Type.StationTransport;
        else return null;
    }

    public Asset.UiButton getCurrentButtonAsset(){
        if(active) return getOnButtonAsset();
        else return getOffButtonAsset();
    }


    //enums
    public enum Type {
        Blaster,
        StationTransport,
        //TODO add other weapon types
    }

}
