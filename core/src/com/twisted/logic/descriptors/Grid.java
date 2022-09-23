package com.twisted.logic.descriptors;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.entities.station.Station;
import com.twisted.logic.mobs.Mobile;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Client and serverside representation of a particular part of space in game.
 */
public class Grid implements Serializable {

    /* Constants */

    public final int id; //this is also the index in the grid array
    public final Vector2 pos;
    public final String nickname;


    /* State */

    public Station station;
    public final Map<Integer, Ship> ships; //sync
    public final Map<Integer, Mobile> mobiles; //sync


    /* Methods */

    /**
     * Constructor
     */
    public Grid(int id, Vector2 pos, String nickname){
        this.id = id;
        this.pos = pos;
        this.nickname = nickname;

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

}
