package com.twisted.net.msg.summary;


import com.twisted.logic.entities.station.Station;

public class StationStartSum implements Summary {

    //data
    public final Station.Model type;
    public final String name;
    public final int owner;
    public final Station.Stage stage;
    public final int[] resources;

    /**
     * Constructor
     */
    protected StationStartSum(Station.Model type, String name, int owner, Station.Stage stage,
                              int[] resources){
        this.type = type;
        this.name = name;
        this.owner = owner;
        this.stage = stage;
        this.resources = resources;
    }

    public static StationStartSum createFromStation(Station s){
        return new StationStartSum(s.model, s.gridNick, s.owner, s.stage, s.resources);
    }

}
