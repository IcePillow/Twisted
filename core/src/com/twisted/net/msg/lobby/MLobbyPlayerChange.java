package com.twisted.net.msg.lobby;

import com.twisted.net.msg.Message;

public class MLobbyPlayerChange implements Message {

    public int id;
    public ChangeType type;

    //used in join and rename
    public String name;
    public boolean isHost;

    //use in rename only
    public String oldName;


    /**
     * Constructor
     */
    public MLobbyPlayerChange(int id, ChangeType type){
        this.id = id;
        this.type = type;

        this.isHost = false;
    }


    public enum ChangeType {
        JOIN,
        LEAVE,
        RENAME,
    }

}
