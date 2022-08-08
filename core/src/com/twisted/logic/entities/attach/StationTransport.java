package com.twisted.logic.entities.attach;

import com.twisted.Asset;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.Station;
import com.twisted.logic.host.GameHost;

public class StationTransport extends Weapon {

    public Station.Type cargo;


    /* Construction */

    public StationTransport(){
        cargo = null;
    }


    /* Action Methods */

    @Override
    public void tick(GameHost host, Grid grid, Ship ship, Entity target, Ship.Targeting targeting,
                     float delta) {}
    @Override
    public void putOnFullCooldown() {}


    /* Data Methods */

    @Override
    public float getMaxRange() {
        return 1;
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
}
