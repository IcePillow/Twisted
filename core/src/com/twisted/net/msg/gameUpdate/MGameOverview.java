package com.twisted.net.msg.gameUpdate;

import java.util.HashMap;

/**
 * Represents a large update being sent to a player.
 */
public class MGameOverview implements MGameUpdate{

    public final HashMap<Integer, Float> jobToTimeLeft; //maps a given job id to the time left
    public final HashMap<Integer, int[]> stationToResources; //maps a station to the resources it contains

    public MGameOverview(){
        jobToTimeLeft = new HashMap<>();
        stationToResources = new HashMap<>();
    }

}
