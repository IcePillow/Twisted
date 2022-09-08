package com.twisted.logic.host.lobby;

import com.twisted.logic.Player;
import com.twisted.logic.host.game.GameHost;
import com.twisted.net.msg.*;
import com.twisted.net.msg.lobby.*;
import com.twisted.net.msg.remaining.MChat;
import com.twisted.net.server.Server;
import com.twisted.net.server.ServerContact;
import com.twisted.local.lobby.Lobby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LobbyHost implements ServerContact {

    //lobby
    private final Lobby lobby;

    //server
    private final Server server;
    private final int port;
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

    //state
    private final MatchSettings settings;


    /* Constructor */

    public LobbyHost(Lobby lobby){
        //initialize
        users = new HashMap<>();
        settings = MatchSettings.createWithDefaults();

        //external references
        this.lobby = lobby;
        this.server = new Server(this);

        //start listening and tell the lobby
        port = server.startListening();
        if(port == -1){
            lobby.serverLaunchFailed();
        }
        else {
            lobby.serverLaunched(port);
        }
    }


    /* Hosting Methods */

    public void shutdown(){
        server.closeServer();
    }


    /* ServerContact Methods */

    @Override
    public void serverReceived(int clientId, Message msg) {
        if(msg instanceof MChat){
            String string = "[" + users.get(clientId).name + "] " + ((MChat) msg).string;

            server.broadcastMessage(new MChat(MChat.Type.PLAYER_CHAT, string));
        }
        else if(msg instanceof MCommand){
            MCommand c = (MCommand) msg;

            switch(c.getType()){
                case START: {
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
                    //otherwise, send rejection message
                    else {
                        server.sendMessage(clientId, new MChat(MChat.Type.LOGISTICAL,
                                "> Only the host can start the game"));
                    }
                    break;
                }
                case NAME: {
                    String string = fixName(c.strings[1], clientId==hostId);

                    if(string != null && users.get(clientId) != null){
                        //broadcast the name change
                        MLobbyPlayerChange m = new MLobbyPlayerChange(clientId, MLobbyPlayerChange.ChangeType.RENAME);
                        m.oldName = users.get(clientId).name;
                        m.name = string;
                        server.broadcastMessage(m);

                        //change in the map
                        users.get(clientId).name = string;
                    }
                    else if(string == null && c.strings[1] != null) {
                        server.sendMessage(clientId, new MChat(MChat.Type.LOGISTICAL,
                                "> Names must be unique, alphabetical, and 4-12 characters"));
                    }
                    else {
                        server.sendMessage(clientId, new MChat(MChat.Type.LOGISTICAL,
                                "> Unrecognized name change request"));
                        new Exception().printStackTrace();
                    }
                    break;
                }
                case KICK: {
                    if(clientId == hostId){
                        int kickId = -1;
                        for(Player p : users.values()){
                            if(p.name.equals(c.strings[1])){
                                kickId = p.getId();
                                break;
                            }
                        }

                        //did not find player to kick
                        if(kickId == -1){
                            server.sendMessage(hostId, new MChat(MChat.Type.LOGISTICAL,
                                    "> Could not find player '" + c.strings[1] + "' to kick"));
                        }
                        //did find player to kick
                        else {
                            server.kickClient(kickId, "Kicked");
                            users.remove(kickId);

                            server.broadcastMessage(new MChat(MChat.Type.LOGISTICAL,
                                    "> Kicked player " + c.strings[1]));
                        }
                    }
                    else {
                        server.sendMessage(clientId, new MChat(MChat.Type.LOGISTICAL,
                                "> Only the host can kick players"));
                    }

                    break;
                }
            }
        }
        else if(msg instanceof MSettingRequest){
            if(clientId == hostId){
                MSettingRequest m = (MSettingRequest) msg;

                //make the change
                MSettingChange change = null;
                switch(m.type){
                    case MAP:
                        if(m.forward) settings.map = settings.map.next();
                        else settings.map = settings.map.prev();

                        change = new MSettingChange(MatchSettings.Type.MAP, settings.map);
                        break;
                    default:
                        System.out.println("Unexpected setting type");
                        new Exception().printStackTrace();
                }

                //tell the users
                if(change != null) server.broadcastMessage(change);
            }
            else {
                server.sendMessage(clientId, new MChat(MChat.Type.LOGISTICAL,
                        "> Only the host can change the settings"));
            }
        }
    }
    @Override
    public void clientConnected(int clientId) {
        //create and place the player
        Player player = new Player(server, clientId, "User" + clientId, false);
        users.put(clientId, player);

        //set host id
        if(users.size() == 1) hostId = clientId;

        //send welcome message
        MLobbyWelcome welcome = new MLobbyWelcome(clientId, hostId, users.size(), settings);
        int idx = 0;
        for(Map.Entry<Integer, Player> e : users.entrySet()){
            welcome.playerIdList[idx] = e.getValue().getId();
            welcome.playerNameList[idx] = e.getValue().name;
            idx++;
        }
        server.sendMessage(clientId, welcome);

        //send the join message to other players
        MLobbyPlayerChange msg = new MLobbyPlayerChange(clientId, MLobbyPlayerChange.ChangeType.JOIN);
        msg.name = player.name;
        server.sendMessageToAllButOne(clientId, msg);
    }
    @Override
    public void clientDisconnected(int clientId, String reason){
        MLobbyPlayerChange msg = new MLobbyPlayerChange(clientId, MLobbyPlayerChange.ChangeType.LEAVE);
        msg.name = users.get(clientId).name;
        server.sendMessageToAllButOne(clientId, msg);
        users.remove(clientId);
    }
    @Override
    public void clientLostConnection(int clientId) {
        MLobbyPlayerChange msg = new MLobbyPlayerChange(clientId, MLobbyPlayerChange.ChangeType.LEAVE);
        msg.name = users.get(clientId).name;
        server.sendMessageToAllButOne(clientId, msg);
        users.remove(clientId);
    }


    /* Utility Methods */

    /**
     * Fixes the username asked for.
     * @param req The requested name.
     * @return Null if invalid name request or a string with the name.
     */
    private String fixName(String req, boolean isHost){

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
        if(string.length() < 4) return null;
        //check reserved names
        if((string.startsWith("Host") && !isHost) || string.startsWith("User")) return null;

        //otherwise approve
        return string;
    }

}
