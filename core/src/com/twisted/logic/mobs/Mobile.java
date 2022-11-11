package com.twisted.logic.mobs;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.twisted.local.game.Game;
import com.twisted.logic.descriptors.Grid;
import com.twisted.util.Quirk;

public abstract class Mobile {

    //constants
    protected final float LTR = Game.LTR;

    //meta
    public int id;
    public int owner;
    public final Model model;

    //physics
    /**
     * Position
     */
    public Vector2 pos;
    /**
     * Velocity
     */
    public Vector2 vel;
    /**
     * Rotation
     */
    public float rot;

    //constructor
    public Mobile(Model model, int id, int owner){
        this.model = model;
        this.id = id;
        this.owner = owner;
    }

    //action
    /**
     * @return True to continue existing, false to fizzle.
     */
    public abstract boolean update(float delta, Grid grid);
    public abstract void draw(ShapeRenderer shape);

    //typing
    public Type getType(){
        if(this instanceof BlasterBolt) return Type.BlasterBolt;
        else if(this instanceof DoomsdayBlast) return Type.DoomsdayBlast;
        else {
            new Quirk(Quirk.Q.UnknownGameData).print();
            return null;
        }
    }

    //enums
    public enum Type {
        BlasterBolt,
        DoomsdayBlast
    }
    public interface Model{ }


}
