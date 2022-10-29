package com.twisted.net.msg.summary;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.descriptors.Grid;

public class GridSum implements Summary{

    //data
    public Vector2 pos;
    public String nick;
    public float radius;
    public float[] resourceGen;

    /**
     * Constructor
     */
    protected GridSum(Vector2 pos, String nick, float radius, float[] resourceGen){
        this.pos = pos;
        this.nick = nick;
        this.radius = radius;
        this.resourceGen = resourceGen;
    }


    public static GridSum createFromGrid(Grid g){
        return new GridSum(g.pos, g.nickname, g.radius, g.resourceGen);
    }

    public Grid createGridFromThis(int id){
        return new Grid(id, pos, nick, radius, resourceGen);
    }

}
