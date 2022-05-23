package com.twisted.local.game.state;

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

    //the color used for the player
    public PlayColor color;

    /**
     * Constructor
     */
    public GamePlayer(int id, String name){
        this.id = id;
        this.name = name;

        this.color = PlayColor.BLACK;
    }

}
