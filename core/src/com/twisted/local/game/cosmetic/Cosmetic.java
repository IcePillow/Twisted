package com.twisted.local.game.cosmetic;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.twisted.local.game.Game;
import com.twisted.logic.descriptors.Grid;

public abstract class Cosmetic {

    public final static float LTR = Game.LTR;
    public final int gridId;

    protected Cosmetic(int gridId){
        this.gridId = gridId;
    }

    /**
     * Called every tick until this cosmetic is removed.
     * @return True to continue existing. False to be removed.
     */
    public abstract boolean tick(float delta);
    /**
     * Called if viewport is looking at this cosmetic's grid.
     */
    public abstract void draw(ShapeRenderer shape, Grid g);

}
