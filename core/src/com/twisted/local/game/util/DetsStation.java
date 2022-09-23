package com.twisted.local.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.twisted.Asset;
import com.twisted.Main;
import com.twisted.Paint;
import com.twisted.local.game.SecDetails;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.station.Station;

public class DetsStation extends DetsGroup {

    //tree
    private Label stationGrid, stationName, stageTimer;
    private Image stageImage, stationIcon;
    private Image healthFill;
    private Label healthLabel;

    //selection
    private Station sel;


    /* Construction */

    public DetsStation(SecDetails sector, Skin skin, Vector2 size) {
        super(sector, skin, size);

        Group topTextGroup = createTopTextGroup();
        topTextGroup.setPosition(6, 100);
        this.addActor(topTextGroup);

        Group healthGroup = createHealthGroup();
        healthGroup.setPosition(6, 89);
        this.addActor(healthGroup);
    }

    private Group createTopTextGroup(){
        Group group = new Group();

        stationIcon = new Image(Asset.retrieveEntityIcon(Station.Tier.Station));
        stationIcon.setColor(Color.GRAY);
        stationIcon.setPosition(0, 2);
        group.addActor(stationIcon);

        stationGrid = new Label("[?]", Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        stationGrid.setColor(Color.LIGHT_GRAY);
        stationGrid.setX(18);
        group.addActor(stationGrid);

        stationName = new Label("[Station Name]", Asset.labelStyle(Asset.Avenir.HEAVY_16));
        stationName.setX(48);
        group.addActor(stationName);

        stageTimer = new Label("", Asset.labelStyle(Asset.Avenir.MEDIUM_14));
        stageTimer.setColor(Color.LIGHT_GRAY);
        stageTimer.setPosition(260, 8);
        group.addActor(stageTimer);

        stageImage = new Image(Asset.retrieve(Asset.UiIcon.STATION_SHIELDED));
        stageImage.setColor(Color.LIGHT_GRAY);
        stageImage.setX(270);
        group.addActor(stageImage);

        return group;
    }

    private Group createHealthGroup(){
        Group group = new Group();

        Image healthOutline = new Image(Asset.retrieve(Asset.Pixel.GRAY));
        healthOutline.setBounds(0, 0, 202, 10);
        group.addActor(healthOutline);

        Image healthInline = new Image(Asset.retrieve(Asset.Pixel.DARKGRAY));
        healthInline.setBounds(1, 1, 200, 8);
        group.addActor(healthInline);

        healthFill = new Image(Asset.retrieve(Asset.Pixel.WHITE));
        healthFill.setColor(Paint.HEALTH_GREEN.col);
        healthFill.setBounds(1, 1, 200, 8);
        group.addActor(healthFill);

        healthLabel = new Label("[health]", Asset.labelStyle(Asset.Avenir.MEDIUM_14));
        healthLabel.setPosition(204, -3);
        healthLabel.setColor(Paint.HEALTH_GREEN.col);
        group.addActor(healthLabel);

        return group;
    }


    /* Overrides */

    @Override
    public void selectEntity(Entity entity) {
        //copy entity
        if(!(entity instanceof Station)){
            System.out.println("Unexpected state");
            new Exception().printStackTrace();
            return;
        }
        sel = (Station) entity;

        //update the name
        stationName.setText(sel.entityModel().toString());
        stationName.setColor(state.findColorForOwner(sel.owner));
        stationIcon.setDrawable(Asset.retrieveEntityIcon(sel.model.tier));

        //update the grid
        stationGrid.setText("[" + state.grids[sel.grid].nickname  +"]");
    }

    @Override
    public void updateEntity() {
        //name
        stationName.setColor(state.findColorForOwner(sel.owner));

        //update stage
        if(sel.stage == Station.Stage.RUBBLE){
            stageImage.setVisible(false);
        }
        else {
            Gdx.app.postRunnable(() -> {
                stageImage.setVisible(true);
                stageImage.setDrawable(Asset.retrieve(sel.getStageIcon(sel.stage)));
            });
        }

        //update stage timer
        if(sel.stage == Station.Stage.SHIELDED || sel.stage == Station.Stage.RUBBLE){
            stageTimer.setVisible(false);
        }
        else {
            stageTimer.setVisible(true);
            stageTimer.setText(Math.round(sel.stageTimer));
            Main.glyph.setText(stageTimer.getStyle().font, stageTimer.getText());
            stageTimer.setX(266-Main.glyph.width);
        }

        //update health
        switch(sel.stage){
            case SHIELDED:
                healthFill.setWidth(200 * sel.shieldHealth / sel.model.maxShield);
                healthLabel.setVisible(true);
                healthLabel.setText(String.format("%" + (1+2*((int) Math.log10(sel.model.maxShield)+1)) + "s",
                        ((int) Math.ceil(sel.shieldHealth)) + "/" + sel.model.maxShield));
                break;
            case ARMORED:
            case VULNERABLE:
                healthFill.setWidth(200 * sel.hullHealth / sel.model.maxHull);
                healthLabel.setVisible(true);
                healthLabel.setText(String.format("%" + (1+2*((int) Math.log10(sel.model.maxHull)+1)) + "s",
                        ((int) Math.ceil(sel.hullHealth)) + "/" + sel.model.maxHull));
                break;
            case RUBBLE:
                healthFill.setVisible(false);
                healthLabel.setVisible(false);
                break;
        }
    }

    @Override
    public Entity getSelectedEntity() {
        return sel;
    }
}
