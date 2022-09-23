package com.twisted.local.game.state;

import com.twisted.Asset;
import com.twisted.Paint;

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
    private final Paint paint;
    public Paint getPaint(){
        return paint;
    }

    /**
     * Constructor
     */
    public GamePlayer(int id, Paint paint, String name){
        this.id = id;
        this.name = name;
        this.paint = paint;
    }


    /* Utility */

    public Asset.Circle getMinimapShapeAsset(){
        switch(paint){
            case PL_BLUE:
                return Asset.Circle.CIRCLE_BLUE;
            case PL_ORANGE:
                return Asset.Circle.CIRCLE_ORANGE;
            case PL_GRAY:
                return Asset.Circle.CIRCLE_GRAY;
            default:
                System.out.println("Unexpected player file");
                new Exception().printStackTrace();
                return null;
        }
    }

}
