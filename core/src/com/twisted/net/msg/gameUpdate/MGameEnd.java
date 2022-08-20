package com.twisted.net.msg.gameUpdate;

import com.twisted.logic.descriptors.Tracking;

import java.util.HashMap;

public class MGameEnd implements MGameUpd {

    //basic required fields
    public final int winnerId;

    //tracking info on each player
    public final HashMap<Integer, Tracking> tracking;


    /**
     * Constructor
     */
    public MGameEnd(int winnerId){
        this.winnerId = winnerId;

        tracking = new HashMap<>();
    }

}
