package com.twisted.local.game.cosmetic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.attach.Laser;
import com.twisted.logic.entities.ship.Ship;

public class LaserBeam extends Cosmetic {

    //constants
    private final static Color LASER_YELLOW = new Color(0.6f, 0.6f, 0, 1);

    //required parameters
    private final Laser source;

    //optional parameters
    public Color color = LASER_YELLOW;

    /**
     * This cosmetic persists until the source is destroyed.
     */
    public LaserBeam(int gridId, Laser source){
        super(gridId);

        this.source = source;
        source.cosmeticBeamExists = true;
    }

    @Override
    public void draw(ShapeRenderer shape, Grid g) {
        //get the target entity
        Entity target = null;
        if(source.attached.targetEntity != null) target = source.attached.targetEntity.retrieveFromGrid(g);

        //check if all targeting is valid
        if(target != null && source.attached.targetingState == Ship.Targeting.Locked &&
                target.pos.dst(source.attached.pos) <= source.model.range){
            shape.setColor(color);

            Vector2 srcPt = source.sourcePoint.cpy().rotateDeg((float) (source.attached.rot*180/Math.PI)-90);
            shape.line(LTR*(source.attached.pos.x + srcPt.x),
                    LTR*(source.attached.pos.y + srcPt.y),
                    LTR*target.pos.x,
                    LTR*target.pos.y);
        }
    }
    @Override
    public boolean tick(float delta) {
        if(source.active) return true;
        source.cosmeticBeamExists = false;
        return false;
    }

}
