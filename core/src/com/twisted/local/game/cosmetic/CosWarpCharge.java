package com.twisted.local.game.cosmetic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.twisted.util.Quirk;
import com.twisted.util.Util;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.ship.Ship;

public class CosWarpCharge extends Cosmetic {

    //required parameters
    private final Ship source;
    private final Color color;

    //drawing
    private final Vector2 pos, scratch1, scratch2;
    private int frame;
    private final float fullScale;
    private float scale;
    private final float[] points;

    /**
     * Constructor
     */
    public CosWarpCharge(int gridId, Ship source, Color color) {
        super(gridId);

        this.source = source;
        this.color = color;

        this.pos = new Vector2();
        this.scratch1 = new Vector2();
        this.scratch2 = new Vector2();
        this.frame = 0;

        switch(source.model.tier){
            case Frigate:
                fullScale = 0.06f;
                points = new float[3*4];
                break;
            case Cruiser:
                fullScale = 0.08f;
                points = new float[4*4];
                break;
            case Battleship:
            case Barge:
                fullScale = 0.11f;
                points = new float[5*4];
                break;
            case Titan:
                fullScale = 0.16f;
                points = new float[7*4];
                break;
            default:
                fullScale = 0;
                points = new float[0];
                new Quirk(Quirk.Q.UnknownGameData).print();
        }
        this.scale = fullScale * 0.3f;
        generatePoints();
    }

    @Override
    public boolean tick(float delta) {
        frame += 1;

        if(source.warpCharge > 0.7f){
            if(frame%6 == 0){
                scale = fullScale;
                generatePoints();
            }
        }
        else if(source.warpCharge > 0.4f){
            if(frame % 10 == 0){
                scale = 0.6f * fullScale;
                generatePoints();
            }
        }
        else if(frame % 14 == 0){
            scale = 0.3f * fullScale;
            generatePoints();
        }

        return (source.warpCharge !=0 && source.grid != -1);
    }
    @Override
    public void draw(ShapeRenderer shape, Grid g) {
        pos.set(source.model.warpSource).rotateDeg(Util.logicalRadToVisualDeg(source.rot)).add(source.pos).scl(LTR);

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(color);

        for(int i=0; i<points.length; i+=4){
            scratch1.set(pos.x + LTR*points[i], pos.y + LTR*points[i+1])
                    .rotateAroundRad(pos, source.rot-(float)Math.PI/2);
            scratch2.set(pos.x + LTR*points[i+2], pos.y + LTR*points[i+3])
                    .rotateAroundRad(pos, source.rot-(float)Math.PI/2);
            shape.line(scratch1, scratch2);
        }

        shape.end();
    }


    /* Utility */

    private void generatePoints(){
        for(int i=0; i<points.length; i+=4){
            //start point
            points[i] = ((float)Math.random() - 0.5f) * scale/3;
            points[i+1] = ((float)Math.random() - 0.5f) * scale/4;
            //end point
            points[i+2] = points[i] + ((float)Math.random() - 0.5f) * scale;
            points[i+3] = -scale + ((float)Math.random() - 0.5f) * scale*2/3;
        }
    }

}
