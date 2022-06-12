package com.twisted.logic.descriptors;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.Station;

import java.io.Serializable;
import java.util.HashMap;

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

    public final HashMap<Integer, Ship> ships;


    /* Methods */

    /**
     * Constructor
     */
    public Grid(int id, Vector2 pos, String nickname){
        this.id = id;
        this.pos = pos;
        this.nickname = nickname;

        this.ships = new HashMap<>();
    }

}
