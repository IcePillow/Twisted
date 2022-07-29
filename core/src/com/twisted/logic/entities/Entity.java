package com.twisted.logic.entities;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.descriptors.Grid;

public abstract class Entity {

    /* Metadata */
    /**
     * Owner. Use 0 for none.
     */
    public int owner;


    /* Logic */

    /**
     * Position.
     */
    public Vector2 pos;
    /**
     * Velocity.
     */
    public Vector2 vel;
    /**
     * Rotation.
     */
    public float rot;
    /**
     * Current grid.
     * -1 for warp
     * Docked counts as being on grid
     */
    public int grid;


    /* Typing Methods */

    public Type getEntityType(){
        if(this instanceof Ship) return Type.Ship;
        else if(this instanceof Station) return Type.Station;
        else return null;
    }

    /**
     * These ids are not unique across entities, but they should be unique across each subclass
     * of entities.
     */
    public abstract int getId();

    public boolean isDocked(){
        switch(getEntityType()){
            case Ship:
                return ((Ship) this).docked;
            default:
                return false;
        }
    }

    public boolean matches(Entity ent){
        return (ent != null && ent.getId() == this.getId() && ent.getEntityType() == this.getEntityType());
    }


    /* Graphics Methods */

    /**
     * These vertices are used for drawing.
     * TODO see if these can be combined with the Polygon in the subclasses and used on serverside
     */
    public abstract float[] getVertices();
    /**
     * Returns the logical radius (i.e. not in visual coords) padded a little. Currently used
     * to display the selection circle on the viewport.
     */
    public abstract float getPaddedLogicalRadius();


    /* Naming Methods */

    public abstract String getFullName();
    /**
     * Name to be displayed in the fleet sector.
     */
    public abstract String getFleetName();


    /* Action Methods */

    public abstract void takeDamage(Grid grid, float amount);

    /* Enums */

    public enum Type {
        Station,
        Ship,
    }

    /**
     * Should be implemented by Type enums in subclasses of Entity.
     */
    public interface Subtype {

    }

}
