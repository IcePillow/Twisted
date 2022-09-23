package com.twisted.local.game.cosmetic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.twisted.local.game.Game;
import com.twisted.logic.descriptors.Grid;

public class Explosion extends Cosmetic {

    //required parameters
    private final float size;
    private final float duration;
    private final Vector2 pos;

    //optional parameters
    public Color color = Color.LIGHT_GRAY;

    //state
    private float elapsed;


    /**
     * Constructor
     */
    public Explosion(int gridId, float size, float duration, Vector2 pos){
        super(gridId);

        this.size = size;
        this.duration = duration;
        this.pos = pos;

        elapsed = 0;
    }

    @Override
    public void draw(ShapeRenderer shape, Grid g){
        float percent = elapsed / duration;

        shape.setColor(color);
        for(float i=90; i<432; i+=72){
            shape.line(LTR * (pos.x + percent*size*(float)Math.cos(Math.PI*i/180)),
                    LTR * (pos.y + percent*size*(float)Math.sin(Math.PI*i/180)),
                    LTR * (pos.x + (percent+0.15f)*size*(float)Math.cos(Math.PI*i/180)),
                    LTR * (pos.y + (percent+0.15f)*size*(float)Math.sin(Math.PI*i/180)));
        }
        if(percent > 0.3f){
            for(float i=45; i<387; i+=72){
                shape.line(LTR * (pos.x + (0.8f*percent-0.3f)*size*(float)Math.cos(Math.PI*i/180)),
                        LTR * (pos.y + (0.8f*percent-0.3f)*size*(float)Math.sin(Math.PI*i/180)),
                        LTR * (pos.x + (0.8f*percent-0.2f)*size*(float)Math.cos(Math.PI*i/180)),
                        LTR * (pos.y + (0.8f*percent-0.2f)*size*(float)Math.sin(Math.PI*i/180)));
            }
        }
    }

    @Override
    public boolean tick(float delta){
        this.elapsed += delta;
        return !(elapsed > duration);
    }

}
