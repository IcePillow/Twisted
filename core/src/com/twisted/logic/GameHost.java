package com.twisted.logic;

import com.twisted.net.msg.MGameStart;
import com.twisted.net.msg.MSceneChange;
import com.twisted.net.msg.Message;
import com.twisted.net.server.LocalClient;
import com.twisted.net.server.Server;
import com.twisted.net.server.ServerContact;
import com.twisted.vis.ClientsideContact;

import java.util.ArrayList;
import java.util.HashMap;

public class GameHost implements ServerContact, ClientsideContact {

    /* Exterior Reference Variables */

    Server server;

    private final LocalClient localClient;
    public LocalClient getLocalClient() {
        return localClient;
    }

    /* Storage Variables */

    private final HashMap<String, Player> players;

    private int mapWidth;
    private int mapHeight;
    private Grid[] grids;


    /* Constructing & Launching */

    /**
     * Constructor. Makes itself the contact of the server.
     * @param players The array of players. Should not exceed the max number for the map.
     */
    public GameHost(Server server, LocalClient localClient, ArrayList<Player> players){

        //copy players over
        this.players = new HashMap<>();
        for(Player p : players){
            this.players.put(p.name, p);
        }

        //create ai players
        for(int i = this.players.size(); i < 2; i++) {
            Player ai = new Player(server, -1, "AI"+i);
            this.players.put(ai.name, ai);
            players.add(ai);
        }

        //make this the server contact and set the server
        this.server = server;
        this.server.setContact(this);
        this.localClient = localClient;

        //tell players to change scene
        MSceneChange sceneChange = new MSceneChange(MSceneChange.Change.GAME);
        sceneChange.isPlayer = true;
        for(Player p : players){
            p.sendMessage(sceneChange);
        }

        //thread that starts the game
        new Thread(() -> {

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //load initial game state
            loadInitialState(players);
            //tell the players the information about the game starting
            sendGameStart();
            //update the players with the first
            sendStateUpdate();

            //TODO start game loop tick

        }).start();

    }

    /**
     * Loads the initial game state.
     * IMPORTANT - ONLY LOADS CLASSIC MAP
     */
    private void loadInitialState(ArrayList<Player> players){

        //map basics
        mapWidth = 1000;
        mapHeight = 1000;

        //grids and stations
        grids = new Grid[2];
        grids[0] = new Grid(0, 50, 400);
        grids[1] = new Grid(1, 950, 600);

        grids[0].station = new Station(0, Station.Type.EXTRACTOR, players.get(0).name, Station.Stage.ARMORED);
        grids[1].station = new Station(1, Station.Type.EXTRACTOR, players.get(1).name, Station.Stage.ARMORED);

    }

    /**
     * Sends the game start message that contains the details for the initial game state.
     */
    private void sendGameStart(){

        //create an array of player names
        String[] playerNames = new String[players.size()];
        int i = 0;
        for (Player p : players.values()){
            playerNames[i] = p.name;
            i++;
        }

        //create the message
        MGameStart msg = new MGameStart(playerNames);

        //fill in the message
        msg.mapWidth = mapWidth;
        msg.mapHeight = mapHeight;
        msg.grids = grids;


        //send the message to each player
        for(Player p : players.values()){
            //set the player's name
            msg.yourPlayer = p.name;
            //and send the message to the player
            p.sendMessage(msg);
        }

    }


    /* ServerContact Methods */

    @Override
    public void serverReceived(int clientId, Message message) {

    }

    @Override
    public void clientConnected(int clientId) {

    }

    @Override
    public void clientLostConnection(int clientId) {

    }


    /* ClientsideContact Methods */

    @Override
    public void send(Message message) {

    }

    @Override
    public void shutdown() {

    }


    /* Logic Methods */

    /**
     * CHANGES
     *
     * New ship
     * New structure
     * Structure state/ownership/health change
     * Structure inventory/manufacturing change
     * Player inventory change
     * Ship position/velocity/health change
     */
    private void sendStateUpdate(){


    }

}
