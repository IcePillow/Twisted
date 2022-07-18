package com.twisted.local.game.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;

public class JobRow extends HorizontalGroup {

    private Label jobName, timer;
    private Actor filler1;

    private final GlyphLayout glyph;
    private final Skin skin;


    /* Constructing */

    public JobRow(Skin skin, GlyphLayout glyph){
        super();

        this.skin = skin;
        this.glyph = glyph;

        initGraphics(skin);
    }

    private void initGraphics(Skin skin){
        timer = new Label("X", skin, "small", Color.LIGHT_GRAY);
        this.addActor(timer);

        filler1 = new Actor();
        filler1.setWidth(0);
        this.addActor(filler1);

        jobName = new Label("X", skin, "small", Color.LIGHT_GRAY);
        this.addActor(jobName);
    }


    /* Updates */

    public void updateTimer(String text){
        timer.setText(text);

        glyph.setText(skin.getFont("small"), text);
        filler1.setWidth(30-glyph.width);
    }

    public void updateName(String text){
        jobName.setText(text);
    }

}
