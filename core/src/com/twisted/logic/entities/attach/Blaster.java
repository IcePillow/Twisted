package com.twisted.logic.entities.attach;

import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.host.GameHost;
import com.twisted.logic.mobs.BlasterBolt;

public class Blaster extends Weapon {

    //data
    public final float damage;
    public final float missileSpeed;
    private final float range;
    private final float fullCooldown;

    //state
    private float cooling; //can only fire if cooling <= 0


    /**
     * Constructor
     * @param speed In units per second
     * @param fullCooldown In seconds
     */
    public Blaster(float range, float damage, float speed, float fullCooldown) {
        this.range = range;
        this.damage = damage;
        this.missileSpeed = speed;
        this.fullCooldown = fullCooldown;

        this.active = false;
    }


    /* Data Methods */

    @Override
    public float getMaxRange() {
        return range;
    }


    /* Action Methods */

    @Override
    public void tick(GameHost host, Grid grid, Ship ship, Entity target,
                     Ship.Targeting targeting, float delta) {
        if(cooling > 0){
            cooling -= delta;
        }
        else if(target != null && targeting == Ship.Targeting.Locked
                && ship.pos.dst(target.pos) <= range && active) {
            BlasterBolt bolt = new BlasterBolt(host.useNextMobileId(), ship.pos.cpy(),
                    this, target.getEntityType(), target.getId());
            grid.mobiles.put(bolt.id, bolt);

            cooling = fullCooldown;
        }
    }

    @Override
    public void putOnFullCooldown(){
        this.cooling = fullCooldown;
    }
}
