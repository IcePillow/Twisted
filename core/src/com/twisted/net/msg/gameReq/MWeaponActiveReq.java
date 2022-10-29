package com.twisted.net.msg.gameReq;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.descriptors.EntPtr;

public class MWeaponActiveReq implements MGameReq {

    public int grid;
    public int shipId;
    public int weaponId;
    public boolean active;

    public EntPtr target;
    public Vector2 location;

    public MWeaponActiveReq(int grid, int shipId, int weaponId, boolean active,
                            EntPtr target, Vector2 location){
        this.grid = grid;
        this.shipId = shipId;
        this.weaponId = weaponId;
        this.active = active;

        this.target = target;
        this.location = location;
    }

}
