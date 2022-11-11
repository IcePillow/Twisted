package com.twisted.local.game.cosmetic;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.twisted.util.Paint;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.attach.Laser;

public class CosLaserBeam extends Cosmetic {

    //required parameters
    private final Laser source;
    private final Paint.Collect collect;

    //drawing
    private final Vector2 endPt;

    //state
    private final Vector2 distVec;
    private float beamStr, beamEnd;
    private float pulseStr, pulseEnd, pulseLen;
    private boolean ending;

    /**
     * This cosmetic persists until the source is disabled.
     */
    public CosLaserBeam(int gridId, Laser source, Paint.Collect collect){
        super(gridId);

        this.source = source;
        this.distVec = new Vector2();
        this.collect = collect;

        this.endPt = new Vector2();

        this.beamStr = 0;
        this.beamEnd = 0;
        this.pulseStr = 0;
        this.pulseEnd = 0;
        this.pulseLen = generateBeamLength();

        this.ending = false;
    }

    @Override
    public boolean tick(float delta) {
        //extend the beam
        if(beamEnd < 1){
            beamEnd += 2f*delta;
            if(beamEnd > 1) beamEnd = 1;
        }
        //move the pulses along
        else if(beamEnd == 1){
            if(pulseEnd > pulseLen) pulseStr += delta;
            pulseEnd += delta;
        }

        //reset the points
        if(pulseStr > 1){
            if(!ending){
                pulseStr = 0;
                pulseEnd = 0;
                pulseLen = generateBeamLength();
            }
            else{
                pulseStr = 1;
                pulseEnd = 1;
            }
        }
        else if(pulseEnd > 1){
            pulseEnd = 1;
        }

        //handle ending
        if(!ending && (!source.isActive() || source.getTarget() == null)){
            ending = true;
        }
        else if(ending){
            beamStr += 2f*delta;

            if(pulseStr < beamStr) pulseStr = beamStr;
            if(pulseEnd < beamStr) pulseEnd = beamStr;
        }

        return beamStr < 1;
    }
    @Override
    public void draw(ShapeRenderer shape, Grid g) {
        //get the target entity
        Entity entity;
        if(!ending && source.getTarget() != null &&
                (entity = source.getTarget().retrieveFromGrid(g)) != null){
            endPt.set(entity.pos);
        }

        //check if all targeting is valid
        Vector2 srcPt = source.mountPoint.cpy().rotateDeg((float) (source.attached.rot*180/Math.PI)-90)
                .add(source.attached.pos);
        distVec.set(endPt.x-srcPt.x, endPt.y-srcPt.y);

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(collect.brightened.c);
        shape.line(LTR * (srcPt.x + beamStr*distVec.x), LTR * (srcPt.y + beamStr*distVec.y),
                LTR * (srcPt.x + beamEnd*distVec.x), LTR * (srcPt.y + beamEnd*distVec.y));

        shape.setColor(collect.base.c);
        shape.line(LTR * (srcPt.x + pulseStr*distVec.x),LTR * (srcPt.y + pulseStr*distVec.y),
                LTR * (srcPt.x + pulseEnd*distVec.x),LTR * (srcPt.y + pulseEnd*distVec.y));

        shape.end();
    }


    /* Utility */

    private float generateBeamLength(){
        return 0.15f + 0.1f * ((float)Math.random() - 0.5f);
    }

}
