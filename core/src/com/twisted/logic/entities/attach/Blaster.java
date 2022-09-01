package com.twisted.logic.entities.attach;

import com.twisted.Asset;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.host.game.ServerGameState;
import com.twisted.logic.mobs.BlasterBolt;

public class Blaster extends Weapon {

    //data
    public final float damage;
    public final float missileSpeed;
    private final float range;
    private final float fullCooldown;


    /**
     * Constructor
     * @param speed In units per second
     * @param fullCooldown In seconds
     */
    public Blaster(Entity attached, float range, float damage, float speed, float fullCooldown) {
        super(attached);

        this.range = range;
        this.damage = damage;
        this.missileSpeed = speed;
        this.fullCooldown = fullCooldown;

        this.active = false;
    }


    /* Action Methods */

    @Override
    public void tick(ServerGameState state, Grid grid, Ship ship, Entity target, Ship.Targeting targeting,
                     float delta) {
        if(timer > 0){
            timer -= delta;
        }
        else if(target != null && targeting == Ship.Targeting.Locked
                && ship.pos.dst(target.pos) <= range && active) {
            BlasterBolt bolt = new BlasterBolt(state.useNextMobileId(), ship.pos.cpy(), this,
                    target.getEntityType(), target.getId());
            grid.mobiles.put(bolt.id, bolt);

            timer = fullCooldown;
        }
    }
    @Override
    public void putOnFullCooldown(){
        this.timer = fullCooldown;
    }


    /* Data Methods */

    @Override
    public float getMaxRange() {
        return range;
    }
    @Override
    public Asset.UiButton getOffButtonAsset(){
        return Asset.UiButton.BLASTER_OFF;
    }
    @Override
    public Asset.UiButton getOnButtonAsset(){
        return Asset.UiButton.BLASTER_ON;
    }
    @Override
    public float getFullTimer(){
        return fullCooldown;
    }

}
