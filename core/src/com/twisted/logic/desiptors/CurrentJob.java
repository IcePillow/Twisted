package com.twisted.logic.desiptors;

import com.twisted.logic.entities.Station;

public class CurrentJob {

    public final int jobId;
    public final int playerId;
    public final Station.Job job;
    public final int grid;

    public float timeLeft;

    public CurrentJob(int jobId, int playerId, Station.Job job, int grid, float timeLeft){
        this.jobId = jobId;
        this.playerId = playerId;
        this.job = job;
        this.grid = grid;
        this.timeLeft = timeLeft;
    }

}
