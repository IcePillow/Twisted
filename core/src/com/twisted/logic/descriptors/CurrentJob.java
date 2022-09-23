package com.twisted.logic.descriptors;

import java.io.Serializable;

public class CurrentJob implements Serializable {

    /* Details */

    public final int jobId;
    public final int owner;
    public final JobType jobType;
    public final int grid;

    public float timeLeft;
    public boolean blocking;

    public CurrentJob(int jobId, int owner, JobType jobType, int grid, float timeLeft){
        this.jobId = jobId;
        this.owner = owner;
        this.jobType = jobType;
        this.grid = grid;
        this.timeLeft = timeLeft;
    }

}
