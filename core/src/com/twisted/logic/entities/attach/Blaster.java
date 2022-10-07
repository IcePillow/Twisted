package com.twisted.logic.entities.attach;

import com.twisted.Asset;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.host.game.ServerGameState;
import com.twisted.logic.mobs.BlasterBolt;

public class Blaster extends Weapon {

    public final Model model;

    /**
     * Constructor
     */
    public Blaster(Ship attached, Model model) {
        super(attached);

        this.model = model;
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
                && ship.pos.dst(target.pos) <= model.range && active) {
            BlasterBolt bolt = new BlasterBolt(state.useNextMobileId(), ship.pos.cpy(), this,
                    EntPtr.createFromEntity(target));
            grid.mobiles.put(bolt.id, bolt);

            timer = model.cooldown;
        }
    }
    @Override
    public void putOnFullCooldown(){
        this.timer = model.cooldown;
    }


    /* Typing Methods */

    @Override
    public Weapon.Model subtype(){
        return model;
    }


    /* Data Methods */

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
        return model.cooldown;
    }


    /* Enums */

    public enum Model implements Weapon.Model {

        Small(3, 2, 1.4f, 2.5f, 2),  //1 dps
        Medium(4, 5, 1.3f, 4f, 3),  //1.7 dps
        Large(5, 15, 1f, 6f, 6);  //2.5 dps

        //data
        public final float range;
        public final float damage;
        public final float speed;
        public final float maxFlightTime; //max
        public final float cooldown;


        //overrides
        @Override
        public Weapon.Type getType(){
            return Type.Blaster;
        }
        @Override
        public float getRange(){
            return range;
        }

        //constructor
        Model(float range, float damage, float speed, float maxFlightTime, float cooldown){
            this.range = range;
            this.damage = damage;
            this.speed = speed;
            this.maxFlightTime = maxFlightTime;
            this.cooldown = cooldown;
        }

    }

}
