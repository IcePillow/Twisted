package com.twisted.logic.entities.attach;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.mobs.DoomsdayBlast;
import com.twisted.util.Asset;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.host.game.ServerGameState;

public class Doomsday extends TargetGrdWeapon {

    public final Model model;
    public Vector2 groundTarget;


    /**
     * Constructor
     */
    public Doomsday(Ship attached, Vector2 sourcePoint, Model model) {
        super(attached, sourcePoint);

        this.model = model;
        this.groundTarget = new Vector2();
    }


    /* Action Methods */

    @Override
    public void tick(ServerGameState state, Grid grid, float frac) {
        super.tick(state, grid, frac);

        if(active){
            timer -= frac;

            //check movement
            if(attached.vel.len() > 0){
                deactivate();
            }

            //fire
            if(timer < 0){
                //create doomsday blast
                DoomsdayBlast mob = new DoomsdayBlast(DoomsdayBlast.chooseModel(this),
                        state.useNextMobileId(), attached.owner,
                        mountPoint.cpy().rotateRad(attached.rot - (float)Math.PI/2).add(attached.pos),
                        this, groundTarget);
                grid.mobiles.put(mob.id, mob);

                //deactivate
                cooldown = model.coolTime;
                deactivate();
            }
        }
    }
    @Override
    public void invalidTick(float frac){

    }
    @Override
    public void activate(Entity entity, Vector2 location){
        super.activate(entity, location);

        //time and target updating
        timer = model.chargeTime;
        groundTarget.set(location);

        //orient
        attached.rot = (float)Math.atan2(groundTarget.y-attached.pos.y, groundTarget.x-attached.pos.x);
    }
    @Override
    public void deactivate(){
        super.deactivate();
    }


    /* Typing Methods */

    @Override
    public Model subtype() {
        return model;
    }


    /* Data Methods */

    @Override
    public Asset.UiButton getOffButtonAsset() {
        return Asset.UiButton.DOOMSDAY_OFF;
    }
    @Override
    public Asset.UiButton getOnButtonAsset() {
        return Asset.UiButton.DOOMSDAY_ON;
    }
    @Override
    public float getFullTimer() {
        return model.chargeTime;
    }
    @Override
    public float getFullCooldown(){
        return model.coolTime;
    }


    public enum Model implements Weapon.Model {
        Capital(5, 0.8f, 1.5f,
                3, 60, 50, 10, 1.8f);

        //data
        public final float range, flightSpd;
        public final float innerBlastRadius, outerBlastRadius;
        public final float chargeTime, coolTime;
        public final float maxDmg, minDmg;

        @Override
        public Type getType() {
            return Type.Doomsday;
        }
        @Override
        public float getRange() {
            return range;
        }
        @Override
        public float getScanRes(){
            return 1;
        }

        /**
         * @param chargeTime Time it takes to charge up, during which any actions will interrupt the charge.
         * @param coolTime Allowed time between blasts.
         */
        Model(float range, float innerBlastRadius, float outerBlastRadius, float chargeTime, float coolTime,
              float maxDmg, float minDmg, float flightSpd){
            this.range = range;
            this.innerBlastRadius = innerBlastRadius;
            this.outerBlastRadius = outerBlastRadius;
            this.chargeTime = chargeTime;
            this.coolTime = coolTime;
            this.maxDmg = maxDmg;
            this.minDmg = minDmg;
            this.flightSpd = flightSpd;
        }
    }
}
