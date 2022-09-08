package com.twisted.logic.entities;

import com.twisted.logic.entities.attach.Blaster;
import com.twisted.logic.entities.attach.Weapon;

public class Frigate extends Ship {

    /**
     * Constructor
     */
    public Frigate(int shipId, int gridId, int owner, boolean docked){
        super(Model.Frigate, shipId, gridId, owner, docked);

        weapons[0] = new Blaster(this, 3, 2, 1.4f, 2);
        weapons[1] = new Blaster(this, 3, 2, 1.4f, 2);
    }


}
