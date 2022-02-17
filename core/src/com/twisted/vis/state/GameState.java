package com.twisted.vis.state;

import com.badlogic.gdx.graphics.Color;
import com.twisted.logic.Grid;

import java.util.HashMap;

/**
 * Client side game state object.
 */
public class GameState {

    /* Player Details */

    //color index
    public static final Color[] COLORS = {
            new Color(0x42a5f5ff),
            new Color(0xfb8c00ff),

            new Color(0x4caf50ff),
            new Color(0xab47bcff),
    };

    //list of players
    public GamePlayer myPlayer;
    public HashMap<String, GamePlayer> players;

    /* Map Details */

    //map
    public int mapWidth;
    public int mapHeight;
    public Grid[] grids;


    /* Changing Details */

    /**
     * Basic constructor.
     */
    public GameState(String myPlayerName, String[] playerNames){
        this.myPlayer = new GamePlayer(myPlayerName);

        //create and then fill the array
        this.players = new HashMap<>();
        for(int i=0; i < playerNames.length; i++){

            GamePlayer gamePlayer = new GamePlayer(playerNames[i]);
            if(i % 2 == 1) gamePlayer.color = PlayerColor.BLUE;
            else gamePlayer.color = PlayerColor.ORANGE;

            this.players.put(playerNames[i], gamePlayer);
        }

    }

    /**
     * An enum containing the names of the colors used. Lowercase them to get the filenames.
     */
    public enum PlayerColor {
        BLUE,
        ORANGE,
        GREEN,
        PURPLE,
        BLACK
    }


}
