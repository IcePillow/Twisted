package com.twisted.local.game.cosmetic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.attach.Doomsday;

public class CosDoomBlast extends Cosmetic{

    //constants
    private final static float INNER_DUR = 0.45f, OUTER_DUR = 0.8f;

    //drawing
    private final Color innerColor, outerColor;
    private float innerPerc, outerPerc;

    //state
    private final Doomsday.Model model;
    private final Vector2 pos;
    private float time;

    /**
     * Constructor
     */
    public CosDoomBlast(int gridId, Vector2 pos, Doomsday.Model model, Color color) {
        super(gridId);

        this.pos = pos;
        this.model = model;
        this.innerColor = color;
        this.outerColor = this.innerColor.cpy();
        this.outerColor.add(0.15f, 0.15f, 0.15f, 0);

        this.innerPerc = 0;
        this.outerPerc = 0;

        this.time = 0;
    }


    /* Action Methods */

    @Override
    public boolean tick(float delta) {
        time += delta;

        if(time < INNER_DUR){
            innerPerc = (float) Math.pow(time/INNER_DUR, 0.5f);
        }
        else if(time < OUTER_DUR) {
            innerPerc = 1;
            outerPerc = (float) Math.pow(time/OUTER_DUR, 0.5f);
        }
        else {
            return false;
        }

        return true;
    }
    @Override
    public void draw(ShapeRenderer shape, Grid g) {
        shape.begin(ShapeRenderer.ShapeType.Filled);

        shape.setColor(outerColor);
        shape.circle(LTR*pos.x, LTR*pos.y, LTR * model.outerBlastRadius * outerPerc);
        shape.setColor(innerColor);
        shape.circle(LTR*pos.x, LTR*pos.y, LTR * model.innerBlastRadius * innerPerc);

        shape.end();
    }
}
