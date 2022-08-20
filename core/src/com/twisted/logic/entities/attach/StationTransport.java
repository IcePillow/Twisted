package com.twisted.logic.entities.attach;

import com.twisted.Asset;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.Station;
import com.twisted.logic.host.GameHost;

public class StationTransport extends Weapon {

    public Station.Type cargo;

    //state
    public boolean deploying;


    /* Construction */

    public StationTransport(){
        cargo = null;

        deploying = false;
        timer = 0;
    }


    /* Action Methods */

    @Override
    public void tick(GameHost host, Grid grid, Ship ship, Entity target, Ship.Targeting targeting,
                     float delta) {

        if(!active){
            putOnFullCooldown();
        }
        else if(target instanceof Station){
            Station st = (Station) target;

            if(deploying){
                //check tick is still valid
                if(st.stage != Station.Stage.RUBBLE){
                    putOnFullCooldown();
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
                    st.hullHealth = st.getMaxHull();
                    st.stageTimer = 30;

                    //update the transport
                    cargo = null;
                    putOnFullCooldown();
                    active = false;

                    //tell players

                }
            }
            else {
                deploying = true;
                timer = cargo.getDeployTime();
            }
        }
        else {
            active = false;
            putOnFullCooldown();
        }
    }

    /**
     * Cancel the deployment.
     */
    @Override
    public void putOnFullCooldown() {
        deploying = false;
        timer = 0;
    }


    /* Data Methods */

    @Override
    public float getMaxRange() {
        return 1.5f;
    }
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
        else return cargo.getDeployTime();
    }
}
