package com.twisted.local.game.state;

import com.badlogic.gdx.graphics.Color;
import com.twisted.util.Paint;
import com.twisted.logic.descriptors.CurrentJob;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Client side game state object.
 */
public class ClientGameState {

    /* General */

    public boolean readyToRender;
    public boolean ending;

    public float serverTickDelay;


    /* Player Details */

    //list of players
    public String[] playerNames;
    public Map<Integer, GamePlayer> players; //sync
    public int myId;


    /* Map Details */

    //map
    public int mapWidth;
    public int mapHeight;

    //main state storage
    public Grid[] grids;
    public final Map<Integer, Ship> inWarp; //sync
    public final Map<Integer, CurrentJob> jobs; //sync


    /**
     * Basic constructor.
     */
    public ClientGameState(HashMap<Integer, String> playerNames,
                           HashMap<Integer, Paint.Collect> playerFiles){
        this.readyToRender = false;
        this.ending = false;

        this.playerNames = playerNames.values().toArray(new String[0]);

        //create and then fill the array
        this.players = Collections.synchronizedMap(new HashMap<>());
        for(Integer key : playerNames.keySet()){
            //create player
            GamePlayer gamePlayer = new GamePlayer(key, playerFiles.get(key), playerNames.get(key));

            //add it
            this.players.put(gamePlayer.getId(), gamePlayer);
        }

        //prepare state storage
        inWarp = Collections.synchronizedMap(new HashMap<>());
        jobs = Collections.synchronizedMap(new HashMap<>());
    }


    /* Utility */

    /**
     * Finds an entity in the state given the pointer.
     */
    public Entity findEntity(EntPtr ptr){
        if(ptr == null) return null;
        return findEntity(ptr.grid, ptr.type, ptr.id, ptr.docked);
    }
    /**
     * Finds an entity in the state.
     */
    public Entity findEntity(int grid, Entity.Type type, int id, boolean docked){
        if(type == Entity.Type.Station){
            return grids[grid].station;
        }
        else if(type == Entity.Type.Ship){
            //in space
            if(grid != -1 && !docked){
                return grids[grid].ships.get(id);
            }
            //docked
            else if(grid != -1){
                return grids[grid].station.dockedShips.get(id);
            }
            //in warp
            else {
                return inWarp.get(id);
            }
        }

        return null;
    }

    public Paint.Collect findPaintCollectForOwner(int owner){
        GamePlayer player = players.get(owner);

        if(player != null){
            return player.getCollect();
        }
        else {
            return Paint.Collect.GRAY;
        }
    }
    /**
     * Gets the color for a given owner id.
     */
    public Color findBaseColorForOwner(int owner){
        return findPaintCollectForOwner(owner).base.c;
    }

}
