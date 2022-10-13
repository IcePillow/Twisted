package com.twisted.logic.entities.attach;

import com.twisted.Asset;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.host.game.ServerGameState;

public class Laser extends Weapon{

    public final Model model;

    //graphics only
    public boolean cosmeticBeamExists;

    //state
    private float curDmg;

    /**
     * Constructor
     */
    public Laser(Ship attached, Model model){
        super(attached);

        this.model = model;
        this.active = false;

        this.cosmeticBeamExists = false;
    }

    /* Action Methods */

    @Override
    public void tick(ServerGameState state, Grid grid, Ship ship, Entity target, Ship.Targeting targeting,
                     float delta) {
        if(target != null && targeting == Ship.Targeting.Locked &&
                ship.pos.dst(target.pos) <= model.range && active){
            //deal damage
            target.takeDamage(grid, attached.owner, curDmg);

            //heat up
            curDmg += model.heatUp;
            if(curDmg > model.maxDmg) curDmg = model.maxDmg;

            //display
            if(!cosmeticBeamExists){
                //do stuff

                cosmeticBeamExists = true;
            }
        }
        else {
            curDmg = model.minDmg;
        }
    }


    /* Typing Methods */

    @Override
    public Weapon.Model subtype() {
        return model;
    }


    /* Data Methods */

    @Override
    public Asset.UiButton getOffButtonAsset() {
        return Asset.UiButton.LASER_OFF;
    }
    @Override
    public Asset.UiButton getOnButtonAsset() {
        return Asset.UiButton.LASER_ON;
    }
    @Override
    public float getFullTimer() {
        return 0;
    }


    public enum Model implements Weapon.Model {
        Large(3.5f, 0.05f, 0.25f, 0.00125f);  //1-5dps over 8sec

        //data
        public final float range;
        public final float minDmg;
        public final float maxDmg;
        public final float heatUp;

        //overrides
        @Override
        public Weapon.Type getType(){
            return Type.Laser;
        }
        @Override
        public float getRange(){
            return range;
        }

        //constructor
        Model(float range, float minDmg, float maxDmg, float heatUp){
            this.range = range;
            this.minDmg = minDmg;
            this.maxDmg = maxDmg;
            this.heatUp = heatUp;
        }
    }

}
