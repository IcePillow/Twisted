package com.twisted.logic.descriptors;

import com.twisted.logic.entities.Entity;

import java.io.Serializable;

/**
 * Describes an entity without referencing it directly.
 *
 * TODO refactor to use this elsewhere
 */
public class EntPtr implements Serializable {

    public int grid;
    public boolean docked;
    public final int id;
    public final Entity.Type type;


    /* Constructing */

    /**
     * Base constructor.
     */
    public EntPtr(Entity.Type type, int id, int grid, boolean docked){
        this.type = type;
        this.id = id;
        this.grid = grid;
        this.docked = docked;
    }

    /**
     * Uses the base constructor with the entity data provided.
     */
    public static EntPtr createFromEntity(Entity entity){
        if(entity == null) return null;
        return new EntPtr(entity.getEntityType(), entity.getId(), entity.grid, entity.isDocked());
    }

    /**
     * Returns a copy of this pointer.
     */
    public EntPtr cpy(){
        return new EntPtr(type, id, grid, docked);
    }


    /* Accessing */

    /**
     * Gets the entity from the grid. Returns null if not found.
     */
    public Entity retrieveFromGrid(Grid grid){
        if(grid.id != this.grid){
            return null;
        }
        else if(type == Entity.Type.Station){
            return grid.station;
        }
        else if(type == Entity.Type.Ship && !docked){
            return grid.ships.get(id);
        }
        else if(type == Entity.Type.Ship) {
            return grid.station.dockedShips.get(id);
        }
        else {
            return null;
        }
    }

    /**
     * Checks if the entity id and type passed in match the ones stored here.
     * Will not match null, but no error will be thrown.
     */
    public boolean matches(Entity ent){
        return (ent != null && ent.getId() == id && ent.getEntityType() == type);
    }

    /**
     * Checks if two pointers have the same values.
     */
    public boolean matches(EntPtr ptr){
        return ptr.grid==grid && ptr.id==id && ptr.type==type;
    }

}
