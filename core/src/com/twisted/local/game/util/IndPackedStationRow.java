package com.twisted.local.game.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.twisted.local.game.SecIndustry;
import com.twisted.logic.entities.Station;

public class IndPackedStationRow extends IndustryRow {

    //storage
    private final Station.Type type;


    //constructor
    public IndPackedStationRow(SecIndustry sector, Skin skin, GlyphLayout glyph, float width,
                               Station.Type type){
        super(sector, skin, glyph, width);

        //copy
        this.type = type;

        //initialize
        initGraphics(skin);
    }

    private void initGraphics(Skin skin){
        Label nameLabel = new Label(type.name(), skin, "small", Color.LIGHT_GRAY);
        this.addActor(nameLabel);

        Actor filler = new Actor();
        this.addActor(filler);
    }


    @Override
    public void update() {}

    @Override
    public boolean matches(Station.Type check){
        return type == check;
    }

}
