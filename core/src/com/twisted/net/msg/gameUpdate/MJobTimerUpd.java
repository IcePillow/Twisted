package com.twisted.net.msg.gameUpdate;

import com.twisted.logic.descriptors.CurrentJob;
import com.twisted.logic.entities.station.Station;

import java.util.HashMap;

public class MJobTimerUpd implements MGameUpd {

    public final HashMap<Integer, Float> jobTimers;

    public MJobTimerUpd(){
        jobTimers = new HashMap<>();
    }


    /**
     * Creates an update object with the data on all the jobs at the provided station.
     */
    public static MJobTimerUpd createFromStation(Station st){
        MJobTimerUpd upd = new MJobTimerUpd();

        for(CurrentJob j : st.currentJobs){
            upd.jobTimers.put(j.jobId, j.timeLeft);
        }

        return upd;
    }

}
