package com.twisted.net.msg.gameReq;

import com.twisted.logic.descriptors.EntPtr;

public class MWeaponActiveReq implements MGameReq {

    public int grid;
    public int shipId;
    public int weaponId;
    public boolean active;
    public EntPtr target;

    public MWeaponActiveReq(int grid, int shipId, int weaponId, boolean active, EntPtr target){
        this.grid = grid;
        this.shipId = shipId;
        this.weaponId = weaponId;
        this.active = active;
        this.target = target;
    }

}
