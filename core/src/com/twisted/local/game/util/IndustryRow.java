package com.twisted.local.game.util;

import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.twisted.local.game.SecIndustry;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.Station;

public abstract class IndustryRow extends HorizontalGroup {

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
