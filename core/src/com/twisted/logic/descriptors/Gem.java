package com.twisted.logic.descriptors;

public enum Gem {

    Calcite(0, 0.1f),
    Kernite(1, 1f),
    Pyrene(2, 2f),
    Crystal(3, 5f);

    //constants
    public final static Gem[] orderedGems = new Gem[]{Calcite, Kernite, Pyrene, Crystal};
    public final static int NUM_OF_GEMS = 4;

    //details
    public final int index;
    public final float volume;


    Gem(int index, float volume){
        this.index = index;
        this.volume = volume;
    }

    /**
     * @param resources Array of length 4.
     * @return The amount of space the gems take up.
     */
    public static float calcVolume(int[] resources){
        float sum = 0;
        for(int i=0; i< resources.length; i++){
            sum += orderedGems[i].volume*resources[i];
        }
        return sum;
    }

}
