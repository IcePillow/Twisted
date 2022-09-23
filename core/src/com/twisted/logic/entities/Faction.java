package com.twisted.logic.entities;

public enum Faction {
    Federation(),
    Republic();

    public String getFilename(){
        return this.name().toLowerCase();
    }

    Faction(){ }
}
