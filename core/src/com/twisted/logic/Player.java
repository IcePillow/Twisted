package com.twisted.logic;

import com.twisted.logic.descriptors.Tracking;
import com.twisted.net.msg.Message;
import com.twisted.net.server.Server;

/**
 * Serverside player object.
 */
public class Player {

    /* Meta Variables */

    private final Server server;

    //used for game logic
    private final int id;
    public int getId(){
        return id;
    }

    //flags
    public final boolean ai;


    /* State Variables */

    //cosmetic
    public String name;

    //game tracking
    public Tracking tracking;


    /* Constructor */

    public Player(Server server, int id, String name, boolean ai){
        this.server = server;
        this.id = id;
        this.name = name;
        this.ai = ai;

        this.tracking = new Tracking();
    }


    /* Utility */

    public void sendMessage(Message msg){
        //only send message if player is not an ai
        if(!ai){
            server.sendMessage(this.id, msg);
        }
    }

}
