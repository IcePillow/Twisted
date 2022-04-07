package com.twisted.net.msg.gameRequest;

import com.twisted.logic.entities.Station;

/**
 * Request to start a job.
 */
public class MJobRequest implements MGameRequest {

    public final int stationGrid;
    public final Station.Job job;

    public MJobRequest(int stationGrid, Station.Job job){
        this.stationGrid = stationGrid;
        this.job = job;
    }

}
