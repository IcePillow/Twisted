package com.twisted.logic.descriptors;

import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.entities.station.Station;

import java.io.Serializable;
import java.util.HashMap;

public class Tracking implements Serializable {

    public final HashMap<Entity.Tier, Integer> entitiesBuilt;
    public final HashMap<Entity.Tier, Integer> entitiesKilled;

    public Tracking(){
        entitiesBuilt = new HashMap<>();
        entitiesKilled = new HashMap<>();

        for(Entity.Tier s : Ship.Tier.values()){
            entitiesBuilt.put(s, 0);
            entitiesKilled.put(s, 0);
        }
        for(Entity.Tier s : Station.Tier.values()){
            entitiesBuilt.put(s, 0);
            entitiesKilled.put(s, 0);
        }
    }


    /* Utility */

    public void incrEntsBuilt(Entity.Tier tier){
        entitiesBuilt.put(tier, entitiesBuilt.get(tier)+1);
    }

    public void incrEntsKilled(Entity.Tier tier){
        entitiesKilled.put(tier, entitiesKilled.get(tier)+1);
    }

}
