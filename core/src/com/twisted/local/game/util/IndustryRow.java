package com.twisted.local.game.util;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.twisted.local.game.SecIndustry;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.entities.station.Station;

public abstract class IndustryRow extends Table {

    //ui tools
    protected final SecIndustry sector;
    protected final float width;

    protected IndustryRow(SecIndustry sector, float width){
        super();

        //copy values
        this.sector = sector;
        this.width = width;
    }


    /* Update Functions */

    public abstract void update();


    /* Matching Functions */

    public boolean matches(Ship check){
        return false;
    }
    public boolean matches(Station.Model check){
        return false;
    }

}
