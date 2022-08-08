package com.twisted.local.game.util;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.twisted.local.game.SecIndustry;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.Station;

public abstract class IndustryRow extends HorizontalGroup {

    //ui tools
    protected final SecIndustry sector;
    protected final GlyphLayout glyph;
    protected final Skin skin;
    protected final float width;

    protected IndustryRow(SecIndustry sector, Skin skin, GlyphLayout glyph, float width){
        super();

        //copy values
        this.sector = sector;
        this.skin = skin;
        this.glyph = glyph;
        this.width = width;
    }


    /* Update Functions */

    public abstract void update();


    /* Matching Functions */

    public boolean matches(Ship check){
        return false;
    }
    public boolean matches(Station.Type check){
        return false;
    }

}
