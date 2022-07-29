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
import com.twisted.logic.entities.Station;

public class DetsStation extends DetsGroup {

    //constants
    private final static Color BLUE_TEXT_COLOR = new Color(0,0.48f,0.84f,1);
    private final static Color GREEN_TEXT_COLOR = new Color(0,0.49f,0,1);

    //tree
    private Label stationGrid, stationName, stageTimer;
    private Image stageImage;
    private Image hullHealthFill, shieldHealthFill;
    private Label healthLabel;

    //selection
    private Station sel;


    /* Construction */

    public DetsStation(SecDetails sector, Skin skin, GlyphLayout glyph, Vector2 size) {
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

        stationGrid = new Label("[?]", skin, "medium", Color.WHITE);
        group.addActor(stationGrid);

        stationName = new Label("[Station Name]", skin, "medium", Color.WHITE);
        stationName.setX(30);
        group.addActor(stationName);

        stageTimer = new Label("", skin, "small", Color.LIGHT_GRAY);
        stageTimer.setPosition(260, 8);
        group.addActor(stageTimer);

        stageImage = new Image(sector.retrieveStationStageTex(Station.Stage.SHIELDED));
        stageImage.setX(270);
        group.addActor(stageImage);

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

        hullHealthFill = new Image(new Texture(Gdx.files.internal("images/pixels/green.png")));
        hullHealthFill.setBounds(1, 1, 200, 8);
        group.addActor(hullHealthFill);

        shieldHealthFill = new Image(new Texture(Gdx.files.internal("images/pixels/blue.png")));
        shieldHealthFill.setBounds(1, 1, 200, 8);
        group.addActor(shieldHealthFill);

        healthLabel = new Label("[health]", skin, "small", Color.WHITE);
        healthLabel.setPosition(204, -7);
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
        stationName.setText(sel.getType().toString());
        stationName.setColor(state.findColorForOwner(sel.owner));

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
            stageImage.setVisible(true);
            stageImage.setDrawable(sector.retrieveStationStageTex(sel.stage));
        }

        //update stage timer
        if(sel.stage == Station.Stage.SHIELDED || sel.stage == Station.Stage.RUBBLE){
            stageTimer.setVisible(false);
        }
        else {
            stageTimer.setVisible(true);
            stageTimer.setText(Math.round(sel.stageTimer));
            glyph.setText(skin.getFont("small"), stageTimer.getText());
            stageTimer.setX(266-glyph.width);
        }

        //update health
        shieldHealthFill.setWidth(200 * sel.shieldHealth / sel.getMaxShield());
        hullHealthFill.setWidth(200 * sel.hullHealth / sel.getMaxHull());
        switch(sel.stage){
            case SHIELDED:
                shieldHealthFill.setVisible(true);
                hullHealthFill.setVisible(false);
                healthLabel.setVisible(true);
                healthLabel.setColor(BLUE_TEXT_COLOR);

                healthLabel.setText(String.format("%" + (1+2*((int) Math.log10(sel.getMaxShield())+1)) + "s",
                        ((int) Math.ceil(sel.shieldHealth)) + "/" + sel.getMaxShield()));
                break;
            case ARMORED:
            case VULNERABLE:
                shieldHealthFill.setVisible(false);
                hullHealthFill.setVisible(true);
                healthLabel.setVisible(true);
                healthLabel.setColor(GREEN_TEXT_COLOR);

                healthLabel.setText(String.format("%" + (1+2*((int) Math.log10(sel.getMaxHull())+1)) + "s",
                        ((int) Math.ceil(sel.hullHealth)) + "/" + sel.getMaxHull()));
                break;
            case DEPLOYMENT:
            case RUBBLE:
                shieldHealthFill.setVisible(false);
                hullHealthFill.setVisible(false);
                healthLabel.setVisible(false);
                break;
        }
    }

    @Override
    public Entity getSelectedEntity() {
        return sel;
    }
}
