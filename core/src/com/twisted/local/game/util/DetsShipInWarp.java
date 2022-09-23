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

    private Group shipWeaponGroup;
    private Label shipName, shipMoveCommand;
    private Label healthLabel;
    private Image healthFill, shipIcon;
    private TogImgButton[] weaponButtons;

    //selection
    private Ship sel;


    /* Construction */

    public DetsShipInWarp(SecDetails sector, Skin skin, Vector2 size){
        super(sector, skin, size);

        Group topTextGroup = createTopTextGroup();
        topTextGroup.setPosition(6, 100);
        this.addActor(topTextGroup);

        Group healthGroup = createHealthGroup();
        healthGroup.setPosition(6, 89);
        this.addActor(healthGroup);

        shipWeaponGroup = createShipWeaponGroup();
        shipWeaponGroup.setPosition(190, 30);
        this.addActor(shipWeaponGroup);
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

    private Group createShipWeaponGroup(){
        shipWeaponGroup = new Group();

        //create actors
        weaponButtons = new TogImgButton[3];
        for(int i=0; i<weaponButtons.length; i++){
            weaponButtons[i] = new TogImgButton(null, null);
            weaponButtons[i].setBounds(i*28, 28, 24, 24);
            shipWeaponGroup.addActor(weaponButtons[i]);
        }

        //listeners
        for(int i=0; i<weaponButtons.length; i++){
            TogImgButton button = weaponButtons[i];
            int weaponId = i;

            button.changeClickListener(new ClickListener(Input.Buttons.LEFT){
                @Override
                public void clicked(InputEvent event, float x, float y){
                    if(event.isHandled()) return;
                    sector.input(sel, SecDetails.Input.SHIP_WEAPON_TOGGLE, weaponId);
                    event.handle();
                }
            });
            button.changeEnterListener(event -> {
                if(event instanceof InputEvent){
                    if(((InputEvent) event).getType() == InputEvent.Type.enter){
                        sector.input(sel, SecDetails.Input.SHIP_WEAPON_HOVER_ON, weaponId);
                    }
                    else if(((InputEvent) event).getType() == InputEvent.Type.exit){
                        sector.input(sel, SecDetails.Input.SHIP_WEAPON_HOVER_OFF, weaponId);

                    }
                }
                return false;
            });
        }

        return shipWeaponGroup;
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

        //update the weapon button visibility
        for(int i=0; i<weaponButtons.length; i++){
            //set visibility
            weaponButtons[i].setVisible(i < sel.model.weapons.length);

            //update the kind of each weapon
            if(weaponButtons[i].isVisible()){
                int iSave = i;
                Gdx.app.postRunnable(() -> {
                    weaponButtons[iSave].switchTextures(
                            Asset.retrieve(sel.weapons[iSave].getOffButtonAsset()),
                            Asset.retrieve(sel.weapons[iSave].getOnButtonAsset()));
                });
            }
        }

        //visibility based on ownership
        shipWeaponGroup.setVisible(sel.owner == state.myId);
    }

    @Override
    public void updateEntity() {
        //update movement and calculate new layout data
        shipMoveCommand.setText(sel.moveCommand);
        Main.glyph.setText(shipMoveCommand.getStyle().font, shipMoveCommand.getText());
        shipMoveCommand.setX(290 - Main.glyph.width*shipMoveCommand.getFontScaleX());

        //update the health
        healthLabel.setText(String.format("%" + (1+2*((int) Math.log10(sel.model.maxHealth)+1)) + "s",
                ((int) Math.ceil(sel.health)) + "/" + sel.model.maxHealth));
        healthFill.setWidth(200 * sel.health / sel.model.maxHealth);

        //update active weapons
        for(int i = 0; i< sel.weapons.length; i++){
            weaponButtons[i].updateVisible(!sel.weapons[i].active);
        }
    }

    @Override
    public Entity getSelectedEntity() {
        return sel;
    }
}
