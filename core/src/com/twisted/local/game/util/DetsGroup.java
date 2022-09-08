package com.twisted.local.game.util;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.twisted.local.game.SecDetails;
import com.twisted.local.game.state.ClientGameState;
import com.twisted.logic.entities.Entity;

public abstract class DetsGroup extends Group {

    /* Fields */

    protected final SecDetails sector;
    protected final Skin skin;
    protected final Vector2 size;

    protected ClientGameState state;


    /* Construction */

    protected DetsGroup(SecDetails sector, Skin skin, Vector2 size){
        this.sector = sector;
        this.skin = skin;
        this.size = size;
    }

    public void setState(ClientGameState state){
        this.state = state;
    }


    /* Methods */

    public abstract void selectEntity(Entity entity);
    public abstract void updateEntity();

    public abstract Entity getSelectedEntity();
}
