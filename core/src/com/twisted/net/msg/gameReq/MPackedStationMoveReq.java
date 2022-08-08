package com.twisted.net.msg.gameReq;

import com.twisted.logic.descriptors.EntPtr;

public class MPackedStationMoveReq implements MGameReq{

    public EntPtr removeFrom;
    public int idxRemoveFrom;
    public EntPtr addTo;


    public MPackedStationMoveReq(EntPtr removeFrom, int idxRemoveFrom, EntPtr addTo){
        this.idxRemoveFrom = idxRemoveFrom;
        this.removeFrom = removeFrom;
        this.addTo = addTo;
    }

}
