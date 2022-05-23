package com.twisted.local.game.state;

import com.badlogic.gdx.graphics.Color;

public enum PlayColor {

    BLUE     ("blue", 0x42a5f5ff),
    ORANGE  ("orange", 0xfb8c00ff),
    BLACK   ("", 0x000000ff);

    public final String file;
    public final int value;
    public final Color object;

    PlayColor(String file, int value){
        this.file = file;
        this.value = value;
        this.object = new Color(value);
    }

}
