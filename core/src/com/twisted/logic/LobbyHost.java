package com.twisted.logic;

import com.twisted.net.msg.*;
import com.twisted.net.server.LocalClient;
import com.twisted.net.server.Server;
import com.twisted.net.server.ServerContact;
import com.twisted.vis.ClientsideContact;
import com.twisted.vis.Lobby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LobbyHost implements ServerContact, ClientsideContact {

    //lobby
    private final Lobby lobby;

    //server
    private final Server server;
    private final LocalClient localClient;

    //user information, maps clientId to a player
    private final HashMap<Integer, Player> users;

    //for passing on to the next host, gets created serverside, then grabbed by the host's clientside
    private GameHost gameHost;
    public GameHost getGameHost() {
        return gameHost;
    }


    /* Constructor */

    public LobbyHost(Lobby lobby, String username){

        //initialize
        users = new HashMap<>();

        //external references
        this.lobby = lobby;
        this.server = new Server(this);

        //modify requested username
        if(username.length() > 12){
            username = username.substring(0, 12);
        }
        username = username.replaceAll("[^A-za-z ]", "");
        if(username.length() < 4){
            username = "Host";
        }

        //add player
        int id = server.getNextId();
        users.put(id, new Player(server, id, username));
        localClient = new LocalClient(server, lobby, server.getNextId());
        server.connectLocalClient(localClient);

        //start listening and tell the lobby
        int port = server.startListening();
        if(port == -1){
            lobby.serverLaunchFailed();
        }
        else {
            lobby.serverLaunched();
            lobby.addToTerminal("> Hosting on port " + port);
            lobby.addToTerminal("> Your username is " + username);
        }

    }


    /* Clientside Contact Methods */

    @Override
    public void shutdown(){
        server.closeServer();
    }

    @Override
    public void send(Message message){
        localClient.send(message);
    }


    /* ServerContact Methods */

    @Override
    public void serverReceived(int clientId, Message message) {

        if(message instanceof MChat){
            String string = "[" + users.get(clientId) + "] " + ((MChat) message).string;

            server.broadcastMessage(new MChat(0, string));
        }
        else if(message instanceof MNameChange){

            //grab the name and take only letters
            String string = ((MNameChange) message).name.replaceAll("[^A-za-z ]", "");

            //length check
            if(string.length() > 12){
                string = string.substring(0, 12);
            }

            //make sure this name is not already taken
            for(Player p : users.values()){
                if (p.name.equals(string)) {
                    string = "";
                    break;
                }
            }

            //final name checks and actually change the name
            if (string.length() > 3 && !string.equals("Host")){
                //change in the map
                Player player = users.get(clientId);
                player.name = string;

                //broadcast the name change
                server.broadcastMessage(new MChat(0, "> " + users.get(clientId) + " changed their name to " + string));
            }

        }
        else if(message instanceof MDisconnect){
            server.broadcastMessage(new MChat(0, "> " + users.get(clientId) + " has left the lobby."));
            users.remove(clientId);
        }
        else if(message instanceof MCommand){
            MCommand c = (MCommand) message;

            if(c.isStart()){
                server.stopListening();
                new Thread(() -> {

                    //initial broadcast
                    server.broadcastMessage(new MChat(0, "[Server] Starting the game..."));

                    //assign players and spectators
                    ArrayList<Player> players = new ArrayList<>();
                    ArrayList<Player> toBeKicked = new ArrayList<>();
                    for(Player user : users.values()) {
                        if(players.size() < 2) {
                            players.add(user);

                            server.broadcastMessage(new MChat(0, "[Server] "
                            + user.name + " will be a player."));
                        }
                        else {
                            toBeKicked.add(user);
                        }
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
                        server.kickClient(p.getClientId());
                    }

                    //create game host
                    gameHost = new GameHost(server, localClient, players);

                }).start();

            }

        }

    }

    @Override
    public void clientConnected(int clientId) {
        users.put(clientId, new Player(server, clientId, "User" + clientId));
        server.broadcastMessage(new MChat(0, "> User" + clientId + " joined the lobby"));
    }

    @Override
    public void clientLostConnection(int clientId) {
        server.broadcastMessage(new MChat(0, "> " + users.get(clientId) + " has left the lobby."));
        users.remove(clientId);
    }

}
