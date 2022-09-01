package com.twisted.net.msg.gameReq;

import com.twisted.logic.descriptors.EntPtr;

public class MPackedStationMoveReq implements MGameReq{

    public final EntPtr removeFrom;
    public final int idxRemoveFrom;
    public final EntPtr addTo;


    public MPackedStationMoveReq(EntPtr removeFrom, int idxRemoveFrom, EntPtr addTo){
        this.idxRemoveFrom = idxRemoveFrom;
        this.removeFrom = removeFrom;
        this.addTo = addTo;
    }

}
