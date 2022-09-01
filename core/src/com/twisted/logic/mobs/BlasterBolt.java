package com.twisted.logic.mobs;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.attach.Blaster;

public class BlasterBolt extends Mobile {

    //vertices
    public static final float[] vertices = new float[]
            {-0.005f,-0.01f,  -0.005f,0.01f,  0.005f,0.01f, 0.005f,-0.01f};

    //target
    private final Entity.Type targetType;
    private final int targetId;

    //metadata
    private final int owner;
    private final Blaster blaster;


    /**
     * Constructor.
     * @param pos Should be a copy if it is used elsewhere.
     * @param blaster Can be null on clientside.
     * @param targetType Can be null on clientside.
     */
    public BlasterBolt(int id, Vector2 pos, Blaster blaster, Entity.Type targetType, int targetId){
        this.id = id;
        this.pos = pos;
        this.vel = new Vector2(0, 0);
        this.rot = 0;

        this.blaster = blaster;
        this.targetType = targetType;
        this.targetId = targetId;

        if(blaster != null) this.owner = blaster.attached.owner;
        else this.owner = 0;
    }

    @Override
    public boolean update(float delta, Grid grid) {
        //get the entity, fizzle if it is gone
        Entity target = null;
        switch (targetType) {
            case Ship:
                target = grid.ships.get(targetId);
                break;
            case Station:
                target = grid.station;
                break;
        }
        if(target == null) return true;

        //check if it can collide, otherwise move it
        if(pos.dst(target.pos) < blaster.missileSpeed*delta){
            target.takeDamage(grid, owner, blaster.damage);
            return true;
        }
        else {
            vel.set(target.pos.x-pos.x, target.pos.y-pos.y).nor().scl(blaster.missileSpeed * delta);
            pos.add(vel);
            if(vel.len() != 0){
                rot = (float) Math.atan2(vel.y, vel.x);
            }
        }

        return false;
    }

    @Override
    public float[] getVertices() {
        return vertices;
    }
}
