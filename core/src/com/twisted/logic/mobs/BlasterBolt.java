package com.twisted.logic.mobs;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.attach.Blaster;

public class BlasterBolt extends Mobile {

    //vertices
    public static final float[] vertices = new float[]
            {-0.005f,-0.01f,  -0.005f,0.01f,  0.005f,0.01f, 0.005f,-0.01f};

    //target
    private final EntPtr target;

    //state
    private float timeFlying;

    //metadata
    private final int owner;
    private final Blaster blaster;


    /**
     * Constructor.
     * @param pos Should be a copy if it is used elsewhere.
     * @param blaster Can be null on clientside.
     * @param target Can be null on clientside.
     */
    public BlasterBolt(int id, Vector2 pos, Blaster blaster, EntPtr target){
        this.id = id;
        this.pos = pos;
        this.vel = new Vector2(0, 0);
        this.rot = 0;

        this.blaster = blaster;
        this.target = target;
        this.timeFlying = 0;

        if(blaster != null) this.owner = blaster.attached.owner;
        else this.owner = 0;
    }

    @Override
    public boolean update(float delta, Grid grid) {
        //get the entity, fizzle if it is gone
        Entity targetEnt = target.retrieveFromGrid(grid);
        if(targetEnt == null) return true;

        //check if it should fizzle due to distance travelled
        if(timeFlying > blaster.model.maxFlightTime) return true;
        timeFlying += delta;

        //check if it can collide, otherwise move it
        if(pos.dst(targetEnt.pos) < blaster.model.speed*delta){
            targetEnt.takeDamage(grid, owner, blaster.model.damage);
            return true;
        }
        else {
            vel.set(targetEnt.pos.x-pos.x, targetEnt.pos.y-pos.y).nor().scl(blaster.model.speed * delta);
            pos.add(vel);
            if(vel.len() != 0){
                rot = (float) Math.atan2(vel.y, vel.x);
                timeFlying += vel.len();
            }
        }

        return false;
    }

    @Override
    public float[] getVertices() {
        return vertices;
    }
}
