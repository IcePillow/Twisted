package com.twisted.vis.state;

import com.badlogic.gdx.graphics.Color;

public class GamePlayer {

    //the player id
    private String name;
    public String getName() {
        return name;
    }

    //the color used for the player
    public GameState.PlayerColor color;

    /**
     * Constructor
     */
    public GamePlayer(String name){
        this.name = name;

        this.color = GameState.PlayerColor.BLACK;
    }

}
