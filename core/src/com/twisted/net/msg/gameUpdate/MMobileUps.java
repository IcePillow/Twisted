package com.twisted.net.msg.gameUpdate;

import com.badlogic.gdx.math.Vector2;
import com.twisted.local.game.cosmetic.CosDoomBlast;
import com.twisted.local.game.cosmetic.Cosmetic;
import com.twisted.local.game.state.ClientGameState;
import com.twisted.logic.entities.attach.Doomsday;
import com.twisted.logic.mobs.DoomsdayBlast;
import com.twisted.logic.mobs.Mobile;
import com.twisted.net.msg.CosmeticCheckable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class can be interpreted as update, insert, or delete;
 */
public class MMobileUps implements MGameUpd, CosmeticCheckable {

    public boolean fizzle;

    public int gridId;
    public int mobileId;
    public Mobile.Type type;
    public Mobile.Model model;
    public int owner;

    public Vector2 pos;
    public Vector2 vel;
    public float rot;

    private MMobileUps(int gridId, int mobileId, Mobile.Type type, Mobile.Model model, int owner){
        this.gridId = gridId;
        this.mobileId = mobileId;
        this.type = type;
        this.model = model;
        this.owner = owner;
    }

    public static MMobileUps createFromMobile(Mobile m, int gridId, boolean fizzle){
        MMobileUps msg = new MMobileUps(gridId, m.id, m.getType(), m.model, m.owner);

        msg.pos = m.pos.cpy();
        msg.vel = m.vel.cpy();
        msg.rot = m.rot;
        msg.fizzle = fizzle;

        return msg;
    }

    @Override
    public List<Cosmetic> createNewCosmetics(ClientGameState state) {
        List<Cosmetic> list = null;

        if(type == Mobile.Type.DoomsdayBlast && fizzle){
            list = new ArrayList<>();
            Mobile m = state.grids[gridId].mobiles.get(mobileId);

            CosDoomBlast c = new CosDoomBlast(gridId, m.pos, ((DoomsdayBlast.Model) m.model).getSourceModel(),
                    state.findPaintCollectForOwner(m.owner).brightened.c);
            list.add(c);
        }

        return list;
    }
}
