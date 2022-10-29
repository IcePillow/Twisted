package com.twisted.local.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.twisted.Asset;
import com.twisted.Main;
import com.twisted.local.game.SecDetails;
import com.twisted.local.lib.ProgressButton;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.entities.station.Station;

public class DetsShipInSpace extends DetsGroup {

    //finals
    private final Color PROGRESS_COLOR = new Color(0.9f, 0.9f, 0.9f, 0.5f);

    //tree
    private Group shipButtonGroup, shipWeaponGroup;
    private Label shipName, shipMoveCommand, shipGrid, shipPosition, shipVelocity, healthLabel;
    private Image healthFill, shipIcon;
    private ProgressButton[] weaponButtons;
    private Image[] targetIconImages, targetFrameImages;
    private Label[] targetTimerLabels;
    private ProgressButton warpButton;

    //selection
    private Ship sel;

    //cache
    private Entity[] cacheStoreWeaponTargets; //should always be accessed through cacheWeaponTarget()
    private Entity cacheWeaponTarget(int idx){
        //update
        if(sel==null || sel.weapons[idx].getTarget()==null){
            cacheStoreWeaponTargets[idx] = null;
        }
        else if(!sel.weapons[idx].getTarget().matches(cacheStoreWeaponTargets[idx])) {
            cacheStoreWeaponTargets[idx] = sel.weapons[idx].getTarget().retrieveFromGrid(state.grids[sel.grid]);
        }

        //return
        return cacheStoreWeaponTargets[idx];
    }


    /* Construction */

    public DetsShipInSpace(SecDetails sector, Skin skin, Vector2 size) {
        super(sector, skin, size);

        Table topTextTable = createTopTextGroup();
        topTextTable.setBounds(6, 110, 300-12, 1);
        this.addActor(topTextTable);

        Group locationGroup = createLocationGroup();
        locationGroup.setPosition(6, 42);
        this.addActor(locationGroup);

        Group healthGroup = createHealthGroup();
        healthGroup.setPosition(6, 89);
        this.addActor(healthGroup);

        shipButtonGroup = createShipButtonGroup();
        shipButtonGroup.setPosition(6, 4);
        this.addActor(shipButtonGroup);

        shipWeaponGroup = createShipWeaponGroup();
        shipWeaponGroup.setPosition(190, 30);
        this.addActor(shipWeaponGroup);
    }

