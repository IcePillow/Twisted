package com.twisted.net.msg.gameUpdate;

import com.twisted.logic.descriptors.CurrentJob;

/**
 * Adding or removing a CurrentJob to a given station.
 */
public class MChangeJob implements MGameUpd {

    public final Action action;
    public final int jobId;
    public final int gridId;
    public final CurrentJob job;

    /**
     * Constructor.
     * @param job If adding, it is the job that is being added. Otherwise, null.
     */
    public MChangeJob(Action action, int jobId, int gridId, CurrentJob job){
        this.action = action;
        this.jobId = jobId;
        this.gridId = gridId;
        this.job = job;
    }

    public enum Action {
        ADDING,
        CANCELING,
        FINISHED
    }

}
