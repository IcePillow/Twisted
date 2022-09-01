package com.twisted.net.msg.gameUpdate;

import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.twisted.logic.descriptors.Tracking;
import com.twisted.logic.descriptors.events.GameEvent;

import java.util.ArrayList;
import java.util.HashMap;

public class MGameEnd implements MGameUpd {

    //basic data fields
    public final int winnerId;
    public final float timeElapsed;

    //tracking info on each player
    public final HashMap<Integer, Tracking> tracking;
    //history tracking
    public final ArrayList<GameEvent> eventHistory;


    /**
     * Constructor
     */
    public MGameEnd(int winnerId, float timeElapsed, ArrayList<GameEvent> eventHistory){
        this.winnerId = winnerId;
        this.timeElapsed = timeElapsed;

        tracking = new HashMap<>();
        this.eventHistory = eventHistory;
    }

}
