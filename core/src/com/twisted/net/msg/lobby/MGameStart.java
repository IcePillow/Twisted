package com.twisted.net.msg.lobby;

import com.badlogic.gdx.math.Vector2;
import com.twisted.Paint;
import com.twisted.logic.entities.station.Station;
import com.twisted.net.msg.Message;

import java.util.HashMap;

public class MGameStart implements Message {

    /* Player Data */

    //all players (no spectators)
    private final HashMap<Integer, String> idToName;
    public HashMap<Integer, String> getPlayers(){
        return idToName;
    }
    private final HashMap<Integer, Paint> playerFiles;
    public HashMap<Integer, Paint> getPlayerFiles(){
        return playerFiles;
    }

    //the player id of the client receiving this
    public int yourPlayerId;


    /* Game Data */

    //server details
    public float tickDelay; //millis between ticks

    //map details
    public int mapWidth; //array is {x, y}
    public int mapHeight;

    //grid details, the id of the grid is the position in the array
    public Vector2[] gridPositions;
    public String[] gridNicknames;
    public Station.Model[] stationTypes;
    public String[] stationNames;
    public int[] stationOwners;
    public Station.Stage[] stationStages;
    public int[][] stationResources;


    /**
     * Constructor.
     */
    public MGameStart(HashMap<Integer, String> idToName, HashMap<Integer, Paint> playerFiles,
                      int numGrids){
        this.idToName = idToName;
        this.playerFiles = playerFiles;

        //grid stuff
        gridPositions = new Vector2[numGrids];
        gridNicknames = new String[numGrids];
        stationTypes = new Station.Model[numGrids];
        stationNames = new String[numGrids];
        stationOwners = new int[numGrids];
        stationStages = new Station.Stage[numGrids];
        stationResources = new int[numGrids][4];
    }

}
