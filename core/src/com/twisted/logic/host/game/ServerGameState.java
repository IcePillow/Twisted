package com.twisted.logic.host.game;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.Player;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.descriptors.events.GameEvent;
import com.twisted.logic.entities.*;
import com.twisted.logic.entities.attach.Weapon;
import com.twisted.net.msg.gameUpdate.MShipDockingChange;
import com.twisted.net.msg.gameUpdate.MShipUpd;
import com.twisted.net.msg.lobby.MGameStart;

import java.util.ArrayList;
import java.util.HashMap;

public class ServerGameState {

    //references
    private final GameHost host;

    //game constants
    int mapWidth;
    int mapHeight;

    //game object containers
    Grid[] grids;
    HashMap<Integer, Ship> shipsInWarp;
    final HashMap<Integer, Player> players;

    //history tracking
    float timeElapsed; //seconds
    private final ArrayList<GameEvent> eventHistory; //constant order

    //id tracking variables, should only be accessed through their respective sync'd methods
    private int nextJobId = 1;
    public synchronized int useNextJobId(){
        nextJobId++;
        return nextJobId-1;
    }
    private int nextShipId = 1;
    public synchronized int useNextShipId(){
        nextShipId++;
        return nextShipId-1;
    }
    private int nextMobileId = 1;
    public synchronized int useNextMobileId(){
        nextMobileId++;
        return nextMobileId-1;
    }


    /* Creation */

    ServerGameState(GameHost host, HashMap<Integer, Player> players){
        this.host = host;

        eventHistory = new ArrayList<>();
        this.players = players;
    }

    void loadInitialState(){
        //map basics
        mapWidth = 1000;
        mapHeight = 1000;

        //players
        Player[] p = players.values().toArray(new Player[0]);

        //grids and stations
        grids = new Grid[]{
                new Grid(0, new Vector2(50, 320), "A"),
                new Grid(1, new Vector2(200, 100), "B"),
                new Grid(2, new Vector2(80, 920), "C"),
                new Grid(3, new Vector2(950, 680), "D"),
                new Grid(4, new Vector2(800, 900), "E"),
                new Grid(5, new Vector2(920, 80), "F"),
                new Grid(6, new Vector2(400, 500), "G"),
                new Grid(7, new Vector2(500, 400), "H"),
        };
        grids[0].station = new Extractor(0, grids[0].nickname, p[0].getId(), Station.Stage.SHIELDED);
        grids[1].station = new Extractor(1, grids[1].nickname, p[0].getId(), Station.Stage.SHIELDED);
        grids[2].station = new Harvester(2, grids[2].nickname, p[0].getId(), Station.Stage.ARMORED);
        grids[3].station = new Extractor(3, grids[3].nickname, p[1].getId(), Station.Stage.SHIELDED);
        grids[4].station = new Extractor(4, grids[4].nickname, p[1].getId(), Station.Stage.SHIELDED);
        grids[5].station = new Harvester(5, grids[5].nickname, p[1].getId(), Station.Stage.ARMORED);
        grids[6].station = new Liquidator(6, grids[6].nickname, 0, Station.Stage.RUBBLE);
        grids[7].station = new Liquidator(7, grids[7].nickname, 0, Station.Stage.RUBBLE);

        //add initial resources
        grids[0].station.resources[0] += 20;
        grids[0].station.resources[1] += 6;
        grids[0].station.resources[2] += 2;
        grids[3].station.resources[0] += 20;
        grids[3].station.resources[1] += 6;
        grids[3].station.resources[2] += 2;

        //set initial timers
        grids[2].station.stageTimer = 60;
        grids[5].station.stageTimer = 60;

        //warp
        shipsInWarp = new HashMap<>();
    }

    void fillGameStart(MGameStart msg){
        //fill in the message
        msg.tickDelay = GameHost.TICK_DELAY;
        msg.mapWidth = mapWidth;
        msg.mapHeight = mapHeight;

        //fill in the grid parts of the message
        for(int j=0; j<grids.length; j++){
            Grid g = grids[j];

            msg.gridPositions[j] = g.pos;
            msg.gridNicknames[j] = g.nickname;
            msg.stationTypes[j] = g.station.subtype();
            msg.stationOwners[j] = g.station.owner;
            msg.stationStages[j] = g.station.stage;
            msg.stationResources[j] = g.station.resources;
        }
    }


    /* Utility */

    Entity findEntityInState(Entity.Type type, int entityId, int gridId){
        if(type == Entity.Type.Station && gridId >= 0 && gridId < grids.length){
            return grids[gridId].station;
        }
        else if(type == Entity.Type.Ship && gridId == -1) {
            return shipsInWarp.get(entityId);
        }
        else if(type == Entity.Type.Ship && gridId >= 0 && gridId < grids.length){
            return grids[gridId].ships.get(entityId);
        }
        else {
            return null;
        }
    }

    void dockShipAtStation(Ship ship, Station station, Grid grid){
        //dock the ship
        grid.ships.remove(ship.id);
        station.dockedShips.put(ship.id, ship);

        //set physics
        ship.rot = 0;
        ship.pos.set(station.pos);
        ship.vel.set(0, 0);
        ship.docked = true;

        //reset other values
        for(Weapon w : ship.weapons){
            w.active = false;
        }
        ship.targetTimeToLock = -1;
        ship.targetingState = null;
        ship.warpTimeToLand = 0;

        //send to clients
        host.broadcastMessage(new MShipDockingChange(ship.id, station.grid, true,
                MShipUpd.createFromShip(ship)));
    }

    /**
     * Should only be used by GameHost at the end of the game to send to users.
     */
    ArrayList<GameEvent> getEventHistory(){
        return eventHistory;
    }

    /**
     * This method will set the time stamp.
     */
    public synchronized void addToEventHistory(GameEvent event){
        event.timeStamp = timeElapsed;
        eventHistory.add(event);
    }

}
