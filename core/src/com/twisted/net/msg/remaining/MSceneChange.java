package com.twisted.net.msg.remaining;

import com.twisted.net.msg.Message;

public class MSceneChange implements Message {

    private Change change;
    public Change getChange() {
        return change;
    }

    //game data
    public boolean isPlayer;


    /**
     * Constructor
     */
    public MSceneChange(Change change){
        this.change = change;
    }


    public enum Change {
        GAME
    }

}
