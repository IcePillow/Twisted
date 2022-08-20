package com.twisted.logic.entities;

import com.twisted.Asset;
import com.twisted.logic.entities.attach.Blaster;
import com.twisted.logic.entities.attach.Weapon;

public class Frigate extends Ship {

    /* Data */

    public static final float[] vertices = new float[]{-0.08f,-0.08f,  0.08f,-0.08f,  0,0.08f};
    public static final Weapon.Type[] weaponSlots = new Weapon.Type[]{Weapon.Type.Blaster, Weapon.Type.Blaster};

    /**
     * Constructor
     */
    public Frigate(int shipId, int gridId, int owner, boolean docked){
        super(shipId, gridId, owner, docked);

        weapons[0] = new Blaster(3, 2, 1.4f, 2);
        weapons[1] = new Blaster(3, 2, 1.4f, 2);
    }


    /* Data Methods */

    @Override
    public float[] getVertices(){
        return vertices;
    }
    @Override
    public float getMaxSpeed() {
        return 0.8f;
    }
    @Override
    public float getMaxAccel() {
        return 0.4f;
    }
    @Override
    public float getTargetRange(){
        return 3.5f;
    }
    @Override
    public int getMaxHealth(){
        return 6;
    }
    @Override
    public float getPaddedLogicalRadius() {
        return 1.4f * 0.113f;
    }
    @Override
    public Weapon.Type[] getWeaponSlots(){
        return weaponSlots;
    }
    @Override
    public Asset.EntityIcon getIconEnum(){
        return Asset.EntityIcon.FRIGATE;
    }

}
