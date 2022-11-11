package com.twisted.local.game.cosmetic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.twisted.util.Util;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.attach.Doomsday;

public class CosDoomCharge extends Cosmetic{

    //required parameters
    private final Doomsday source;
    private final Color color;

    //drawing
    private final Vector2 pos;


    /**
     * Constructor
     */
    public CosDoomCharge(int gridId, Doomsday source, Color color) {
        super(gridId);

        this.source = source;
        this.color = color;

        pos = new Vector2();
    }


    /* Action Methods */

    @Override
    public boolean tick(float delta) {
        return source.isActive();
    }
    @Override
    public void draw(ShapeRenderer shape, Grid g) {
        float percent = 1 - source.timer/source.model.chargeTime;
        pos.set(source.mountPoint).rotateDeg(Util.logicalRadToVisualDeg(source.attached.rot))
                .add(source.attached.pos);

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(color);
        shape.circle(LTR * pos.x, LTR * pos.y, LTR*percent*0.1f);
        shape.end();
    }
}
