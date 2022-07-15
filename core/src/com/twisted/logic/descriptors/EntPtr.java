package com.twisted.logic.descriptors;

import com.twisted.logic.entities.Entity;

/**
 * Describes an entity without referencing it directly.
 *
 * TODO refactor to use this elsewhere
 */
public class EntPtr {

    public int grid;
    public final int id;
    public final Entity.Type type;

    /**
     * Base constructor.
     */
    public EntPtr(Entity.Type type, int id, int grid){
        this.type = type;
        this.id = id;
        this.grid = grid;
    }

    /**
     * Uses the base constructor with the entity data provided.
     */
    public static EntPtr createFromEntity(Entity entity, int grid){
        return new EntPtr(entity.getEntityType(), entity.getId(), grid);
    }

    /**
     * Returns a copy of this entity pointer.
     */
    public EntPtr cpy(){
        return new EntPtr(type, id, grid);
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
        else if(type == Entity.Type.Ship){
            return grid.ships.get(id);
        }
        else {
            return null;
        }
    }

    /**
     * Checks if the entity id and type passed in match the ones stored here.
     * Will not match null, but no error will be thrown.
     */
    public boolean matches(Entity entity){
        return (entity != null && entity.getId() == id && entity.getEntityType() == type);
    }

}
