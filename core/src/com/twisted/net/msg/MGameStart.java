package com.twisted.net.msg;

import com.twisted.logic.Grid;

public class MGameStart implements Message {

    /* Player Data */

    //all players (no spectators)
    private final String[] players;
    public String[] getPlayers(){
        return players;
    }

    //the player id of the client receiving this
    public String yourPlayer;


    /* Game Data */

    //map details
    public int mapWidth; //array is {x, y}
    public int mapHeight;
    public Grid[] grids;


    /**
     * Constructor.
     */
    public MGameStart(String[] players){
        this.players = players;
    }

}
