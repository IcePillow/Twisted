package com.twisted.net.msg.gameUpdate;

import com.twisted.logic.entities.Station;

public class MStationUpd implements MGameUpd{

    //metadata
    public int stationId;

    //high level state
    public int owner;
    public Station.Stage stage;
    public float stageTimer;

    //combat
    public float shieldHealth;
    public float hullHealth;


    /* Exterior Facing Methods */

    public void copyDataToStation(Station s){
        s.owner = owner;
        s.stage = stage;
        s.stageTimer = stageTimer;

        s.shieldHealth = shieldHealth;
        s.hullHealth = hullHealth;
    }

    public static MStationUpd createFromStation(Station s){
        MStationUpd u = new MStationUpd();
        u.stationId = s.getId();

        u.owner = s.owner;
        u.stage = s.stage;
        u.stageTimer = s.stageTimer;

        u.shieldHealth = s.shieldHealth;
        u.hullHealth = s.hullHealth;

        return u;
    }
}
