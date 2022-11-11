package com.twisted.net.msg.summary;

import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.entities.attach.StationTrans;
import com.twisted.logic.entities.attach.Weapon;
import com.twisted.logic.entities.station.Station;

public class WeaponSum implements Summary {

    //required data
    public final boolean active;
    public final float locking;
    public final float timer;
    public final float cooldown;
    public final EntPtr target;

    //optional data
    public Station.Model cargo;

    /**
     * Constructor
     */
    public WeaponSum(boolean active, float locking, float timer, float cooldown, EntPtr target){
        this.active = active;
        this.locking = locking;
        this.timer = timer;
        this.cooldown = cooldown;
        this.target = target;
    }


    /* Exterior Facing */

    public static WeaponSum createFromWeapon(Weapon w){
        WeaponSum s = new WeaponSum(w.isActive(), w.getLockTimer(), w.timer, w.cooldown, w.getTarget());
        if(w instanceof StationTrans) s.cargo = ((StationTrans) w).cargo;

        return s;
    }

    public void copyToWeapon(Weapon w){
        w.setActive(active);
        w.setLockTimer(locking);
        w.timer = timer;
        w.cooldown = cooldown;
        w.setTarget(target);
        if(w instanceof StationTrans) ((StationTrans) w).cargo = cargo;
    }


}
