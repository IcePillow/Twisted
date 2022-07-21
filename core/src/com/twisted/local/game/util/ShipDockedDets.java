package com.twisted.local.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.twisted.local.game.SecDetails;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;

public class ShipDockedDets extends DetailsSecGroup {

    //tree
    private Label shipName, shipGrid, healthLabel;
    private Image healthFill;

    //selection
    private Ship sel;


    /* Construction */

    public ShipDockedDets(SecDetails sector, Skin skin, GlyphLayout glyph, Vector2 size){
        super(sector, skin, glyph, size);

        Group topTextGroup = createTopTextGroup();
        topTextGroup.setPosition(6, 100);
        this.addActor(topTextGroup);

        Group healthGroup = createHealthGroup();
        healthGroup.setPosition(6, 87);
        this.addActor(healthGroup);
    }

    private Group createTopTextGroup(){
        Group group = new Group();

        shipGrid = new Label("[?]", skin, "medium", Color.WHITE);
        group.addActor(shipGrid);

        shipName = new Label("[Ship Name]", skin, "medium", Color.WHITE);
        shipName.setPosition(30, 0);
        group.addActor(shipName);

        Label dockedLabel = new Label("Docked", skin, "small", Color.LIGHT_GRAY);
        dockedLabel.setFontScale(0.9f);
        glyph.setText(skin.getFont("small"), dockedLabel.getText());
        dockedLabel.setPosition(288 - glyph.width*dockedLabel.getFontScaleX(), 0);
        group.addActor(dockedLabel);

        return group;
    }

    private Group createHealthGroup(){
        Group group = new Group();

        Image healthOutline = new Image(new Texture(Gdx.files.internal("images/pixels/gray.png")));
        healthOutline.setBounds(0, 0, 202, 10);
        group.addActor(healthOutline);

        Image healthInline = new Image(new Texture(Gdx.files.internal("images/pixels/darkgray.png")));
        healthInline.setBounds(1, 1, 200, 8);
        group.addActor(healthInline);

        healthFill = new Image(new Texture(Gdx.files.internal("images/pixels/green.png")));
        healthFill.setBounds(1, 1, 200, 8);
        group.addActor(healthFill);

        healthLabel = new Label("[health]", skin, "small",
                new Color(0,0.49f,0,1));
        healthLabel.setPosition(204, -7);
        group.addActor(healthLabel);

        return group;
    }


    /* Overrides */

    @Override
    public void selectEntity(Entity entity) {
        //copy entity
        if(!(entity instanceof Ship)){
            System.out.println("Unexpected state");
            new Exception().printStackTrace();
            return;
        }
        sel = (Ship) entity;

        //update the name
        shipName.setText(sel.getType().toString());
        shipName.setColor(state.players.get(sel.owner).getColor());

        //update the grid
        shipGrid.setText("[" + state.grids[sel.grid].nickname + "]");
    }

    @Override
    public void updateEntity() {
        //update the health
        healthLabel.setText(String.format("%" + (1+2*((int) Math.log10(sel.getMaxHealth())+1)) + "s",
                ((int) Math.ceil(sel.health)) + "/" + sel.getMaxHealth()));
        healthFill.setWidth(200 * sel.health / sel.getMaxHealth());
    }

    @Override
    public Entity getSelectedEntity() {
        return sel;
    }
}
