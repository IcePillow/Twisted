package com.twisted.logic;

import com.badlogic.gdx.scenes.scene2d.ui.Image;

import java.io.Serializable;

public class Station implements Serializable {

    /* Graphics */

    public Image minimapSprite;


    /* Variables */

    public final int grid;
    public final Type type;

    public String owner;
    public Stage stage;


    /* Methods */

    /**
     * Constructor
     */
    public Station(int grid, Type type, String owner, Stage stage){
        this.grid = grid;
        this.type = type;
        this.owner = owner;
        this.stage = stage;
    }


    /**
     * The type of station that this is.
     */
    public enum Type {
        EXTRACTOR,
        HARVESTER,
        LIQUIDATOR
    }

    /**
     * The stage that the station is currently in.
     */
    public enum Stage {
        NONE,
        DEPLOYMENT,
        ARMORED,
        REINFORCED,
        VULNERABLE
    }

}