    private Table createTopTextGroup(){
        Table table = new Table();

        shipIcon = new Image(Asset.retrieveEntityIcon(Ship.Tier.Frigate));
        shipIcon.setColor(Color.GRAY);
        table.add(shipIcon).padRight(2);

        shipGrid = new Label("[?]", Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        table.add(shipGrid).minWidth(20).padRight(2);

        shipName = new Label("[Ship Name]", Asset.labelStyle(Asset.Avenir.HEAVY_16));
        table.add(shipName).padRight(2).growX();

        shipMoveCommand = new Label("[movement cmd]", Asset.labelStyle(Asset.Avenir.MEDIUM_12));
        shipMoveCommand.setColor(Color.LIGHT_GRAY);
        table.add(shipMoveCommand);

        return table;
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

        healthLabel = new Label("[health]", Asset.labelStyle(Asset.Avenir.MEDIUM_14));
        healthLabel.setColor(new Color(0,0.49f,0,1));
        healthLabel.setPosition(204, -3);
        group.addActor(healthLabel);

        return group;
    }

    private Group createShipButtonGroup(){
        shipButtonGroup = new Group();

        //create the image buttons
        ImageButton stopButton = new ImageButton(Asset.retrieve(Asset.UiButton.STOP));
        stopButton.setBounds(0, 0, 24, 24);
        shipButtonGroup.addActor(stopButton);
        ImageButton moveButton = new ImageButton(Asset.retrieve(Asset.UiButton.MOVE));
        moveButton.setBounds(28, 0, 24, 24);
        shipButtonGroup.addActor(moveButton);
        ImageButton orbitButton = new ImageButton(Asset.retrieve(Asset.UiButton.ORBIT));
        orbitButton.setBounds(56, 0, 24, 24);
        shipButtonGroup.addActor(orbitButton);
        ImageButton alignButton = new ImageButton(Asset.retrieve(Asset.UiButton.ALIGN));
        alignButton.setBounds(84, 0, 24, 24);
        shipButtonGroup.addActor(alignButton);
        warpButton = new ProgressButton(Asset.retrieve(Asset.UiButton.WARP), null, 1, 1);
        warpButton.setProgressColor(PROGRESS_COLOR);
        warpButton.setBounds(112, 0, 24, 24);
        shipButtonGroup.addActor(warpButton);
        ImageButton dockButton = new ImageButton(Asset.retrieve(Asset.UiButton.DOCK));
        dockButton.setBounds(140, 0, 24, 24);
        shipButtonGroup.addActor(dockButton);

        //hover listeners
        dockButton.addListener((Event event) -> {
            if(event instanceof InputEvent){
                if(((InputEvent) event).getType() == InputEvent.Type.enter){
                    sector.input(sel, SecDetails.Input.SHIP_DOCK_HOVER_ON);
                }
                else if(((InputEvent) event).getType() == InputEvent.Type.exit){
                    sector.input(sel, SecDetails.Input.SHIP_DOCK_HOVER_OFF);
                }
            }

            return true;
        });

        //click listeners
        stopButton.addCaptureListener((Event event) -> {
            if(event.isHandled()) return true;

            if(event instanceof ChangeListener.ChangeEvent){
                sector.input(sel, SecDetails.Input.SHIP_STOP);
                return true;
            }
            return true;
        });
        moveButton.addCaptureListener((Event event) -> {
            if(event.isHandled()) return true;
            if(event instanceof ChangeListener.ChangeEvent){
                sector.input(sel, SecDetails.Input.SHIP_MOVE);
                return true;
            }
            return true;
        });
        orbitButton.addCaptureListener((Event event) -> {
            if(event.isHandled()) return true;
            if(event instanceof ChangeListener.ChangeEvent){
                sector.input(sel, SecDetails.Input.SHIP_ORBIT);
            }
            return true;
        });
        alignButton.addCaptureListener((Event event) -> {
            if(event.isHandled()) return true;
            if(event instanceof ChangeListener.ChangeEvent){
                sector.input(sel, SecDetails.Input.SHIP_ALIGN);
                return true;
            }
            return true;
        });
        warpButton.changeClickListener(new ClickListener(Input.Buttons.LEFT){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(event.isHandled()) return;
                sector.input(sel, SecDetails.Input.SHIP_WARP);
                event.handle();
            }
        });
        dockButton.addCaptureListener((Event event) -> {
            if(event.isHandled()) return true;

            if(event instanceof ChangeListener.ChangeEvent){
                sector.input(sel, SecDetails.Input.SHIP_DOCK);
            }

            return true;
        });

        return shipButtonGroup;
    }

