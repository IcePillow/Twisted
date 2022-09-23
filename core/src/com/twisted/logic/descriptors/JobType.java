package com.twisted.logic.descriptors;

import com.sun.org.apache.xml.internal.serializer.EncodingInfo;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.entities.station.Station;

/**
 * The possible things (ships/stations) that can be constructed.
 */
public enum JobType {
    //frigates
    Sparrow(1, 1, 0, 0, 2),
    Alke(1, 1, 0, 0, 2),
    //cruisers
    Helios(2, 1, 0, 0, 2),
    //battleships
    Themis(3, 1, 0, 0, 2),
    //barges
    Heron(4, 1, 0, 0, 2),
    //stations
    Extractor(5, 1, 0, 0, 1),
    Harvester(5, 1, 0, 0, 2),
    Liquidator(5, 1, 0, 0, 2),
    //titans
    Nyx(10, 1, 0, 0, 2);

    public final int calcite;
    public final int kernite;
    public final int pyrene;
    public final int crystal;
    public final int duration; //in seconds

    JobType(int calcite, int kernite, int pyrene, int crystal, int duration) {
        this.calcite = calcite;
        this.kernite = kernite;
        this.pyrene = pyrene;
        this.crystal = crystal;

        this.duration = duration;
    }

    /**
     * Gets the gem cost for this job.
     */
    public int getGemCost(Gem gem){
        switch (gem){
            case Calcite:
                return this.calcite;
            case Kernite:
                return this.kernite;
            case Pyrene:
                return this.pyrene;
            case Crystal:
                return this.crystal;
            default:
                System.out.println("[Error] Unexpected gem type.");
                new Exception().printStackTrace();
                return 0;
        }
    }

    public Entity.Type getType(){
        switch(this){
            case Alke:
            case Sparrow:
            case Helios:
            case Themis:
            case Heron:
            case Nyx:
                return Entity.Type.Ship;
            case Extractor:
            case Harvester:
            case Liquidator:
                return Entity.Type.Station;
            default:
                System.out.println("Unexpected JobType");
                new Exception().printStackTrace();
                return null;
        }
    }
    public Entity.Tier getTier(){
        return this.getModel().getTier();
    }
    public Entity.Model getModel(){
        switch(this){
            case Alke:
                return Ship.Model.Alke;
            case Sparrow:
                return Ship.Model.Sparrow;
            case Helios:
                return Ship.Model.Helios;
            case Themis:
                return Ship.Model.Themis;
            case Heron:
                return Ship.Model.Heron;
            case Extractor:
                return Station.Model.Extractor;
            case Harvester:
                return Station.Model.Harvester;
            case Liquidator:
                return Station.Model.Liquidator;
            case Nyx:
                return Ship.Model.Nyx;
            default:
                System.out.println("Unexpected JobType");
                new Exception().printStackTrace();
                return null;
        }
    }
}
