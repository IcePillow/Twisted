package com.twisted.logic.descriptors;

import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.Station;

import java.io.Serializable;
import java.util.HashMap;

public class Tracking implements Serializable {

    public final HashMap<Entity.Subtype, Integer> entitiesBuilt;
    public final HashMap<Entity.Subtype, Integer> entitiesKilled;

    public Tracking(){
        entitiesBuilt = new HashMap<>();
        entitiesKilled = new HashMap<>();

        for(Entity.Subtype s : Ship.Type.values()){
            entitiesBuilt.put(s, 0);
            entitiesKilled.put(s, 0);
        }
        for(Entity.Subtype s : Station.Type.values()){
            entitiesBuilt.put(s, 0);
            entitiesKilled.put(s, 0);
        }
    }


    /* Utility */

    public void incrEntsBuilt(Entity.Subtype subtype){
        entitiesBuilt.put(subtype, entitiesBuilt.get(subtype)+1);
    }

    public void incrEntsKilled(Entity.Subtype subtype){
        entitiesKilled.put(subtype, entitiesKilled.get(subtype)+1);
    }

}
