package com.twisted.logic.desiptors;

import com.twisted.logic.entities.Station;

import java.io.Serializable;

public class CurrentJob implements Serializable {

    /* Details */

    public final int jobId;
    public final int owner;
    public final Station.Job jobType;
    public final int grid;

    public float timeLeft;

    public CurrentJob(int jobId, int owner, Station.Job jobType, int grid, float timeLeft){
        this.jobId = jobId;
        this.owner = owner;
        this.jobType = jobType;
        this.grid = grid;
        this.timeLeft = timeLeft;
    }

}
