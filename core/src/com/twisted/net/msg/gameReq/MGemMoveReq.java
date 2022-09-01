package com.twisted.net.msg.gameReq;

import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Gem;

public class MGemMoveReq implements MGameReq{

    public final EntPtr removeFrom;
    public final Gem gemType;
    public final int amount;
    public final EntPtr addTo;


    public MGemMoveReq(EntPtr removeFrom, Gem gemType, int amount, EntPtr addTo){
        this.removeFrom = removeFrom;
        this.gemType = gemType;
        this.amount = amount;
        this.addTo = addTo;
    }

}
