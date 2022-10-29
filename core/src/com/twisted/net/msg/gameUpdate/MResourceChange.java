package com.twisted.net.msg.gameUpdate;

import com.twisted.logic.descriptors.EntPtr;

import java.util.Arrays;

public class MResourceChange implements MGameUpd {

    public final EntPtr entity;
    public final int[] resources;

    public MResourceChange(EntPtr entity, int[] resources){
        this.entity = entity;
        this.resources = Arrays.copyOf(resources, resources.length);
    }

}
