package com.twisted.logic.entities.attach;

import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.host.GameHost;
import com.twisted.logic.mobs.BlasterBolt;

public class Blaster implements Weapon {

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
    }


    /* Data Methods */

    @Override
    public float getMaxRange() {
        return range;
    }


    /* Action Methods */

    @Override
    public void fire(GameHost host, Grid grid, Ship ship, Entity target, float delta) {
        if(cooling > 0){
            cooling -= delta;
        }
        else {
            BlasterBolt bolt = new BlasterBolt(host.useNextMobileId(), ship.pos.cpy(),
                    this, target.getEntityType(), target.getId());
            grid.mobiles.put(bolt.id, bolt);

            cooling = fullCooldown;
        }
    }
}
