package com.twisted.local.game.cosmetic;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.twisted.local.game.Game;

public abstract class Cosmetic {

    public final static float LTR = Game.LTR;

    public final int gridId;

    protected Cosmetic(int gridId){
        this.gridId = gridId;
    }

    /**
     * @return True to continue existing. False to be removed from the viewport.
     */
    public boolean renderShape(float delta, ShapeRenderer shape){
        return false;
    }

}
