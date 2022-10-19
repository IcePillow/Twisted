package com.twisted.local.game.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.twisted.Asset;

public class JobRow extends Table {

    private Label jobName, timer;


    /* Constructing */

    public JobRow(){
        super();

        initGraphics();
    }

    private void initGraphics(){
        timer = new Label("X", Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        timer.setColor(Color.GRAY);
        add(timer).minWidth(25);

        jobName = new Label("X", Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        jobName.setColor(Color.LIGHT_GRAY);
        add(jobName).expandX().left();
    }


    /* Updates */

    public void updateTimer(String text){
        timer.setText(text);

    }
    public void updateName(String text){
        jobName.setText(text);
    }

}
