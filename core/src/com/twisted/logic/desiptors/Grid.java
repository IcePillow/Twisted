package com.twisted.logic.desiptors;

import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.Station;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Client and serverside representation of a particular part of space in game.
 */
public class Grid implements Serializable {

    /* Variables */

    public final int id; //this is also the index in the grid array

    public final int x;
    public final int y;

    public Station station;


    /* State */

    public final HashMap<Integer, Ship> ships;


    /* Methods */

    /**
     * Constructor
     */
    public Grid(int id, int x, int y){
        this.id = id;
        this.x = x;
        this.y = y;

        this.ships = new HashMap<>();
    }

}
