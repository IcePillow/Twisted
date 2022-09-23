package com.twisted.logic.entities.station;

import com.twisted.logic.entities.station.Station;

public class Harvester extends Station {

    /**
     * Constructor
     */
    public Harvester(int grid, String gridNick, int owner, Stage stage) {
        super(Model.Harvester, grid, gridNick, owner, stage);

    }

}
