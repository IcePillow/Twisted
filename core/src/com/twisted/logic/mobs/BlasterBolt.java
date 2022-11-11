package com.twisted.logic.mobs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.twisted.util.Paint;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.attach.Blaster;
import com.twisted.util.Quirk;

public class BlasterBolt extends Mobile {

    //vertices
    public static final float[] vertices = new float[]
            {-0.005f,-0.01f,  -0.005f,0.01f,  0.005f,0.01f, 0.005f,-0.01f};

    //metadata
    public final Blaster blaster;

    //clientside drawing
    private Color color;
    public void setColor(Color color){
        this.color = color;
    }

    //target
    private final EntPtr target;

    //state
    private float timeFlying;


    /**
     * Constructor.
     * @param pos Should be a copy if it is used elsewhere.
     * @param blaster Can be null on clientside.
     * @param target Can be null on clientside.
     */
    public BlasterBolt(Model model, int id, int owner, Vector2 pos, Blaster blaster, EntPtr target){
        super(model, id, owner);

        this.pos = pos;
        this.vel = new Vector2(0, 0);
        this.rot = 0;
        this.color = Paint.PL_GRAY.c;

        this.blaster = blaster;
        this.target = target;
        this.timeFlying = 0;
    }


    /* Action Methods */

    @Override
    public boolean update(float delta, Grid grid) {
        //get the entity, fizzle if it is gone
        Entity targetEnt = target.retrieveFromGrid(grid);
        if(targetEnt == null) return false;

        //check if it should fizzle due to distance travelled
        if(timeFlying > blaster.model.maxFlightTime) return false;
        timeFlying += delta;

        //check if it can collide, otherwise move it
        if(pos.dst(targetEnt.pos) < blaster.model.speed*delta){
            targetEnt.takeDamage(grid, owner, blaster.model.damage);
            return false;
        }
        else {
            vel.set(targetEnt.pos.x-pos.x, targetEnt.pos.y-pos.y).nor().scl(blaster.model.speed * delta);
            pos.add(vel);
            if(vel.len() != 0){
                rot = (float) Math.atan2(vel.y, vel.x);
            }
        }

        return true;
    }
    @Override
    public void draw(ShapeRenderer shape){
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(color);

        Polygon drawable = new Polygon(vertices);
        drawable.scale(LTR * ((Model)model).drawScale);
        drawable.translate(pos.x*LTR, pos.y*LTR);
        drawable.rotate((float) (rot*180/Math.PI)-90);
        shape.polygon(drawable.getTransformedVertices());
        shape.end();
    }


    /* Utility */

    public static Model chooseModel(Blaster blaster){
        switch(blaster.model){
            case Small:
                return Model.Small;
            case Medium:
                return Model.Medium;
            case Large:
                return Model.Large;
            default:
                new Quirk(Quirk.Q.UnknownGameData).print();
                return null;
        }
    }


    /* Enums */

    public enum Model implements Mobile.Model {
        Small(1),
        Medium(1.3f),
        Large(1.8f);

        public final float drawScale;

        Model(float drawScale){
            this.drawScale = drawScale;
        }
    }
}
