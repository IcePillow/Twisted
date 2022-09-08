package com.twisted.local.game.util;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.twisted.Asset;
import com.twisted.Main;

public class JobRow extends HorizontalGroup {

    private Label jobName, timer;
    private Actor filler1;


    /* Constructing */

    public JobRow(){
        super();

        initGraphics();
    }

    private void initGraphics(){
        timer = new Label("X", Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        this.addActor(timer);

        filler1 = new Actor();
        filler1.setWidth(0);
        this.addActor(filler1);

        jobName = new Label("X", Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        this.addActor(jobName);
    }


    /* Updates */

    public void updateTimer(String text){
        timer.setText(text);

        Main.glyph.setText(timer.getStyle().font, text);
        filler1.setWidth(30-Main.glyph.width);
    }

    public void updateName(String text){
        jobName.setText(text);
    }

}
