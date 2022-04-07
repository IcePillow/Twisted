package com.twisted.net.msg.remaining;

import com.twisted.logic.desiptors.Grid;
import com.twisted.net.msg.Message;
import com.twisted.vis.PlayColor;

import java.util.HashMap;

public class MGameStart implements Message {

    /* Player Data */

    //all players (no spectators)
    private final HashMap<Integer, String> idToName;
    public HashMap<Integer, String> getPlayers(){
        return idToName;
    }
    private final HashMap<Integer, PlayColor> idToColor;
    public HashMap<Integer, PlayColor> getColors(){
        return idToColor;
    }

    //the player id of the client receiving this
    public int yourPlayerId;


    /* Game Data */

    //map details
    public int mapWidth; //array is {x, y}
    public int mapHeight;
    public Grid[] grids;


    /**
     * Constructor.
     */
    public MGameStart(HashMap<Integer, String> idToName, HashMap<Integer, PlayColor> idToColor){
        this.idToName = idToName;
        this.idToColor = idToColor;
    }

}
