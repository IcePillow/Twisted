package com.twisted.logic;

import java.io.Serializable;

public class Grid implements Serializable {

    /* Variables */

    public final int id; //this is also the index in the grid array

    public final int x;
    public final int y;

    public Station station;


    /* Methods */

    /**
     * Constructor
     */
    public Grid(int id, int x, int y){
        this.id = id;
        this.x = x;
        this.y = y;
    }

}
