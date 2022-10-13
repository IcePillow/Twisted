package com.twisted.local.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.twisted.Asset;
import com.twisted.Main;
import com.twisted.local.game.SecDetails;
import com.twisted.local.lib.TogImgButton;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;

public class DetsShipInWarp extends DetsGroup {

    //tree

    private Label shipName, shipMoveCommand;
    private Label shipPosition, shipVelocity, healthLabel;
    private Image healthFill, shipIcon;

    //selection
    private Ship sel;


    /* Construction */

    public DetsShipInWarp(SecDetails sector, Skin skin, Vector2 size){
        super(sector, skin, size);

        Group topTextGroup = createTopTextGroup();
        topTextGroup.setPosition(6, 100);
        this.addActor(topTextGroup);

        Group locationGroup = createLocationGroup();
        locationGroup.setPosition(6, 42);
        this.addActor(locationGroup);

        Group healthGroup = createHealthGroup();
        healthGroup.setPosition(6, 89);
        this.addActor(healthGroup);
    }

    private Group createTopTextGroup(){
        Group group = new Group();

        shipIcon = new Image(Asset.retrieveEntityIcon(Ship.Tier.Frigate));
        shipIcon.setPosition(0, 2);
        shipIcon.setColor(Color.GRAY);
        group.addActor(shipIcon);

        Label shipGrid = new Label("[W]", Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        shipGrid.setColor(Color.LIGHT_GRAY);
        shipGrid.setPosition(18, 0);
        group.addActor(shipGrid);

        shipName = new Label("[Ship Name]", Asset.labelStyle(Asset.Avenir.HEAVY_16));
        shipName.setPosition(40, 0);
        group.addActor(shipName);

        shipMoveCommand = new Label("[movement cmd]", Asset.labelStyle(Asset.Avenir.MEDIUM_14));
        shipMoveCommand.setColor(Color.LIGHT_GRAY);
        shipMoveCommand.setFontScale(0.9f);
        Main.glyph.setText(skin.getFont("small"), shipMoveCommand.getText());
        shipMoveCommand.setPosition(288 - Main.glyph.width*shipMoveCommand.getFontScaleX(), 0);
        group.addActor(shipMoveCommand);

        return group;
    }

    private Group createLocationGroup(){
        Group group = new Group();

        shipPosition = new Label("[x]\n[y]", Asset.labelStyle(Asset.Avenir.MEDIUM_14));
        shipPosition.setColor(Color.LIGHT_GRAY);
        group.addActor(shipPosition);

        shipVelocity = new Label("[sp]\n[ag]", Asset.labelStyle(Asset.Avenir.MEDIUM_14));
        shipVelocity.setColor(Color.LIGHT_GRAY);
        shipVelocity.setPosition(84, 0);
        group.addActor(shipVelocity);

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
        shipName.setText(sel.entityModel().toString());
        shipName.setColor(state.players.get(sel.owner).getPaint().col);
        shipIcon.setDrawable(Asset.retrieveEntityIcon(sel.model.tier));
    }

    @Override
    public void updateEntity() {
        //update movement and calculate new layout data
        shipMoveCommand.setText(sel.moveDescription);
        Main.glyph.setText(shipMoveCommand.getStyle().font, shipMoveCommand.getText());
        shipMoveCommand.setX(290 - Main.glyph.width*shipMoveCommand.getFontScaleX());

        //update the position
        float[] rounded = sel.roundedWarpPos(0);
        shipPosition.setText("X = " + (int)rounded[0] + "\nY = " + (int)rounded[1]);
        rounded = sel.roundedWarpBear(0);
        shipVelocity.setText("Sp = " + (int)rounded[0] + "\nAg = " + (int)rounded[1]);

        //update the health
        healthLabel.setText(String.format("%" + (1+2*((int) Math.log10(sel.model.maxHealth)+1)) + "s",
                ((int) Math.ceil(sel.health)) + "/" + sel.model.maxHealth));
        healthFill.setWidth(200 * sel.health / sel.model.maxHealth);
    }

    @Override
    public Entity getSelectedEntity() {
        return sel;
    }
}
