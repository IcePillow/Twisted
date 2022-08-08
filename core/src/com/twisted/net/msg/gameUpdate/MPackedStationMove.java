package com.twisted.net.msg.gameUpdate;

import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.entities.Station;

public class MPackedStationMove implements MGameUpd {

    /**
     * Entity to remove this from. Can be null.
     */
    public EntPtr removeFrom;
    public int idxRemoveFrom;
    /**
     * Entity to add this to. Can be null.
     */
    public EntPtr addTo;
    public int idxAddTo;

    public Station.Type type;


    public MPackedStationMove(EntPtr removeFrom, int idxRemoveFrom, EntPtr addTo, int idxAddTo,
                              Station.Type type){
        this.idxRemoveFrom = idxRemoveFrom;
        this.removeFrom = removeFrom;
        this.addTo = addTo;
        this.idxAddTo = idxAddTo;
        this.type = type;
    }

}
