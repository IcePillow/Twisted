package com.twisted.net.msg.gameReq;

public class MWeaponActiveReq implements MGameReq {

    public int grid;
    public int shipId;
    public int weaponId;
    public boolean active;

    public MWeaponActiveReq(int grid, int shipId, int weaponId, boolean active){
        this.grid = grid;
        this.shipId = shipId;
        this.weaponId = weaponId;
        this.active = active;
    }

}
