package com.twisted.logic.entities.ship;

import com.twisted.logic.entities.attach.Weapon;

public class Titan extends Ship{

    public Titan(Model model, int id, int grid, int owner, boolean docked) {
        super(model, id, grid, owner, docked);

        for(Weapon w : weapons){
            w.sourcePoint.set(0, 0.25f);
        }
    }

}
