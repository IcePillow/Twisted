package com.twisted.net.msg.gameReq;

import com.twisted.logic.entities.Station;

/**
 * Request to start a job.
 */
public class MJobReq implements MGameReq {

    public final int stationGrid;
    public final Station.Job job;

    public MJobReq(int stationGrid, Station.Job job){
        this.stationGrid = stationGrid;
        this.job = job;
    }

}
