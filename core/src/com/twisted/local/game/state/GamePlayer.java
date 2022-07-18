package com.twisted.local.game.state;

import com.badlogic.gdx.graphics.Color;

/**
 * Clientside representation of a player.
 */
public class GamePlayer {

    //player id
    private final int id;
    public int getId(){
        return id;
    }

    //the player name
    private final String name;
    public String getName() {
        return name;
    }

    //file code
    private final String fileCode;
    public String getFileCode(){
        return fileCode;
    }

    //the color used for the player
    private final Color color;
    public Color getColor(){
        return color;
    }

    /**
     * Constructor
     */
    public GamePlayer(int id, String fileCode, String name, Color color){
        this.id = id;
        this.name = name;
        this.fileCode = fileCode;
        this.color = color;
    }

}
