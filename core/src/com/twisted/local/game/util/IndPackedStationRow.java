package com.twisted.local.game.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.twisted.Asset;
import com.twisted.local.game.SecIndustry;
import com.twisted.logic.entities.station.Station;

public class IndPackedStationRow extends IndustryRow {

    //storage
    private final Station.Model type;


    //constructor
    public IndPackedStationRow(SecIndustry sector, float width, Station.Model type){
        super(sector, width);

        //copy
        this.type = type;

        //initialize
        initGraphics();
    }

    private void initGraphics(){
        Label nameLabel = new Label(type.name(), Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        nameLabel.setColor(Color.LIGHT_GRAY);
        add(nameLabel).expandX().left();
    }


    @Override
    public void update() {}

    @Override
    public boolean matches(Station.Model check){
        return type == check;
    }

}
