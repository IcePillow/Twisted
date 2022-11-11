package com.twisted.logic.host.game;

import com.twisted.util.Paint;
import com.twisted.logic.Player;
import com.twisted.logic.entities.ship.*;
import com.twisted.net.msg.*;
import com.twisted.net.msg.gameReq.*;
import com.twisted.net.msg.gameUpdate.*;
import com.twisted.net.msg.lobby.MGameStart;
import com.twisted.net.msg.remaining.MSceneChange;
import com.twisted.net.server.Server;
import com.twisted.net.server.ServerContact;
import com.twisted.util.Quirk;

import java.util.*;

public class GameHost implements ServerContact {

    /* Constants */

    public static final int TPS = 20; //ticks per second
    public static final int TICK_DELAY = (int) (1000f/TPS); //millis between each tick
    public static final float FRAC = 1f/TPS; //fraction of a second per tick


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

            loadInitialState(players);
            sendGameStart();
            preLoopCalls();
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
        HashMap<Integer, Paint.Collect> idToFile = new HashMap<>();
        int i = 0;
        for(Player p : state.players.values()){
            if(i==0){
                idToFile.put(p.getId(), Paint.Collect.BLUE);
            }
            else if(i==1){
                idToFile.put(p.getId(), Paint.Collect.ORANGE);
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
        Ship s1 = new Frigate(Ship.Model.Alke, state.useNextShipId(), 0, 2, false);
        s1.pos.set(1, 1);
        s1.rot = (float) Math.PI/2;
        state.grids[0].ships.put(s1.id, s1);
        server.broadcastMessage(MAddShip.createFromShipBody(s1));

        Ship s2 = new Frigate(Ship.Model.Alke, state.useNextShipId(), 0, 1, false);
        s2.pos.set(-1f, 1f);
        s2.rot = (float) -Math.PI/2;
        state.grids[0].ships.put(s2.id, s2);
        server.broadcastMessage(MAddShip.createFromShipBody(s2));

        Ship s3 = new Battleship(Ship.Model.Themis, state.useNextShipId(), 0, 1, false);
        s3.pos.set(-2, 2.5f);
        state.grids[0].ships.put(s3.id, s3);
        server.broadcastMessage(MAddShip.createFromShipBody(s3));

        Ship s4 = new Cruiser(Ship.Model.Helios, state.useNextShipId(), 2, 1, false);
        s4.pos.set(1.5f, 0);
        s4.rot = (float) -Math.PI/2;
        state.grids[2].ships.put(s4.id, s4);
        server.broadcastMessage(MAddShip.createFromShipBody(s4));

        Ship s5 = new Titan(Ship.Model.Nyx, state.useNextShipId(), 0, 1, false);
        s5.pos.set(2f, 0);
        s5.rot = (float) -Math.PI/2;
        state.grids[0].ships.put(s5.id, s5);
        server.broadcastMessage(MAddShip.createFromShipBody(s5));

        //off grid
        Ship s6 = new Battleship(Ship.Model.Themis, state.useNextShipId(), 6, 2, false);
        s6.pos.set(1.5f, 1.5f);
        s6.rot = (float) -Math.PI/2;
        state.grids[6].ships.put(s6.id, s6);
        server.broadcastMessage(MAddShip.createFromShipBody(s6));

        Ship s7 = new Battleship(Ship.Model.Themis, state.useNextShipId(), 6, 2, false);
        s7.pos.set(2, 2);
        s7.rot = (float) -Math.PI/2;
        state.grids[6].ships.put(s7.id, s7);
        server.broadcastMessage(MAddShip.createFromShipBody(s7));

        //gallery
        Ship alke = new Frigate(Ship.Model.Alke, state.useNextShipId(), 0, 2, false);
        alke.pos.set(-2, -2);
        alke.rot = (float) Math.PI/2;
        state.grids[0].ships.put(alke.id, alke);
        server.broadcastMessage(MAddShip.createFromShipBody(alke));

        Ship sparrow = new Frigate(Ship.Model.Sparrow, state.useNextShipId(), 0, 2, false);
        sparrow.pos.set(-1.8f, -2);
        sparrow.rot = (float) Math.PI/2;
        state.grids[0].ships.put(sparrow.id, sparrow);
        server.broadcastMessage(MAddShip.createFromShipBody(sparrow));

        Ship helios = new Cruiser(Ship.Model.Helios, state.useNextShipId(), 0, 2, false);
        helios.pos.set(-1.4f, -2);
        helios.rot = (float) Math.PI/2;
        state.grids[0].ships.put(helios.id, helios);
        server.broadcastMessage(MAddShip.createFromShipBody(helios));

        Ship heron = new Barge(Ship.Model.Heron, state.useNextShipId(), 0, 2, false);
        heron.pos.set(-0.8f, -2);
        heron.rot = (float) Math.PI/2;
        state.grids[0].ships.put(heron.id, heron);
        server.broadcastMessage(MAddShip.createFromShipBody(heron));

        Ship themis = new Battleship(Ship.Model.Themis, state.useNextShipId(), 0, 2, false);
        themis.pos.set(-0.3f, -2);
        themis.rot = (float) Math.PI/2;
        state.grids[0].ships.put(themis.id, themis);
        server.broadcastMessage(MAddShip.createFromShipBody(themis));

        Ship nyx = new Titan(Ship.Model.Nyx, state.useNextShipId(), 0, 2, false);
        nyx.pos.set(0.5f, -2f);
        nyx.rot = (float) Math.PI/2;
        state.grids[0].ships.put(nyx.id, nyx);
        server.broadcastMessage(MAddShip.createFromShipBody(nyx));
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
                        new Quirk(Quirk.Q.ErrorDuringThreadSleep).print();
                    }
                }
                else {
                    new Quirk(Quirk.Q.ErrorDuringThreadSleep).printWithMessage("Game loop sleep time was " + sleepTime);
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
