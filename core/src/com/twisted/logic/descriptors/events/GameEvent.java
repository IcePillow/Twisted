package com.twisted.logic.descriptors.events;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.twisted.Asset;
import com.twisted.Main;
import com.twisted.local.game.state.ClientGameState;

import java.io.Serializable;

public abstract class GameEvent implements Serializable {

    //fields
    public float timeStamp;


    /* Utility */

    public Label timeForCurtain(){
        int durMin = ((int) timeStamp) / 60;
        int durSec = ((int) timeStamp) % 60;
        Label timeLabel = new Label(durMin + ":" + ((durSec<10?("0"+durSec):(durSec))),
                Asset.labelStyle(Asset.Avenir.MEDIUM_12));
        timeLabel.setColor(Color.DARK_GRAY);

        return timeLabel;
    }

    public abstract HorizontalGroup describeForCurtain(ClientGameState state);

    /* Typing */

    public abstract Type getType();

    public enum Type {
        GAME_END,
        STATION_STAGE_CHANGE,
    }

}
