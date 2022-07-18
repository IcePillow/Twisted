package com.twisted.logic.descriptors;

import com.twisted.logic.entities.Entity;

/**
 * Describes an entity without referencing it directly.
 *
 * TODO refactor to use this elsewhere
 */
public class EntPtr {

    public int grid;
    public int docked;
    public final int id;
    public final Entity.Type type;

    /**
     * Base constructor.
     */
    public EntPtr(Entity.Type type, int id, int grid, int docked){
        this.type = type;
        this.id = id;
        this.grid = grid;
        this.docked = docked;
    }

    /**
     * Uses the base constructor with the entity data provided.
     */
    public static EntPtr createFromEntity(Entity entity, int grid, int docked){
        return new EntPtr(entity.getEntityType(), entity.getId(), grid, docked);
    }

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
        else if(type == Entity.Type.Ship && docked == -1){
            return grid.ships.get(id);
        }
        else if(type == Entity.Type.Ship) {
            return grid.station.dockedShips.get(id);
        }
        else {
            return null;
        }
    }

    public EntPtr cpy(){
        return new EntPtr(type, id, grid, docked);
    }

    /**
     * Checks if the entity id and type passed in match the ones stored here.
     * Will not match null, but no error will be thrown.
     */
    public boolean matches(Entity entity){
        return (entity != null && entity.getId() == id && entity.getEntityType() == type);
    }

    /**
     * Checks if two pointers have the same values.
     */
    public boolean matches(EntPtr ptr){
        return ptr.grid==grid && ptr.id==id && ptr.type==type;
    }

}
