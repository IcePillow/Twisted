package com.twisted.net.msg.remaining;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.entities.Station;
import com.twisted.net.msg.Message;
import com.twisted.local.game.state.PlayColor;

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

    //server details
    public int tickDelay; //millis between ticks

    //map details
    public int mapWidth; //array is {x, y}
    public int mapHeight;

    //grid details, the id of the grid is the position in the array
    public Vector2[] gridPositions;
    public Station.Type[] stationTypes;
    public String[] stationNames;
    public int[] stationOwners;
    public Station.Stage[] stationStages;
    public int[][] stationResources;


    /**
     * Constructor.
     */
    public MGameStart(HashMap<Integer, String> idToName, HashMap<Integer, PlayColor> idToColor, int numGrids){
        this.idToName = idToName;
        this.idToColor = idToColor;

        //grid stuff
        gridPositions = new Vector2[numGrids];
        stationTypes = new Station.Type[numGrids];
        stationNames = new String[numGrids];
        stationOwners = new int[numGrids];
        stationStages = new Station.Stage[numGrids];
        stationResources = new int[numGrids][4];
    }

}
