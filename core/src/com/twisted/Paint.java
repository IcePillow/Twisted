package com.twisted;

import com.badlogic.gdx.graphics.Color;

import java.io.Serializable;

public enum Paint implements Serializable {

    //player colors
    PL_GRAY(0x9d9d9dff), //empty player
    PL_BLUE(0x42a5f5ff),
    PL_ORANGE(0xd67700ff),

    //game colors
    SPACE(0x0c0c26ff),

    //ui utility
    TITLE_PURPLE(0x613369ff),
    HEALTH_GREEN(0x008500ff),
    MENU_A1(0x000000aa),
    MENU_A2(0x00000054), //sync Asset.Pixel.MENU_A2

    //extra constants
    VERY_LIGHT_GRAY(0xe6e6e6ff);

    //storage, should not be modified
    public final Color col;

    /**
     * Constructor
     */
    Paint(int col){
        this.col = new Color(col);
    }
}
