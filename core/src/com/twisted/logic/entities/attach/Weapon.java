package com.twisted.logic.entities.attach;

import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.host.GameHost;

public abstract class Weapon {

    //state
    public boolean active;


    /* Action Methods */

    /**
     * This is where the checks for firing are done and firing is done if appropriate.
     * @param ship The ship that is firing this blaster.
     * @param target The targeted entity, null is okay.
     * @param delta The amount of time that has passed since the last game loop.
     */
    public abstract void tick(GameHost host, Grid grid, Ship ship, Entity target,
                              Ship.Targeting targeting, float delta);

    /* Data Methods */

    public abstract float getMaxRange();

    public Type getType(){
        if(this instanceof Blaster) return Type.Blaster;
        else return null;
    }


    //enums
    public enum Type {
        Blaster,
        //TODO add other weapon types
    }

}
