package com.twisted.local.game.state;

import com.badlogic.gdx.graphics.Color;

public enum PlayerFile {

    GRAY("gray", new Color(0x9d9d9dff)),
    BLUE("blue", new Color(0x42a5f5ff)),
    ORANGE("orange", new Color(0xfb8c00ff));

    public final String filepath;
    public final Color color;

    PlayerFile(String filepath, Color color){
        this.filepath = filepath;
        this.color = color;
    };

}
