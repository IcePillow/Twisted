package com.twisted.logic.host.game;

import com.twisted.local.game.state.PlayerFile;
import com.twisted.logic.Player;
import com.twisted.logic.entities.*;
import com.twisted.net.msg.*;
import com.twisted.net.msg.gameReq.*;
import com.twisted.net.msg.gameUpdate.*;
import com.twisted.net.msg.lobby.MGameStart;
import com.twisted.net.msg.remaining.MSceneChange;
import com.twisted.net.server.Server;
import com.twisted.net.server.ServerContact;

import java.util.*;

public class GameHost implements ServerContact {

    /* Constants */

    public static final int TICK_DELAY = 50; //millis between each tick
    public static final float FRAC = TICK_DELAY / 1000f; //fraction of a second per tick


    /* Exterior Reference Variables */

    //fully exterior
    private final Server server;

    //helpers
    private ServerGameState state;
    private ServerGameLoop loop;


    /* Storage Variables */

    //player details
    private final int hostId;

    //networking and game loop
    private final HashMap<MGameReq, Integer> requests; //requests being read during game logic
    private final Map<MGameReq, Integer> hotRequests; //requests stored between ticks (synchronized)
    private boolean looping;


    /* Constructing & Launching */

    /**
     * Constructor. Makes itself the contact of the server.
     * @param players Hashmap of players. Will be used and modified.
     */
    public GameHost(Server server, HashMap<Integer, Player> players, int hostId){
        //initialize and copy
        this.requests = new HashMap<>();
        this.hotRequests = Collections.synchronizedMap(new HashMap<>());
        this.looping = true;
        this.hostId = hostId;

        //create ai players
        for(int i = players.size(); i < 2; i++) {
            int id = server.useNextId();

            Player aiPlayer = new Player(server, id, "AI"+id, true);
            players.put(id, aiPlayer);
        }

        //make this the server contact and set the server
        this.server = server;
        this.server.setContact(this);

        //tell players to change scene
        MSceneChange sceneChange = new MSceneChange(MSceneChange.Change.GAME);
        sceneChange.isPlayer = true;
        for(Player p : players.values()){
            p.sendMessage(sceneChange);
        }

        //thread that starts the game
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //load initial game state
            loadInitialState(players);

            //tell the players the information about the game starting
            sendGameStart();

            preLoopCalls();

            //begin the game loop
            startGameLoop();

        }).start();
    }

    /**
     * Loads the initial game state.
     * IMPORTANT - ONLY LOADS CLASSIC MAP
     */
    private void loadInitialState(HashMap<Integer, Player> players){
        //create the initial state
        state = new ServerGameState(this, players);
        state.loadInitialState();

        //create the looper
        loop = new ServerGameLoop(this, state);
    }

    /**
     * Sends the game start message that contains the details for the initial game state.
     */
    private void sendGameStart(){
        //create maps of ids to player names and colors
        HashMap<Integer, String> idToName = new HashMap<>();
        for (Player p : state.players.values()){
            idToName.put(p.getId(), p.name);
        }
        HashMap<Integer, PlayerFile> idToFile = new HashMap<>();
        int i = 0;
        for(Player p : state.players.values()){
            if(i==0){
                idToFile.put(p.getId(), PlayerFile.BLUE);
            }
            else if(i==1){
                idToFile.put(p.getId(), PlayerFile.ORANGE);
            }
            else break;
            i++;
        }

        //create the message
        MGameStart msg = new MGameStart(idToName, idToFile, state.grids.length);
        state.fillGameStart(msg);

        //send the message to each player
        for(Player p : state.players.values()){
            //set the player's name
            msg.yourPlayerId = p.getId();
            //and send the message to the player
            p.sendMessage(msg);
        }
    }

    /**
     * Any calls that should be made after the game state is initialized but before the game loop
     * begins. Mostly for development.
     */
    private void preLoopCalls(){
        //dev ships
        Ship s1 = new Frigate(state.useNextShipId(), 0, 2, false);
        s1.pos.set(1, 1);
        s1.rot = (float) Math.PI/2;
        state.grids[0].ships.put(s1.id, s1);
        server.broadcastMessage(MAddShip.createFromShipBody(s1));

        Ship s2 = new Frigate(state.useNextShipId(), 0, 1, false);
        s2.pos.set(-0.9f, 0.9f);
        s2.rot = (float) -Math.PI/2;
        state.grids[0].ships.put(s2.id, s2);
        server.broadcastMessage(MAddShip.createFromShipBody(s2));
    }


    /* ServerContact Methods */

    /**
     * Called when the server receives a message from a client.
     */
    @Override
    public void serverReceived(int clientId, Message message) {
        if(message instanceof MGameReq){
            hotRequests.put((MGameReq) message, clientId);
        }
    }

    /**
     * Should never be called in this GameHost.
     */
    @Override
    public void clientConnected(int clientId) {

    }

    @Override
    public void clientDisconnected(int clientId, String reason) {
        //TODO this function
    }

    @Override
    public void clientLostConnection(int clientId) {
        //TODO this function
    }


    /* Game Loop */

    /**
     * Prepares the message arrays for the game loop by copying from hotRequests to requests.
     */
    private void prepRequests(){
        requests.clear();
        requests.putAll(hotRequests);
        hotRequests.clear();
    }

    /**
     * Starts the game loop thread.
     */
    private void startGameLoop(){
        new Thread(() -> {
            //declare
            long sleepTime, startTime;

            //the game loop
            while(looping){
                startTime = System.currentTimeMillis();

                //run the methods
                prepRequests();
                loop.loop(requests);

                //sleep
                sleepTime = startTime+GameHost.TICK_DELAY - System.currentTimeMillis();
                if(sleepTime > 0){
                    try {
                        Thread.sleep(sleepTime);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    System.out.println("[Warning] Game loop sleep time was " + sleepTime);
                }
            }

        }).start();
    }


    /* Exterior Facing */

    void broadcastMessage(Message msg){
        server.broadcastMessage(msg);
    }

    void sendMessage(int userId, Message msg){
        server.sendMessage(userId, msg);
    }

    void endGame(int winner){
        //end the game
        looping = false;

        //create game end message
        MGameEnd gameEnd = new MGameEnd(winner, state.timeElapsed, state.getEventHistory());

        //fill player tracking info
        for(Map.Entry<Integer, Player> e : state.players.entrySet()){
            gameEnd.tracking.put(e.getKey(), e.getValue().tracking);
        }

        //send last message then close the server
        broadcastMessage(gameEnd);
        server.closeServer();
    }


}
