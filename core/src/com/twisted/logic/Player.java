package com.twisted.logic;

import com.twisted.net.msg.Message;
import com.twisted.net.server.Server;

/**
 * Serverside player object.
 */
public class Player {

    /* Meta Variables*/

    private final Server server;

    private final int clientId;
    public int getClientId(){
        return clientId;
    }

    /* State Variables */

    public String name;


    /* Constructor */

    public Player(Server server, int clientId, String name){
        this.server = server;
        this.clientId = clientId;
        this.name = name;
    }


    /* Utility */

    public void sendMessage(Message msg){
        if(clientId > 0) server.sendMessage(this.clientId, msg);
    }

}
