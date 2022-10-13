package com.twisted.logic.entities;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.attach.Weapon;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.entities.station.Station;

import java.io.Serializable;

public abstract class Entity implements Serializable {

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

    public Type entityType(){
        if(this instanceof Ship) return Type.Ship;
        else if(this instanceof Station) return Type.Station;
        else return null;
    }

    public abstract Model entityModel();

    /**
     * These ids are not unique across entities, but they are unique across each subclass
     * of entities.
     */
    public abstract int getId();

    public boolean matches(Entity ent){
        return (ent != null && ent.getId() == this.getId() && ent.entityType() == this.entityType());
    }

    public boolean matches(EntPtr ptr){
        return (ptr != null && ptr.id == getId() && ptr.type == entityType());
    }


    /* State Methods */

    public boolean isDocked(){
        if(entityType() == Type.Ship){
            return ((Ship) this).docked;
        }
        else {
            return false;
        }
    }

    /**
     * Checks if this entity is valid to be warped to as a beacon right now.
     */
    public boolean isValidBeacon(){
        if(this.entityType() == Type.Station){
            return true;
        }
        else {
            for(Weapon w : ((Ship) this).weapons){
                if(w.getType() == Weapon.Type.Beacon && w.isActive()) return true;
            }
            return false;
        }
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
        Ship;
    }

    public interface Tier {
        String getFilename();
    }
    public interface Model {
        String getFilename();
        Tier getTier();

        float[] getVertices();
        /**
         * Returns the logical radius (i.e. not in visual coords) padded a little. Currently, used
         * to display the selection circle on the viewport.
         */
        float getPaddedLogicalRadius();
    }


}
