package com.twisted.logic.entities.attach;

import com.badlogic.gdx.math.Vector2;
import com.twisted.util.Asset;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.descriptors.events.EvStationStageChange;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.entities.station.Station;
import com.twisted.logic.host.game.ServerGameState;

/**
 * Station transport.
 */
public class StationTrans extends TargetedWeapon {

    //state
    public Station.Model cargo;

    //data
    public final Model model;


    /* Construction */

    public StationTrans(Ship attached, Vector2 sourcePoint, Model model){
        super(attached,sourcePoint);

        this.model = model;

        cargo = null;

        timer = 0;
    }


    /* Action Methods */

    @Override
    public void tick(ServerGameState state, Grid grid, float frac) {
        super.tick(state, grid, frac);

        if(active){
            Entity tgt = state.findEntity(target);

            //check if this should be deactivated
            if(!(tgt instanceof Station) || cargo == null || attached.pos.dst(tgt.pos) > model.range){
                deactivate();
            }
            //targeting is valid
            else if(isLocked()) {
                Station st = (Station) tgt;

                //check tick is still valid
                if(st.stage != Station.Stage.RUBBLE){
                    deactivate();
                    return;
                }

                //time step
                timer -= frac;

                //finish deployment
                if(timer <= 0){
                    //update the station
                    st.owner = attached.owner;
                    st.stage = Station.Stage.ARMORED;
                    st.hullHealth = st.model.maxHull;
                    st.stageTimer = 30;

                    //update the transport
                    cargo = null;
                    deactivate();

                    //add event
                    state.addToEventHistory(new EvStationStageChange(st.getId(), Station.Stage.RUBBLE,
                            0, st.stage, st.owner));
                }
            }
        }
    }
    @Override
    public void activate(Entity entity, Vector2 location){
        super.activate(entity, location);

        timer = cargo.deployTime;
    }
    @Override
    public void deactivate() {
        super.deactivate();

        timer = 0;
    }


    /* Typing Methods */

    public Weapon.Model subtype(){
        return model;
    }


    /* Data Methods */

    @Override
    public Asset.UiButton getOffButtonAsset() {
        if(cargo == null) return Asset.UiButton.DEFAULT;

        switch(cargo){
            case Extractor:
                return Asset.UiButton.EXTRACTOR_OFF;
            case Harvester:
                return Asset.UiButton.HARVESTER_OFF;
            case Liquidator:
                return Asset.UiButton.LIQUIDATOR_OFF;
            default:
                return Asset.UiButton.DEFAULT;
        }
    }
    @Override
    public Asset.UiButton getOnButtonAsset() {
        if(cargo == null) return Asset.UiButton.DEFAULT;

        switch(cargo){
            case Extractor:
                return Asset.UiButton.EXTRACTOR_ON;
            case Harvester:
                return Asset.UiButton.HARVESTER_ON;
            case Liquidator:
                return Asset.UiButton.LIQUIDATOR_ON;
            default:
                return Asset.UiButton.DEFAULT;
        }
    }
    @Override
    public float getFullTimer(){
        if(cargo == null) return 0;
        else return cargo.deployTime;
    }
    @Override
    public boolean requiresTarget() {
        return true;
    }


    public enum Model implements Weapon.Model {

        Medium(1.5f, 0.8f);

        //data
        public final float range;
        public final float scanRes;

        //overrides
        @Override
        public Weapon.Type getType(){
            return Type.StationTrans;
        }
        @Override
        public float getRange(){
            return range;
        }
        @Override
        public float getScanRes(){
            return scanRes;
        }

        /**
         * Constructor
         */
        Model(float range, float scanRes){
            this.range = range;
            this.scanRes = scanRes;
        }
    }

}
