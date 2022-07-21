package com.twisted.local.game.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.twisted.local.game.SecDetails;
import com.twisted.logic.entities.Entity;

public class EmptyDets extends DetailsSecGroup{


    public EmptyDets(SecDetails sector, Skin skin, GlyphLayout glyph, Vector2 size) {
        super(sector, skin, glyph, size);

        Label label = new Label("[Selection]", skin, "medium", Color.WHITE);
        label.setPosition(8, 100);
        this.addActor(label);
    }


    /* Entity Handling */

    @Override
    public void selectEntity(Entity entity) {}

    @Override
    public void updateEntity() {}

    public Entity getSelectedEntity(){
        return null;
    }
}
