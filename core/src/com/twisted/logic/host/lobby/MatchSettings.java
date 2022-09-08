package com.twisted.logic.host.lobby;

import java.io.Serializable;
import java.util.Arrays;

public class MatchSettings implements Serializable {

    public Map map;


    /* Construction */

    public MatchSettings(){

    }

    public static MatchSettings createWithDefaults(){
        MatchSettings obj = new MatchSettings();
        obj.map = Map.Classic;

        return obj;
    }


    /* Enums */

    /**
     * The type of setting.
     */
    public enum Type {
        MAP,
    }

    /**
     * Interface for each of the settings
     */
    public interface Setting {
        Setting next();
        Setting prev();
    }

    public enum Map implements Setting {
        Classic,
        Placeholder;

        @Override
        public Map next(){
            Map[] arr = Map.values();
            int idx = Arrays.binarySearch(arr, this);
            return arr[(idx + 1) % arr.length];
        }
        @Override
        public Map prev(){
            Map[] arr = Map.values();
            int idx = Arrays.binarySearch(arr, this);
            return arr[(idx - 1 + arr.length) % arr.length];
        }
    }
}
