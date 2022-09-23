package com.twisted.logic.entities.ship;

import com.twisted.logic.entities.attach.Weapon;

public class Cruiser extends Ship {

    /**
     * Constructor
     */
    public Cruiser(Ship.Model model, int shipId, int gridId, int owner, boolean docked){
        super(model, shipId, gridId, owner, docked);

        for(Weapon w : weapons){
            w.sourcePoint.set(0, 0.15f);
        }
    }

}
