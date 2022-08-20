package com.twisted.local.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.twisted.Asset;
import com.twisted.Main;
import com.twisted.local.game.SecDetails;
import com.twisted.local.lib.ProgressButton;
import com.twisted.local.lib.TogImgButton;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;

public class DetsShipInSpace extends DetsGroup {

    //finals
    private final Color PROGRESS_COLOR = new Color(0.9f, 0.9f, 0.9f, 0.5f);

    //tree
    private Group shipButtonGroup, shipWeaponGroup;
    private Label shipName, shipMoveCommand, shipGrid;
    private Label shipPosition, shipVelocity, healthLabel;
    private Label targetLabel;
    private TogImgButton targetButton;
    private Image healthFill, targetImage;
    private ProgressButton[] weaponButtons;

    //selection
    private Ship sel;

    //caching, all cached values should be accessed through their methods
    private Entity cacheStoreTargetEnt;
    private Entity cacheTargetEnt(){
        //update
        if(sel.targetEntity == null ){
            cacheStoreTargetEnt = null;
        }
        else if(!sel.targetEntity.matches(cacheStoreTargetEnt)) {
            cacheStoreTargetEnt = sel.targetEntity.retrieveFromGrid(state.grids[sel.grid]);
        }

        //return
        return cacheStoreTargetEnt;
    }


    /* Construction */

