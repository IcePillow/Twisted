package com.twisted.logic.entities;

import com.twisted.logic.descriptors.Gem;
import com.twisted.logic.entities.attach.StationTransport;
import com.twisted.logic.entities.attach.Weapon;

public class Barge extends Ship {

    /* Data */

    public static final float[] vertices = new float[]{-0.1f,-0.12f, 0f,-0.08f,  0.1f,-0.12f,  0.1f,0.12f,
            -0.1f,0.12f};
    public static final Weapon.Type[] weaponSlots = new Weapon.Type[]{Weapon.Type.StationTransport};


    /* State */

    public final int[] resources;


    /* Constructor */

    public Barge(int shipId, int gridId, int owner, boolean docked) {
        super(shipId, gridId, owner, docked);

        weapons[0] = new StationTransport(this);

        resources = new int[4];
    }


    /* Data Methods */

    @Override
    public float[] getVertices() {
        return vertices;
    }
    @Override
    public float getPaddedLogicalRadius() {
        return 1.4f * 0.156f;
    }
    @Override
    public float getMaxSpeed() {
        return 0.1f;
    }
    @Override
    public float getMaxAccel() {
        return 0.02f;
    }
    @Override
    public float getTargetRange() {
        return 1.5f;
    }
    @Override
    public int getMaxHealth() {
        return 10;
    }
    @Override
    public Weapon.Type[] getWeaponSlots() {
        return weaponSlots;
    }
    /**
     * Amount of space for carrying resources.
     */
    public float getHoldSize(){
        return 10f;
    }


    /* Utility */

    /**
     * @return The max number of additional gems that can be fit.
     */
    public float maxGemsCanFit(Gem gemType){
        float space = getHoldSize();
        for(int i = 0; i<Gem.NUM_OF_GEMS; i++){
            space -= Gem.orderedGems[i].volume * resources[i];
        }

        if(space < 0) return 0;
        else return (float) Math.floor(space/gemType.volume);
    }
}
