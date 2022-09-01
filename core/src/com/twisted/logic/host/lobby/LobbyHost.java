package com.twisted.logic.host.lobby;

import com.twisted.logic.Player;
import com.twisted.logic.host.game.GameHost;
import com.twisted.net.msg.*;
import com.twisted.net.msg.remaining.MChat;
import com.twisted.net.msg.remaining.MCommand;
import com.twisted.net.server.Server;
import com.twisted.net.server.ServerContact;
import com.twisted.local.lobby.Lobby;

import java.util.ArrayList;
import java.util.HashMap;

public class LobbyHost implements ServerContact {

    //lobby
    private final Lobby lobby;

    //server
    private final Server server;
    private int port;
    public int getPort(){
        return port;
    }

    //user information, maps clientId to a player
    private final HashMap<Integer, Player> users;
    private int hostId;

    //for passing on to the next host, gets created serverside, then grabbed by the host's clientside
    private GameHost gameHost;
    public GameHost getGameHost() {
        return gameHost;
    }


    /* Constructor */

    public LobbyHost(Lobby lobby){

        //initialize
        users = new HashMap<>();

        //external references
        this.lobby = lobby;
        this.server = new Server(this);

        //start listening and tell the lobby
        port = server.startListening();
        if(port == -1){
            lobby.serverLaunchFailed();
        }
        else{
            lobby.serverLaunched(port);
        }

    }


    /* Hosting Methods */

    public void shutdown(){
        server.closeServer();
    }


    /* ServerContact Methods */

    @Override
    public void serverReceived(int clientId, Message message) {
        if(message instanceof MChat){
            String string = "[" + users.get(clientId).name + "] " + ((MChat) message).string;

            server.broadcastMessage(new MChat(MChat.Type.PLAYER_CHAT, string));
        }
        else if(message instanceof MCommand){
            MCommand c = (MCommand) message;

            if(c.isStart()){
                //do the start
                if(clientId == hostId){
                    server.stopListening();
                    new Thread(() -> {
                        //initial broadcast
                        server.broadcastMessage(new MChat(MChat.Type.LOGISTICAL,"[Server] Starting the game..."));

                        //assign players and spectators
                        ArrayList<Player> toBeKicked = new ArrayList<>();
                        int playerCount = 0;
                        for(Player user : users.values()) {
                            if(playerCount < 2) {
                                server.broadcastMessage(new MChat(MChat.Type.LOGISTICAL, "[Server] "
                                        + user.name + " will be a player."));
                            }
                            else {
                                toBeKicked.add(user);
                            }
                            playerCount++;
                        }

                        //sleep
                        try {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //kick players
                        for(Player p : toBeKicked){
                            server.kickClient(p.getId(), "Game started");
                            users.remove(p.getId());
                        }

                        //create game host
                        gameHost = new GameHost(server, users, hostId);

                    }).start();
                }
                //otherwise send rejection message
                else {
                    server.sendMessage(clientId, new MChat(MChat.Type.LOGISTICAL,
                            "> Only the host can start the game."));
                }

            }
            else if(c.isName()){
                String string = fixName(c.strings[1]);

                if(string != null){
                    //broadcast the name change
                    server.sendMessageToAllButOne(clientId, new MChat(MChat.Type.LOGISTICAL,
                            "> " + users.get(clientId).name + " changed their name to " + string));
                    server.sendMessage(clientId, new MChat(MChat.Type.LOGISTICAL,
                            "> You changed your name to " + string));

                    //change in the map
                    users.get(clientId).name = string;
                }
                else {
                    server.sendMessage(clientId, new MChat(MChat.Type.LOGISTICAL,
                            "> That name is invalid."));
                }
            }
        }
    }

    @Override
    public void clientConnected(int clientId) {
        //create and place the player
        Player player = new Player(server, clientId, "User" + clientId, false);
        users.put(clientId, player);

        //either set hostId or send a welcome message
        if(users.size() == 1) hostId = clientId;
        else server.sendMessage(clientId, new MChat(MChat.Type.LOGISTICAL, "> Welcome to the lobby hosted by "
                + users.get(hostId).name));

        //broadcast the join message
        for(Player p : users.values()){
            if(p.getId() != clientId){
                server.sendMessage(p.getId(), new MChat(MChat.Type.LOGISTICAL,
                        "> " + player.name + " joined the lobby"));
            }
        }
    }

    @Override
    public void clientDisconnected(int clientId, String reason){
        server.broadcastMessage(new MChat(MChat.Type.LOGISTICAL, "> " + users.get(clientId).name + " has left the lobby."));
        users.remove(clientId);
    }

    @Override
    public void clientLostConnection(int clientId) {
        server.broadcastMessage(new MChat(MChat.Type.LOGISTICAL, "> " + users.get(clientId).name + " lost connection."));
        users.remove(clientId);
    }


    /* Utility Methods */

    /**
     * Fixes the username asked for.
     * @param req The requested name.
     * @return Null if invalid name request or a string with the name.
     */
    private String fixName(String req){

        //replace non-alphabetical
        String string = req.replaceAll("[^A-za-z]", "");
        //shorten the string
        if(string.length() > 12){
            string = string.substring(0, 12);
        }
        //make sure this name is not already taken
        for(Player p : users.values()){
            if (p.name.equals(string)) {
                return null;
            }
        }
        //check length
        if(string.length() < 3) return null;

        //otherwise approve
        return string;
    }

}
