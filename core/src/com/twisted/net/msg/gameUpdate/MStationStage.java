package com.twisted.net.msg.gameUpdate;

import com.twisted.logic.entities.Station;

public class MStationStage implements MGameUpd{

    public int stationId;

    public int owner;
    public Station.Stage stage;

    public MStationStage(int stationId) {
        this.stationId = stationId;
    }


    /* External Methods */

    public static MStationStage createFromStation(Station s){
        MStationStage m = new MStationStage(s.getId());

        m.owner = s.owner;
        m.stage = s.stage;

        return m;
    }

}
