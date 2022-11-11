package com.twisted.local.game.cosmetic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.twisted.local.game.state.ClientGameState;
import com.twisted.util.Quirk;
import com.twisted.util.Util;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.attach.Beacon;

public class CosBeaconFlair extends Cosmetic {

    //required parameters
    private final Beacon source;
    private final Color color;

    //drawing
    private final float size;
    private final Vector2 pos;

    //state
    private float cycle;

    /**
     * Constructor
     */
    public CosBeaconFlair(int gridId, Beacon source, Color color) {
        super(gridId);

        this.source = source;
        this.color = color;

        //prepare drawing
        pos = new Vector2(source.mountPoint).rotateDeg(Util.logicalRadToVisualDeg(source.attached.rot))
                .add(source.attached.pos);
        switch(source.model){
            case Medium:
                size = 0.05f;
                break;
            default:
                size = 0;
                new Quirk(Quirk.Q.UnknownGameData).print();
        }
        cycle = 0;
    }


    /* Action Methods */

    @Override
    public boolean tick(float delta) {
        cycle += 2.5f * delta;

        return source.isActive();
    }
    @Override
    public void draw(ShapeRenderer shape, Grid g) {
        float radius = size + 0.15f * size * (float)Math.sin(cycle);

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(color);
        shape.circle(LTR*pos.x, LTR*pos.y, LTR*radius);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(color);
        shape.circle(LTR*pos.x, LTR*pos.y, LTR*radius*1.35f);
        shape.end();
    }


    /* State Methods */

    public boolean showsThroughFog(ClientGameState state){
        return true;
    }
}
