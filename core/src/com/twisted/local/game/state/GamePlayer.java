package com.twisted.local.game.state;

import com.twisted.Asset;

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

    //file
    private final PlayerFile file;
    public PlayerFile getFile(){
        return file;
    }

    /**
     * Constructor
     */
    public GamePlayer(int id, PlayerFile file, String name){
        this.id = id;
        this.name = name;
        this.file = file;
    }


    /* Utility */

    public Asset.Shape getMinimapShapeAsset(){
        switch(file){
            case BLUE:
                return Asset.Shape.CIRCLE_BLUE;
            case ORANGE:
                return Asset.Shape.CIRCLE_ORANGE;
            case GRAY:
                return Asset.Shape.CIRCLE_GRAY;
            default:
                System.out.println("Unexpected player file");
                new Exception().printStackTrace();
                return null;
        }
    }

}
