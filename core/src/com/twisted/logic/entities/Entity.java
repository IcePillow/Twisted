package com.twisted.logic.entities;

import com.badlogic.gdx.math.Vector2;
import com.twisted.Asset;
import com.twisted.logic.descriptors.EntPtr;
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


    /* Serverside Logic */

    /**
     * Last player to damage this entity.
     */
    public int lastHit;


    /* Typing Methods */

    /**
     * This is distinct from the getType() that is defined in each child in that it specifies the
     * child of Entity, not the grandchild.
     */
    public Type getEntityType(){
        if(this instanceof Ship) return Type.Ship;
        else if(this instanceof Station) return Type.Station;
        else return null;
    }

    public abstract Subtype getSubtype();

    /**
     * These ids are not unique across entities, but they are unique across each subclass
     * of entities.
     */
    public abstract int getId();

    public boolean matches(Entity ent){
        return (ent != null && ent.getId() == this.getId() && ent.getEntityType() == this.getEntityType());
    }

    public boolean matches(EntPtr ptr){
        return (ptr != null && ptr.id == getId() && ptr.type == getEntityType());
    }


    /* State Methods */

    public boolean isDocked(){
        if(getEntityType() == Type.Ship){
            return ((Ship) this).docked;
        }
        else {
            return false;
        }
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
    /**
     * The icon that represents this entity.
     */
    public Asset.EntityIcon getIconEnum(){
        return this.getSubtype().getIcon();
    }


    /* Naming Methods */

    /**
     * The normal name for this entity.
     */
    public abstract String getFullName();
    /**
     * Name to be displayed in the fleet sector.
     */
    public abstract String getFleetName();


    /* Action Methods */

    public void takeDamage(Grid grid, int owner, float amount){
        this.lastHit = owner;
    }


    /* Enums */

    public enum Type {
        Station,
        Ship,
    }

    /**
     * Should be implemented by Type enums in subclasses of Entity.
     */
    public interface Subtype {
        Asset.EntityIcon getIcon();
    }

}
