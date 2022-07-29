package com.twisted.net.msg.gameUpdate;

import com.badlogic.gdx.math.Vector2;
import com.twisted.logic.mobs.Mobile;

/**
 * This class can be interpreted as update, insert, or delete;
 */
public class MMobileUps implements MGameUpd {

    public boolean fizzle;

    public int gridId;
    public int mobileId;
    public Mobile.Type type;

    public Vector2 pos;
    public Vector2 vel;
    public float rot;

    private MMobileUps(int gridId, int mobileId, Mobile.Type type){
        this.gridId = gridId;
        this.mobileId = mobileId;
        this.type = type;
    }

    public static MMobileUps createFromMobile(Mobile m, int gridId, boolean fizzle){
        MMobileUps msg = new MMobileUps(gridId, m.id, m.getType());
        msg.pos = m.pos.cpy();
        msg.vel = m.vel.cpy();
        msg.rot = m.rot;
        msg.fizzle = fizzle;

        return msg;
    }

}
