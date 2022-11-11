package com.twisted.util;

import com.badlogic.gdx.graphics.Color;

import java.io.Serializable;

public enum Paint implements Serializable {

    /* Color Values */

    //player colors
    PL_GRAY(0x9d9d9dff), //empty player
    PL_BLUE(0x42a5f5ff),
    PL_ORANGE(0xd67700ff),
    //dimmed and brightened player colors
    PL_GRAY_DIM(0x4d4d4dff),
    PL_GRAY_BRIGHT(0x4f4f4fff),
    PL_BLUE_DIM(0x085391ff),
    PL_BLUE_BRIGHT(0xa8d5faff),
    PL_ORANGE_DIM(0x6b3b00ff),
    PL_ORANGE_BRIGHT(0xff9a1fff),

    //game colors
    DEEP_SPACE(0x060612ff),
    SPACE(0x0c0c24ff),
    LASER_YELLOW(0xffff77ff),
    LASER_BEAM_YELLOW(0xffff00ff),
    DOOMSDAY_CHARGE_GRAY(0xbbbbbbff),

    //ui utility
    TITLE_PURPLE(0x613369ff),
    HEALTH_GREEN(0x008500ff),
    MENU_A1(0x000000aa),
    MENU_A2(0x00000054), //sync Asset.Pixel.MENU_A2

    //extra constants
    VERY_LIGHT_GRAY(0xe6e6e6ff);

    //storage, should not be modified
    public final Color c;

    /**
     * Constructor
     */
    Paint(int c){
        this.c = new Color(c);
    }


    /* Utility */

    public enum Collect {
        BLUE(PL_BLUE, PL_BLUE_DIM, PL_BLUE_BRIGHT),
        ORANGE(PL_ORANGE, PL_ORANGE_DIM, PL_ORANGE_BRIGHT),
        GRAY(PL_GRAY, PL_GRAY_DIM, PL_GRAY_BRIGHT);

        public final Paint base;
        public final Paint dimmed, brightened;

        Collect(Paint base, Paint dimmed, Paint brightened){
            this.base = base;
            this.dimmed = dimmed;
            this.brightened = brightened;
        }
    }

}
