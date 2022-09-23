package com.twisted.net.msg.gameReq;

import com.twisted.logic.descriptors.JobType;
import com.twisted.logic.entities.station.Station;

/**
 * Request to start a job.
 */
public class MJobReq implements MGameReq {

    public final int stationGrid;
    public final JobType job;

    public MJobReq(int stationGrid, JobType job){
        this.stationGrid = stationGrid;
        this.job = job;
    }

}
