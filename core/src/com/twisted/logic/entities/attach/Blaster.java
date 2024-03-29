package com.twisted.logic.entities.attach;

import com.badlogic.gdx.math.Vector2;
import com.twisted.util.Asset;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.host.game.ServerGameState;
import com.twisted.logic.mobs.BlasterBolt;

public class Blaster extends TargetedWeapon {

    public final Model model;

    /**
     * Constructor
     */
    public Blaster(Ship attached, Vector2 sourcePoint, Model model) {
        super(attached, sourcePoint);

        this.model = model;
        this.active = false;
    }


    /* Action Methods */

    @Override
    public void tick(ServerGameState state, Grid grid, float frac) {
        super.tick(state, grid, frac);

        //reduce timer
        if(timer > 0) timer -= frac;

        //do stuff while active
        if(active){
            Entity tgt = state.findEntity(target);

            //check if this should be deactivated
            if(tgt == null || attached.pos.dst(tgt.pos) > model.range){
                deactivate();
            }
            //targeting is valid
            else if(isLocked()) {
                //fire
                if(timer <= 0){
                    BlasterBolt bolt = new BlasterBolt(BlasterBolt.chooseModel(this),
                            state.useNextMobileId(), attached.owner,
                            mountPoint.cpy().rotateRad(attached.rot - (float)Math.PI/2).add(attached.pos),
                            this, target);
                    grid.mobiles.put(bolt.id, bolt);

                    timer = model.cooldown;
                }
            }
        }
    }
    @Override
    public void invalidTick(float frac){
        //reduce timer
        if(timer > 0) timer -= frac;
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
        Small(3, 2, 1.4f, 2.5f, 2, 6),  //1 dps
        Medium(4, 5, 1.3f, 4f, 3, 2),  //1.7 dps
        Large(5, 15, 1f, 6f, 6, 1);  //2.5 dps

        //data
        public final float range;
        public final float damage;
        public final float speed;
        public final float maxFlightTime;
        public final float cooldown;
        public final float scanRes;


        //overrides
        @Override
        public Weapon.Type getType(){
            return Type.Blaster;
        }
        @Override
        public float getRange(){
            return range;
        }
        @Override
        public float getScanRes(){
            return scanRes;
        }

        //constructor
        Model(float range, float damage, float speed, float maxFlightTime, float cooldown,
              float scanRes){
            this.range = range;
            this.damage = damage;
            this.speed = speed;
            this.maxFlightTime = maxFlightTime;
            this.cooldown = cooldown;
            this.scanRes = scanRes;
        }

    }

}
