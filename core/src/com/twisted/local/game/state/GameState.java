package com.twisted.local.game.state;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.twisted.logic.descriptors.CurrentJob;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.Station;

import java.util.HashMap;

/**
 * Client side game state object.
 */
public class GameState {

    /* General */

    public boolean readyToRender;

    public int serverTickDelay;


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
    public HashMap<Integer, Ship> inWarp;
    public HashMap<Integer, CurrentJob> jobs;


    /* Sprites */

    public Texture viewportBackground;


    /**
     * Basic constructor.
     */
    public GameState(HashMap<Integer, String> playerNames, HashMap<Integer, PlayerFile> playerFiles){
        this.readyToRender = false;

        this.playerNames = playerNames.values().toArray(new String[0]);

        //create and then fill the array
        this.players = new HashMap<>();
        for(Integer key : playerNames.keySet()){
            //create player
            GamePlayer gamePlayer = new GamePlayer(key, playerFiles.get(key).file, playerNames.get(key),
                    playerFiles.get(key).color);

            //add it
            this.players.put(gamePlayer.getId(), gamePlayer);
        }

        //prepare state storage
        inWarp = new HashMap<>();
        jobs = new HashMap<>();
    }


    /* Utility */

    /**
     * Finds the grid id of a given ship. Not efficient to use this a lot.
     * @return Returns -1 if in warp. Returns -99 if not found.
     */
    public int findShipGridId(int shipId){
        if(inWarp.containsKey(shipId)){
            return -1;
        }

        for(Grid g : grids){
            if(g.ships.containsKey(shipId)) return g.id;
        }

        return -99;
    }

    /**
     * Finds an entity in the state given the pointer.
     */
    public Entity findEntity(EntPtr ptr){
        if(ptr.type == Entity.Type.Station){
            return grids[ptr.grid].station;
        }
        else if(ptr.type == Entity.Type.Ship && ptr.grid != -1 && ptr.docked == -1){
            return grids[ptr.grid].ships.get(ptr.id);
        }
        else if(ptr.type == Entity.Type.Ship && ptr.grid != -1){
            return grids[ptr.grid].station.dockedShips.get(ptr.id);
        }
        else if(ptr.type == Entity.Type.Ship) {
            return inWarp.get(ptr.id);
        }

        return null;
    }

    /**
     * Gets the color for a given owner id.
     */
    public Color findColorForOwner(int owner){
        GamePlayer player = players.get(owner);

        if(player != null){
            return player.getColor();
        }
        else {
            return PlayerFile.GRAY.color;
        }
    }

}
