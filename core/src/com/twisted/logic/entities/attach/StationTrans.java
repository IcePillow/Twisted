package com.twisted.logic.entities.attach;

import com.twisted.Asset;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.descriptors.events.EvStationStageChange;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.entities.station.Station;
import com.twisted.logic.host.game.ServerGameState;

/**
 * Station transport.
 */
public class StationTrans extends Weapon {

    public Station.Model cargo;

    //state
    public boolean deploying;

    //data
    public final Model model;


    /* Construction */

    public StationTrans(Ship attached, Model model){
        super(attached);

        this.model = model;

        cargo = null;

        deploying = false;
        timer = 0;
    }


    /* Action Methods */

    @Override
    public void tick(ServerGameState state, Grid grid, Ship ship, Entity target, Ship.Targeting targeting,
                     float delta) {
        if(target instanceof Station){
            Station st = (Station) target;

            if(deploying){
                //check tick is still valid
                if(st.stage != Station.Stage.RUBBLE){
                    deactivate();
                    active = false;
                    return;
                }

                //time step
                timer -= delta;

                //finish deployment
                if(timer <= 0){
                    //update the station
                    st.owner = ship.owner;
                    st.stage = Station.Stage.ARMORED;
                    st.hullHealth = st.model.maxHull;
                    st.stageTimer = 30;

                    //update the transport
                    cargo = null;
                    deactivate();
                    active = false;

                    //add event
                    state.addToEventHistory(new EvStationStageChange(st.getId(), Station.Stage.RUBBLE,
                            0, st.stage, st.owner));
                }
            }
            else {
                deploying = true;
                timer = cargo.deployTime;
            }
        }
        else {
            deactivate();
        }
    }

    /**
     * Cancel the deployment.
     */
    @Override
    public void deactivate() {
        super.deactivate();

        deploying = false;
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


    public enum Model implements Weapon.Model {

        Medium(1.5f);

        //data
        public final float range;

        //overrides
        @Override
        public Weapon.Type getType(){
            return Type.StationTrans;
        }
        @Override
        public float getRange(){
            return range;
        }

        /**
         * Constructor
         */
        Model(float range){
            this.range = range;
        }
    }

}
