package com.twisted.net.msg.gameUpdate;

import com.twisted.logic.descriptors.EntPtr;

public class MResourceChange implements MGameUpd {

    public final EntPtr entity;
    /**
     * The change in the resource amounts.
     */
    public final int[] resourceChanges;

    public MResourceChange(EntPtr entity){
        this.entity = entity;
        this.resourceChanges = new int[4];
    }

}