    public DetsShipInSpace(SecDetails sector, Skin skin, GlyphLayout glyph, Vector2 size) {
        super(sector, skin, glyph, size);

        Group topTextGroup = createTopTextGroup();
        topTextGroup.setPosition(6, 100);
        this.addActor(topTextGroup);

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

    private Group createTopTextGroup(){
        Group group = new Group();

        shipGrid = new Label("[?]", skin, "medium", Color.WHITE);
        group.addActor(shipGrid);

        shipName = new Label("[Ship Name]", skin, "medium", Color.WHITE);
        shipName.setPosition(30, 0);
        group.addActor(shipName);

        shipMoveCommand = new Label("[movement cmd]", skin, "small", Color.LIGHT_GRAY);
        shipMoveCommand.setFontScale(0.9f);
        glyph.setText(skin.getFont("small"), shipMoveCommand.getText());
        shipMoveCommand.setPosition(288 - glyph.width*shipMoveCommand.getFontScaleX(), 0);
        group.addActor(shipMoveCommand);

        return group;
    }

    private Group createLocationGroup(){
        Group group = new Group();

        shipPosition = new Label("[x]\n[y]", skin, "small", Color.LIGHT_GRAY);
        group.addActor(shipPosition);

        shipVelocity = new Label("[sp]\n[ag]", skin, "small", Color.LIGHT_GRAY);
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
        ImageButton warpButton = new ImageButton(Asset.retrieve(Asset.UiButton.WARP));
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
        warpButton.addCaptureListener((Event event) -> {
            if(event.isHandled()) return true;
            if(event instanceof ChangeListener.ChangeEvent){
                sector.input(sel, SecDetails.Input.SHIP_WARP);
            }
            return true;
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

        //create actors
        targetImage = new Image(Asset.retrieve(Asset.EntityIcon.STATION));
        targetImage.setPosition(36, 3);
        shipWeaponGroup.addActor(targetImage);

        targetLabel = new Label("[none]", skin, "small", Color.WHITE);
        targetLabel.setPosition(28, 0);
        shipWeaponGroup.addActor(targetLabel);

        targetButton = new TogImgButton(
                new TextureRegionDrawable(new Texture(Gdx.files.internal("images/ui/buttons/target-off.png"))),
                new TextureRegionDrawable(new Texture(Gdx.files.internal("images/ui/buttons/target-on.png")))
        );
        targetButton.setBounds(0, 0, 24, 24);
        shipWeaponGroup.addActor(targetButton);

        weaponButtons = new ProgressButton[3];
        for(int i=0; i<weaponButtons.length; i++){
            weaponButtons[i] = new ProgressButton(null, null, 1, 1);
            weaponButtons[i].setProgressColor(PROGRESS_COLOR);
            weaponButtons[i].setBounds(i*28, 28, 24, 24);
            shipWeaponGroup.addActor(weaponButtons[i]);
        }

        //listeners
        targetButton.changeClickListener(new ClickListener(Input.Buttons.LEFT){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(event.isHandled()) return;
                sector.input(sel, SecDetails.Input.SHIP_TARGET);
                event.handle();
            }
        });
        targetButton.changeEnterListener(event -> {
            if(event instanceof InputEvent){
                if(((InputEvent) event).getType() == InputEvent.Type.enter){
                    sector.input(sel, SecDetails.Input.SHIP_TARGET_HOVER_ON);
                }
                else if(((InputEvent) event).getType() == InputEvent.Type.exit){
                    sector.input(sel, SecDetails.Input.SHIP_TARGET_HOVER_OFF);
                }
            }
            return false;
        });
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

        //update the name
        shipName.setText(sel.getType().toString());
        shipName.setColor(state.findColorForOwner(sel.owner));

        //update the grid
        shipGrid.setText("[" + state.grids[sel.grid].nickname  +"]");

        for(int i=0; i<weaponButtons.length; i++){
            //set visibility
            weaponButtons[i].setVisible(i < sel.getWeaponSlots().length);

            //update the kind of each weapon
            if(weaponButtons[i].isVisible()){
                int iSave = i;
                Gdx.app.postRunnable(() -> {
                    Asset.UiButton key = sel.weapons[iSave].getCurrentButtonAsset();
                    weaponButtons[iSave].switchTexture(Asset.retrieve(key), key);
                });
            }
        }

        //visibility based on ownership
        shipButtonGroup.setVisible(sel.owner == state.myId);
        shipWeaponGroup.setVisible(sel.owner == state.myId);
    }

    @Override
    public void updateEntity() {
        //update movement and calculate new layout data
        shipMoveCommand.setText(sel.moveCommand);
        glyph.setText(skin.getFont("small"), shipMoveCommand.getText());
        shipMoveCommand.setX(288 - glyph.width*shipMoveCommand.getFontScaleX());

        //update the health
        healthLabel.setText(String.format("%" + (1+2*((int) Math.log10(sel.getMaxHealth())+1)) + "s",
                ((int) Math.ceil(sel.health)) + "/" + sel.getMaxHealth()));
        healthFill.setWidth(200 * sel.health / sel.getMaxHealth());

        //update targeting text and image
        if(sel.targetEntity != null){
            switch(sel.targetingState){
                case Locking:
                    targetImage.setVisible(false);
                    if(sel.targetTimeToLock > 0.95f) targetLabel.setText(Math.round(sel.targetTimeToLock) + "s");
                    else targetLabel.setText(Main.df1.format(sel.targetTimeToLock) + "s");
                    break;
                case Locked:
                    Gdx.app.postRunnable(() -> {
                        targetImage.setDrawable(Asset.retrieve(cacheTargetEnt().getIconEnum()));
                        targetImage.setColor(state.findColorForOwner(cacheTargetEnt().owner));
                        targetImage.setVisible(true);
                    });
                    targetLabel.setText("[   ]");
                    break;
            }
        }
        else {
            targetImage.setVisible(false);
            targetLabel.setText("");
        }

        //targeting button
        targetButton.updateVisible(sel.targetingState==null);

        //update active weapons
        for(int i = 0; i<sel.weapons.length; i++){
            //update the texture
            if(sel.weapons[i].getCurrentButtonAsset() != null &&
                    !sel.weapons[i].getCurrentButtonAsset().equals(weaponButtons[i].getTextureKey())){
                int iSave = i;
                Gdx.app.postRunnable(() -> {
                    Asset.UiButton key = sel.weapons[iSave].getCurrentButtonAsset();
                    weaponButtons[iSave].switchTexture(Asset.retrieve(key), key);
                });
            }

            //update the progress
            if(!sel.weapons[i].active){
                weaponButtons[i].setProgress(0);
            }
            else if(sel.weapons[i].getFullTimer() == 0) {
                weaponButtons[i].setProgress(1);
            }
            else {
                weaponButtons[i].setProgress(1 - sel.weapons[i].timer / sel.weapons[i].getFullTimer());
            }
        }

        //warp dependent
        if(sel.grid != -1){
            float[] rounded = sel.roundedPosition(1);
            shipPosition.setText("X = " + rounded[0] + "\nY = " + rounded[1]);

            rounded = sel.roundedBearing(2);
            shipVelocity.setText("Sp = " + rounded[0] + "\nAg = " + (int) rounded[1]);
        }
        else {
            shipGrid.setText("[W]");
            shipPosition.setText("X = ?\nY = ?");
            shipVelocity.setText("Sp = ?\nAg = ?");
        }
    }

    @Override
    public Entity getSelectedEntity(){
        return sel;
    }
}
