package com.twisted.local.game.state;

import com.twisted.util.Asset;
import com.twisted.util.Paint;
import com.twisted.util.Quirk;

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
    private final Paint.Collect collect;
    public Paint.Collect getCollect(){
        return collect;
    }

    /**
     * Constructor
     */
    public GamePlayer(int id, Paint.Collect collect, String name){
        this.id = id;
        this.name = name;
        this.collect = collect;
    }


    /* Utility */

    public Asset.Circle getMinimapShapeAsset(){
        switch(collect){
            case BLUE:
                return Asset.Circle.CIRCLE_BLUE;
            case ORANGE:
                return Asset.Circle.CIRCLE_ORANGE;
            case GRAY:
                return Asset.Circle.CIRCLE_GRAY;
            default:
                new Quirk(Quirk.Q.UnknownGameData).print();
                return null;
        }
    }

}
