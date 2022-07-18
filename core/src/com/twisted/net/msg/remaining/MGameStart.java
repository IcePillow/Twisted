package com.twisted.net.msg.remaining;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.twisted.local.game.state.PlayerFile;
import com.twisted.logic.entities.Station;
import com.twisted.net.msg.Message;

import java.util.HashMap;

public class MGameStart implements Message {

    /* Player Data */

    //all players (no spectators)
    private final HashMap<Integer, String> idToName;
    public HashMap<Integer, String> getPlayers(){
        return idToName;
    }
    private final HashMap<Integer, PlayerFile> playerFiles;
    public HashMap<Integer, PlayerFile> getPlayerFiles(){
        return playerFiles;
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
    public String[] gridNicknames;
    public Station.Type[] stationTypes;
    public String[] stationNames;
    public int[] stationOwners;
    public Station.Stage[] stationStages;
    public int[][] stationResources;


    /**
     * Constructor.
     */
    public MGameStart(HashMap<Integer, String> idToName, HashMap<Integer, PlayerFile> playerFiles,
                      int numGrids){
        this.idToName = idToName;
        this.playerFiles = playerFiles;

        //grid stuff
        gridPositions = new Vector2[numGrids];
        gridNicknames = new String[numGrids];
        stationTypes = new Station.Type[numGrids];
        stationNames = new String[numGrids];
        stationOwners = new int[numGrids];
        stationStages = new Station.Stage[numGrids];
        stationResources = new int[numGrids][4];
    }

}
