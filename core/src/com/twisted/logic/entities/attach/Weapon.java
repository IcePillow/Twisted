package com.twisted.logic.entities.attach;

import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.host.GameHost;

public interface Weapon {

    //data methods

    float getMaxRange();


    //action methods

    /**
     * This method does not necessarily fire the weapon.
     * @param ship The ship that is firing this blaster.
     * @param target The targeted entity.
     * @param delta The amount of time that has passed since the last game loop.
     */
    void fire(GameHost host, Grid grid, Ship ship, Entity target, float delta);


    //enums

    enum Type {
        Blaster,
        //TODO add other weapon types
    }

}
