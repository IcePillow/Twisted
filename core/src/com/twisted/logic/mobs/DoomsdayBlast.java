package com.twisted.logic.mobs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.attach.Doomsday;
import com.twisted.logic.entities.attach.Weapon;
import com.twisted.util.Quirk;

public class DoomsdayBlast extends Mobile{

    //clientside drawing
    private Color color;
    public void setColor(Color color){
        this.color = color;
    }

    //metadata
    public final Doomsday source;

    //target location
    public final Vector2 target;


    /**
     * Constructor
     */
    public DoomsdayBlast(Model model, int id, int owner, Vector2 pos, Doomsday source, Vector2 target) {
        super(model, id, owner);

        this.pos = pos;
        this.vel = new Vector2();
        this.rot = 0;

        this.source = source;
        this.target = target;
    }

    @Override
    public boolean update(float delta, Grid grid) {

        //detonate
        if(pos.dst(target) < delta * source.model.flightSpd){
            //set position
            pos.set(target);

            //deal damage
            for(Entity ent : grid.entitiesInSpace()){
                float dist = ent.pos.dst(target);
                if(!(ent.matches(source.attached) || dist > source.model.outerBlastRadius)){
                    //inside inner radius
                    if(dist < source.model.innerBlastRadius){
                        ent.takeDamage(grid, source.attached.owner, source.model.maxDmg);
                    }
                    //inside outer radius
                    else {
                        ent.takeDamage(grid, source.attached.owner, outerDamageCalc(dist, source.model));
                    }
                }
            }

            return false;
        }
        //normal movement
        else {
            vel.set(target.x-pos.x, target.y-pos.y).nor().scl(source.model.flightSpd);
            pos.add(vel.x*delta, vel.y*delta);
        }

        return true;
    }
    @Override
    public void draw(ShapeRenderer shape) {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(color);
        shape.circle(pos.x*LTR, pos.y*LTR, ((Model) model).drawSize*LTR);
        shape.end();
    }


    /* Utility Methods */

    private float outerDamageCalc(float distance, Doomsday.Model m){
        float p = (distance-m.innerBlastRadius) / (m.outerBlastRadius-m.innerBlastRadius);
        p = 1 - p;

        float dmgRatio = 4f/3f * ((1f/(float)Math.pow(p+1, 2)) - 1f/4f);

        return dmgRatio * (m.maxDmg-m.minDmg) + m.minDmg;
    }

    public static Model chooseModel(Doomsday doomsday){
        switch(doomsday.model){
            case Capital:
                return Model.Capital;
            default:
                new Quirk(Quirk.Q.UnknownGameData).print();
                return null;
        }
    }


    /* Enums */

    public enum Model implements Mobile.Model {
        Capital(0.1f);

        //data
        public final float drawSize;

        //constructor
        Model(float drawSize){
            this.drawSize = drawSize;
        }

        //utility
        public Doomsday.Model getSourceModel(){
            if(this == Capital) return Doomsday.Model.Capital;
            else {
                new Quirk(Quirk.Q.UnknownGameData).print();
                return null;
            }
        }
    }
}
