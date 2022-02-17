package com.twisted.net.msg;

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
