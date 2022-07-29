package com.twisted.net.msg.gameUpdate;

import com.twisted.logic.entities.Station;

public class MStationUpd implements MGameUpd{

    //metadata
    public int stationId;

    //combat
    public float shieldHealth;
    public float hullHealth;


    /* Exterior Facing Methods */

    public void copyDataToStation(Station s){
        s.shieldHealth = shieldHealth;
        s.hullHealth = hullHealth;
    }

    public static MStationUpd createFromStation(Station s){
        MStationUpd u = new MStationUpd();

        u.stationId = s.getId();
        u.shieldHealth = s.shieldHealth;
        u.hullHealth = s.hullHealth;

        return u;
    }
}
