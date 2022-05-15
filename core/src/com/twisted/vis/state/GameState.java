package com.twisted.vis.state;

import com.badlogic.gdx.graphics.Texture;
import com.twisted.logic.desiptors.CurrentJob;
import com.twisted.logic.desiptors.Grid;
import com.twisted.vis.PlayColor;

import java.util.HashMap;

/**
 * Client side game state object.
 */
public class GameState {

    /* General */

    public boolean readyToRender;


    /* Player Details */

    //list of players
    public String[] playerNames;
    public HashMap<Integer, GamePlayer> players;
    public int myId;


    /* Map Details */

    //map
    public int mapWidth;
    public int mapHeight;

    //main state storage
    public Grid[] grids;
    public HashMap<Integer, CurrentJob> jobs;


    /* Sprites */

    public Texture viewportBackground;


    /* Changing Details */

    /**
     * Basic constructor.
     */
    public GameState(HashMap<Integer, String> playerNames, HashMap<Integer, PlayColor> playerColors){
        this.readyToRender = false;

        this.playerNames = playerNames.values().toArray(new String[0]);

        //create and then fill the array
        this.players = new HashMap<>();
        for(Integer key : playerNames.keySet()){
            //create player
            GamePlayer gamePlayer = new GamePlayer(key, playerNames.get(key));
            gamePlayer.color = playerColors.get(key);

            //add it
            this.players.put(gamePlayer.getId(), gamePlayer);
        }

        //prepare state storage
        jobs = new HashMap<>();

    }

}
