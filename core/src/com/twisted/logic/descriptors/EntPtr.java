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

    public EntPtr(Entity.Type type, int id, int grid){
        this.type = type;
        this.id = id;
        this.grid = grid;
    }


    public static EntPtr createFromEntity(Entity entity, int grid){
        return new EntPtr(entity.getEntityType(), entity.getId(), grid);
    }

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

    public boolean matches(Entity entity){
        return (entity.getId() == id && entity.getEntityType() == type);
    }

}
