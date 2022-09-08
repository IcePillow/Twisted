package com.twisted.logic.entities;

import com.twisted.logic.descriptors.Gem;
import com.twisted.logic.entities.attach.StationTransport;
import com.twisted.logic.entities.attach.Weapon;

public class Barge extends Ship {

    /* State */

    public final int[] resources;


    /* Constructor */

    public Barge(int shipId, int gridId, int owner, boolean docked) {
        super(Model.Barge, shipId, gridId, owner, docked);

        weapons[0] = new StationTransport(this);

        resources = new int[4];
    }


    /* Data Methods */

    /**
     * Amount of space for carrying resources.
     */
    public float getHoldSize(){
        return 10f;
    }


    /* Utility */

    /**
     * @return The max number of additional gems that can be fit.
     */
    public float maxGemsCanFit(Gem gemType){
        float space = getHoldSize();
        for(int i = 0; i<Gem.NUM_OF_GEMS; i++){
            space -= Gem.orderedGems[i].volume * resources[i];
        }

        if(space < 0) return 0;
        else return (float) Math.floor(space/gemType.volume);
    }
}
