package com.twisted.logic.descriptors;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.entities.station.Station;
import com.twisted.logic.mobs.Mobile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Client and serverside representation of a particular part of space in game.
 */
public class Grid implements Serializable {

    /* Clientside */

    public float fogTimer;


    /* Constants */

    public final int id; //this is also the index in the grid array
    public final Vector2 loc; //location (named to not confuse with position)
    public final String nickname;
    public final float radius;
    public final float[] resourceGen;


    /* State */

    public Station station;
    public final Map<Integer, Ship> ships; //sync
    public final Map<Integer, Mobile> mobiles; //sync


    /* Methods */

    /**
     * Constructor
     * @param resourceGen Array of length Gem.NUM_OF_GEMS
     */
    public Grid(int id, Vector2 loc, String nickname, float radius, float[] resourceGen){
        this.id = id;
        this.loc = loc;
        this.nickname = nickname;
        this.radius = radius;
        this.resourceGen = resourceGen;

        this.ships = Collections.synchronizedMap(new HashMap<>());
        this.mobiles = Collections.synchronizedMap(new HashMap<>());
    }


    /* Utility */

    public Entity retrieveEntity(Entity.Type type, int id){

        if(type == Entity.Type.Station){
            return station;
        }
        else if(type == Entity.Type.Ship){
            return ships.get(id);
        }
        else {
            return null;
        }
    }

    public ArrayList<Entity> entitiesInSpace(){
        ArrayList<Entity> entities = new ArrayList<>(ships.values());
        entities.add(station);

        return entities;
    }

    /**
     * Client side only. Determines whether an entity is showing taking into account fog.
     */
    public boolean entityShowing(Entity entity){
        return fogTimer > 0 || entity.isShowingThroughFog();
    }

}