    private Group createShipWeaponGroup(){
        shipWeaponGroup = new Group();

        //weapon buttons
        weaponButtons = new ProgressButton[3];
        for(int i=0; i<weaponButtons.length; i++){
            weaponButtons[i] = new ProgressButton(null, null, 1, 1);
            weaponButtons[i].setProgressColor(PROGRESS_COLOR);
            weaponButtons[i].setBounds(i*28, 28, 24, 24);
            shipWeaponGroup.addActor(weaponButtons[i]);
        }

        //target entity icons
        targetIconImages = new Image[3];
        for(int i=0; i<targetIconImages.length; i++){
            targetIconImages[i] = new Image(Asset.retrieveEntityIcon(Station.Model.Extractor));
            targetIconImages[i].setPosition(i*28+4, 4);
            shipWeaponGroup.addActor(targetIconImages[i]);
        }
        //target frame icons
        targetFrameImages = new Image[3];
        for(int i=0; i<targetFrameImages.length; i++){
            targetFrameImages[i] = new Image(Asset.retrieve(Asset.UiIcon.TARGET_FRAME));
            targetFrameImages[i].setColor(Color.LIGHT_GRAY);
            targetFrameImages[i].setPosition(i*28, 0);
            shipWeaponGroup.addActor(targetFrameImages[i]);
        }
        //target timer labels
        targetTimerLabels = new Label[3];
        for(int i=0; i<targetTimerLabels.length; i++){
            targetTimerLabels[i] = new Label("", Asset.labelStyle(Asset.Avenir.MEDIUM_14));
            targetTimerLabels[i].setColor(Color.LIGHT_GRAY);
            targetTimerLabels[i].setPosition(i*28, 12);
            shipWeaponGroup.addActor(targetTimerLabels[i]);
        }

        //cache
        cacheStoreWeaponTargets = new Entity[3];

        //listeners
        for(int i=0; i<weaponButtons.length; i++){
            ProgressButton button = weaponButtons[i];
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

        //update the name and grid and icon
        shipName.setText(sel.model.toString());
        shipName.setColor(state.findColorForOwner(sel.owner));
        shipGrid.setText("[" + state.grids[sel.grid].nickname  +"]");
        shipIcon.setDrawable(Asset.retrieveEntityIcon(sel.model.tier));

        //weapon buttons
        for(int i=0; i<weaponButtons.length; i++){
            //set visibility
            weaponButtons[i].setVisible(i < sel.model.weapons.length);

            //update the kind of each weapon
            if(weaponButtons[i].isVisible()){
                int iSave = i;
                Gdx.app.postRunnable(() -> {
                    Asset.UiButton key = sel.weapons[iSave].getCurrentButtonAsset();
                    weaponButtons[iSave].switchTexture(Asset.retrieve(key), key);
                });
            }
        }

        //weapon targets
        for(int i=sel.weapons.length; i<3; i++){
            targetIconImages[i].setVisible(false);
            targetFrameImages[i].setVisible(false);
            targetTimerLabels[i].setVisible(false);
        }

        //visibility based on ownership
        shipButtonGroup.setVisible(sel.owner == state.myId);
        shipWeaponGroup.setVisible(sel.owner == state.myId);
    }
    @Override
    public void updateEntity() {
        //update movement and calculate new layout data
        shipMoveCommand.setText(sel.moveDescription);

        //update the health
        healthLabel.setText(String.format("%" + (1+2*((int) Math.log10(sel.model.maxHealth)+1)) + "s",
                ((int) Math.ceil(sel.health)) + "/" + sel.model.maxHealth));
        healthFill.setWidth(200 * sel.health / sel.model.maxHealth);

        //warping button progress
        warpButton.setProgress(sel.warpCharge);

        //update weapons
        for(int i=0; i<sel.weapons.length; i++){
            //update the texture
            if(sel.weapons[i].getCurrentButtonAsset() != null &&
                    !sel.weapons[i].getCurrentButtonAsset().equals(weaponButtons[i].getTextureKey())){
                int iSave = i;
                Gdx.app.postRunnable(() -> {
                    Asset.UiButton key = sel.weapons[iSave].getCurrentButtonAsset();
                    weaponButtons[iSave].switchTexture(Asset.retrieve(key), key);
                });
            }

            //progress on cooldown
            if(sel.weapons[i].cooldown > 0){
                weaponButtons[i].setProgress(sel.weapons[i].cooldown / sel.weapons[i].getFullCooldown());
            }
            //progress off with no cooldown
            else if(!sel.weapons[i].isActive() || sel.weapons[i].getFullTimer() == 0){
                weaponButtons[i].setProgress(0);
            }
            //progress while active
            else {
                weaponButtons[i].setProgress(1 - sel.weapons[i].timer / sel.weapons[i].getFullTimer());
            }

            //target frame images
            targetFrameImages[i].setVisible(sel.weapons[i].isActive());

            //target locked
            if(sel.weapons[i].isLocked()){
                targetIconImages[i].setVisible(true);
                targetTimerLabels[i].setVisible(false);

                targetIconImages[i].setColor(state.findColorForOwner(cacheWeaponTarget(i).owner));
                int iSave = i;
                Gdx.app.postRunnable(() -> targetIconImages[iSave].setDrawable(Asset.retrieveEntityIcon(
                        cacheWeaponTarget(iSave).entityModel().getTier())));
            }
            //target locking or doesn't need targets
            else if(sel.weapons[i].isActive()){
                targetIconImages[i].setVisible(false);
                targetTimerLabels[i].setVisible(true);

                if(sel.weapons[i].requiresTarget()){
                    targetTimerLabels[i].setText(sel.weapons[i].getLockTimerText());
                }
                else {
                    targetTimerLabels[i].setText("X");
                }

                int iSave = i;
                Gdx.app.postRunnable(() -> {
                    Main.glyph.setText(targetTimerLabels[iSave].getStyle().font, targetTimerLabels[iSave].getText());
                    targetTimerLabels[iSave].setX(iSave*28 + 12 - Main.glyph.width/2f);
                });
            }
            //target not active
            else {
                targetIconImages[i].setVisible(false);
                targetTimerLabels[i].setVisible(false);
            }
        }

        //position location
        float[] rounded = sel.roundedPos(1);
        shipPosition.setText("X = " + rounded[0] + "\nY = " + rounded[1]);
        rounded = sel.roundedBear(2);
        shipVelocity.setText("Sp = " + rounded[0] + "\nAg = " + (int) rounded[1]);
    }
    @Override
    public Entity getSelectedEntity(){
        return sel;
    }
}
