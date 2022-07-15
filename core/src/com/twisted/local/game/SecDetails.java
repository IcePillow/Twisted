package com.twisted.local.game;

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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.twisted.Main;
import com.twisted.local.game.util.TogImgButton;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.net.msg.gameReq.*;

/**
 * The details sector that shows the details of a particular entity when clicked on. Currently
 * displayed in the bottom left corner.
 *
 * Tree
    > parent
        > (0) decoration
        > (1) child parents (empty/ship/etc)
 */
class SecDetails extends Sector {

    //reference variables
    private Game game;

    //graphics utilities
    private Skin skin;

    //tree
    private Group parent, emptyParent, shipParent;
    private Group shipButtonGroup, shipWeaponGroup;
    private Label shipName, shipMoveCommand, shipGrid, shipPosition, shipVelocity, healthLabel,
            targetLabel;
    private TogImgButton targetButton;
    private Image healthFill;
    private TogImgButton[] weaponButtons;

    //graphics state
    int selectGridId, selectShipId; //note gridId can be -1 for in warp

    //input state
    private ExternalWait externalWait;

    //stored clicks
    private EntPtr storeEntClick;


    /**
     * Constructor
     */
    SecDetails(Game game){
        this.game = game;
        this.skin = game.skin;

        externalWait = ExternalWait.NONE;
    }

    /**
     * Initialize the group.
     */
    @Override
    Group init() {
        parent = super.init();
        parent.setBounds(0, 100, 300, 125);

        //decoration group
        Group decoration = new Group();

        //add the main window background
        Image ribbon = new Image(new Texture(Gdx.files.internal("images/pixels/darkpurple.png")));
        ribbon.setSize(parent.getWidth(), parent.getHeight());
        decoration.addActor(ribbon);
        Image embedded = new Image(new Texture(Gdx.files.internal("images/pixels/black.png")));
        embedded.setBounds(3, 3, parent.getWidth()-6, parent.getHeight()-6);
        decoration.addActor(embedded);

        parent.addActor(decoration);

        //create the ship details group
        parent.addActor(initEmptySubgroup());
        initShipSubgroup();

        return parent;
    }

    /**
     * Init the base level of the subgroup.
     */
    private Group initEmptySubgroup(){
        emptyParent = new Group();

        Label label = new Label("[Selection]", skin, "medium", Color.WHITE);
        label.setPosition(8, 100);
        emptyParent.addActor(label);

        return emptyParent;
    }

    /**
     * Init the ship subgroup.
     */
    private void initShipSubgroup(){
        //initialize the details group
        shipParent = new Group();

        //initialize the higher direct children of shipParent
        Group topTextGroup = new Group();
        topTextGroup.setPosition(6, 100);
        shipParent.addActor(topTextGroup);

        Group locationGroup = new Group();
        locationGroup.setPosition(6, 42);
        shipParent.addActor(locationGroup);

        Group healthGroup = new Group();
        healthGroup.setPosition(6, 87);
        shipParent.addActor(healthGroup);

        shipButtonGroup = new Group();
        shipButtonGroup.setPosition(6, 4);
        shipParent.addActor(shipButtonGroup);

        shipWeaponGroup = new Group();
        shipWeaponGroup.setPosition(190, 30);
        shipParent.addActor(shipWeaponGroup);

        // Children of the topTextGroup \\
        shipGrid = new Label("[?]", skin, "medium", Color.WHITE);
        topTextGroup.addActor(shipGrid);

        shipName = new Label("[Ship Name]", skin, "medium", Color.WHITE);
        shipName.setPosition(30, 0);
        topTextGroup.addActor(shipName);

        shipMoveCommand = new Label("[movement cmd]", skin, "small", Color.LIGHT_GRAY);
        shipMoveCommand.setFontScale(0.9f);
        game.glyph.setText(skin.getFont("small"), shipMoveCommand.getText());
        shipMoveCommand.setPosition(290 - game.glyph.width*shipMoveCommand.getFontScaleX(), 0);
        topTextGroup.addActor(shipMoveCommand);

        // Children of the locationGroup \\
        shipPosition = new Label("[x]\n[y]", skin, "small", Color.LIGHT_GRAY);
        locationGroup.addActor(shipPosition);

        shipVelocity = new Label("[sp]\n[ag]", skin, "small", Color.LIGHT_GRAY);
        shipVelocity.setPosition(84, 0);
        locationGroup.addActor(shipVelocity);

        // Children of the healthGroup \\
        Image healthOutline = new Image(new Texture(Gdx.files.internal("images/pixels/gray.png")));
        healthOutline.setBounds(0, 0, 202, 10);
        healthGroup.addActor(healthOutline);

        Image healthInline = new Image(new Texture(Gdx.files.internal("images/pixels/darkgray.png")));
        healthInline.setBounds(1, 1, 200, 8);
        healthGroup.addActor(healthInline);

        healthFill = new Image(new Texture(Gdx.files.internal("images/pixels/green.png")));
        healthFill.setBounds(1, 1, 200, 8);
        healthGroup.addActor(healthFill);

        healthLabel = new Label("[health]", skin, "small",
                new Color(0,0.49f,0,1));
        healthLabel.setPosition(204, -7);
        healthGroup.addActor(healthLabel);

        // Children of shipButtonGroup \\
        ImageButton moveButton = new ImageButton(new TextureRegionDrawable(
                new Texture(Gdx.files.internal("images/ui/buttons/move.png"))));
        moveButton.setBounds(0, 0, 24, 24);
        shipButtonGroup.addActor(moveButton);
        ImageButton orbitButton = new ImageButton(new TextureRegionDrawable(
                new Texture(Gdx.files.internal("images/ui/buttons/orbit.png"))));
        orbitButton.setBounds(28, 0, 24, 24);
        shipButtonGroup.addActor(orbitButton);
        ImageButton alignButton = new ImageButton(new TextureRegionDrawable(
                new Texture(Gdx.files.internal("images/ui/buttons/align.png"))));
        alignButton.setBounds(56, 0, 24, 24);
        shipButtonGroup.addActor(alignButton);
        ImageButton warpButton = new ImageButton(new TextureRegionDrawable(
                new Texture(Gdx.files.internal("images/ui/buttons/warp.png"))));
        warpButton.setBounds(84, 0, 24, 24);
        shipButtonGroup.addActor(warpButton);

        //listeners for the shipButtonGroup
        SecDetails thisCrossSectorListener = this;
        moveButton.addCaptureListener((Event event) -> {
            if(event.isHandled()) return true;

            if(event instanceof ChangeListener.ChangeEvent){
                game.updateCrossSectorListening(this, "Move command...");
                game.viewportSelection(SecViewport.Select.LINE, true, Entity.Type.Ship, selectGridId, selectShipId);
                this.externalWait = ExternalWait.MOVE;
                return true;
            }
            return true;
        });
        orbitButton.addCaptureListener((Event event) -> {
            if(event.isHandled()) return true;
            if(event instanceof ChangeListener.ChangeEvent){
                game.updateCrossSectorListening(this, "Orbit command...");
                this.externalWait = ExternalWait.ORBIT_WHO;
            }
            return true;
        });
        alignButton.addCaptureListener((Event event) -> {
            if(event.isHandled()) return true;

            if(event instanceof ChangeListener.ChangeEvent){
                game.updateCrossSectorListening(this, "Align command...");
                this.externalWait = ExternalWait.ALIGN;
                return true;
            }
            return true;
        });
        warpButton.addCaptureListener((Event event) -> {
            if(event.isHandled()) return true;

            if(event instanceof ChangeListener.ChangeEvent){
                game.updateCrossSectorListening(this, "Warp command...");
                this.externalWait = ExternalWait.WARP;
            }
            return true;
        });

        // Children of the shipWeaponGroup \\
        targetLabel = new Label("[firing]", skin, "small", Color.WHITE);
        targetLabel.setPosition(28, 0);
        shipWeaponGroup.addActor(targetLabel);

        targetButton = new TogImgButton(
                new TextureRegionDrawable(new Texture(Gdx.files.internal("images/ui/buttons/target-off.png"))),
                new TextureRegionDrawable(new Texture(Gdx.files.internal("images/ui/buttons/target-on.png")))
        );
        targetButton.setBounds(0, 0, 24, 24);
        shipWeaponGroup.addActor(targetButton);

        weaponButtons = new TogImgButton[3];
        for(int i=0; i<weaponButtons.length; i++){
            weaponButtons[i] = new TogImgButton(null, null);
            weaponButtons[i].setBounds(i*28, 28, 24, 24);
            shipWeaponGroup.addActor(weaponButtons[i]);
        }

        //listeners for the shipWeaponGroup
        targetButton.changeClickListener(new ClickListener(Input.Buttons.LEFT){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(event.isHandled()) return;

                //listen for an entity to select
                if(selectGridId > -1 && state.grids[selectGridId].ships.get(selectShipId).targetingState == null){
                    game.updateCrossSectorListening(thisCrossSectorListener, "Target command...");
                    externalWait = ExternalWait.TARGET;
                }
                //cancel targeting
                else {
                    game.sendGameRequest(new MTargetReq(selectGridId, selectShipId, null, -1));
                }

                event.handle();
            }
        });
        for(int i=0; i<weaponButtons.length; i++){
            TogImgButton button = weaponButtons[i];
            int weaponId = i;

            button.changeClickListener(new ClickListener(Input.Buttons.LEFT){
                @Override
                public void clicked(InputEvent event, float x, float y){
                    if(event.isHandled()) return;
                    game.sendGameRequest(new MWeaponActiveReq(selectGridId, selectShipId, weaponId,
                            !state.grids[selectGridId].ships.get(selectShipId).weapons[weaponId].active));
                    event.handle();
                }
            });
        }
    }

    @Override
    void load() {

    }

    @Override
    void render(float delta) {
    }

    @Override
    void dispose() {

    }


    /* Event Methods */

    @Override
    void viewportClickEvent(Vector2 screenPos, Vector2 gamePos, Entity.Type type, int typeId) {
        if(game.getGrid() == selectGridId){
            //create the request
            MGameReq req = null;
            if(externalWait == ExternalWait.MOVE){
                req = new MShipMoveReq(selectGridId, selectShipId, gamePos);

                game.updateCrossSectorListening(null, null);
            }
            else if(externalWait == ExternalWait.ALIGN){
                Ship s = state.grids[selectGridId].ships.get(selectShipId);
                req = new MShipAlignReq(selectGridId, selectShipId,
                        (float) Math.atan2(gamePos.y-s.pos.y, gamePos.x-s.pos.x));

                game.updateCrossSectorListening(null, null);
            }
            else if(externalWait == ExternalWait.TARGET){
                if(type != null){
                    req = new MTargetReq(selectGridId, selectShipId, type, typeId);
                }

                game.updateCrossSectorListening(null, null);
            }
            else if(externalWait == ExternalWait.ORBIT_WHO){
                if(type != null){
                    storeEntClick = new EntPtr(type, typeId, game.getGrid());

                    game.updateCrossSectorListening(this, "Orbit radius command...");
                    externalWait = ExternalWait.ORBIT_DIST;
                    game.viewportSelection(SecViewport.Select.CIRCLE, true, type, selectGridId, typeId);
                }
                else {
                    game.updateCrossSectorListening(null, null);
                }
            }
            else if(externalWait == ExternalWait.ORBIT_DIST){
                Entity entity = null;
                if(storeEntClick.type == Entity.Type.Ship){
                    entity = state.grids[selectGridId].ships.get(storeEntClick.id);
                }
                else if(storeEntClick.type == Entity.Type.Station){
                    entity = state.grids[selectGridId].station;
                }

                if(entity != null){
                    float radius = gamePos.dst(entity.pos);

                    req = new MShipOrbitReq(selectGridId, selectShipId, storeEntClick.type, storeEntClick.id,
                            radius);
                }

                game.updateCrossSectorListening(null, null);
            }
            else {
                game.updateCrossSectorListening(null, null);
            }

            //send the message to the server
            if(req != null) game.sendGameRequest(req);
        }
        else {
            game.addToLog("Can't cmd a ship on a dif grid", SecLog.LogColor.YELLOW);
            game.updateCrossSectorListening(null, null);
        }
    }

    @Override
    void minimapClickEvent(int grid){
        if(selectGridId != -1){
            //create the request
            MGameReq req = null;
            if(externalWait == ExternalWait.WARP){
                req = new MShipWarpReq(selectGridId, selectShipId, grid);
            }
            else if(externalWait == ExternalWait.ALIGN){
                //calculate the angle
                Vector2 g2 = state.grids[grid].pos;
                Vector2 g1 = state.grids[selectGridId].pos;
                float angle = (float) Math.atan2(g2.y-g1.y, g2.x-g1.x);

                //create the request
                req = new MShipAlignReq(selectGridId, selectShipId, angle);
            }

            //send the message to the server
            if(req != null) game.sendGameRequest(req);
        }
        else {
            game.addToLog("Cannot command a ship that is currently in warp", SecLog.LogColor.YELLOW);
        }

        //release the cross sector listening
        game.updateCrossSectorListening(null, null);
    }

    @Override
    void fleetClickEvent(Entity entity, int gridId){
        //create the request
        MGameReq req = null;
        if(externalWait == ExternalWait.TARGET){
            if(gridId == selectGridId){
                req = new MTargetReq(selectGridId, selectShipId, entity.getEntityType(), entity.getId());
            }
            else {
                game.addToLog("Can't target entity on dif grid", SecLog.LogColor.YELLOW);
            }

            game.updateCrossSectorListening(null, null);
        }
        else if(externalWait == ExternalWait.ORBIT_WHO){
            storeEntClick = new EntPtr(entity.getEntityType(), entity.getId(), gridId);

            game.updateCrossSectorListening(this, "Orbit radius command...");
            externalWait = ExternalWait.ORBIT_DIST;
            game.viewportSelection(SecViewport.Select.CIRCLE, true, entity.getEntityType(),
                    selectGridId, entity.getId());
        }
        else {
            game.updateCrossSectorListening(null, null);
        }

        //send the message to the server
        if(req != null) game.sendGameRequest(req);
    }

    @Override
    void crossSectorListeningCancelled(){
        if(externalWait == ExternalWait.ORBIT_DIST){
            game.viewportSelection(SecViewport.Select.CIRCLE, false, null, 0, 0);
        }
        else if(externalWait == ExternalWait.MOVE){
            game.viewportSelection(SecViewport.Select.LINE, false, null, 0, 0);
        }

        externalWait = ExternalWait.NONE;
    }

    /**
     * A ship is selected to be displayed in the details sector
     */
    void shipSelected(int gridId, int shipId){
        //set the selections
        this.selectGridId = gridId;
        this.selectShipId = shipId;

        //update the primary child
        parent.removeActorAt(1, true);
        parent.addActor(shipParent);

        //get the ship
        Ship s;
        if(gridId != -1){
            s = state.grids[gridId].ships.get(shipId);
        }
        else {
            s = state.inWarp.get(shipId);
        }

        //call loading to prepare the sector for the new ship
        shipSelectedLoading(s);

        //call update to make the state changes
        updateShipData(s, gridId);
    }

    /**
     * Called when new data about the ship has come through.
     */
    void updateShipData(Ship s, int gridId){
        //meta data
        this.selectGridId = gridId;

        //update movement and calculate new layout data
        shipMoveCommand.setText(s.moveCommand);
        game.glyph.setText(skin.getFont("small"), shipMoveCommand.getText());
        shipMoveCommand.setX(290 - game.glyph.width*shipMoveCommand.getFontScaleX());

        //update the health
        healthLabel.setText(String.format("%" + (1+2*((int) Math.log10(s.getMaxHealth())+1)) + "s",
                ((int) Math.ceil(s.health)) + "/" + s.getMaxHealth()));
        healthFill.setWidth(200 * s.health / s.getMaxHealth());

        //update targeting text
        if(s.targetEntity != null){
            switch(s.targetingState){
                case Locking:
                    if(s.targetTimeToLock > 0.95f) targetLabel.setText(Math.round(s.targetTimeToLock) + "s");
                    else targetLabel.setText(Main.df1.format(s.targetTimeToLock) + "s");
                    break;
                case Locked:
                    targetLabel.setText("[X]");
                    break;
            }
        }
        else {
            targetLabel.setText("");
        }

        //targeting button
        targetButton.updateVisible(s.targetingState==null);

        //update active weapons
        for(int i=0; i<s.weapons.length; i++){
            weaponButtons[i].updateVisible(!s.weapons[i].active);
        }

        //warp dependent
        if(gridId != -1){
            shipGrid.setText("[" + state.grids[gridId].nickname  +"]");

            float[] rounded = s.roundedPosition(1);
            shipPosition.setText("X = " + rounded[0] + "\nY = " + rounded[1]);

            rounded = s.roundedBearing(2);
            shipVelocity.setText("Sp = " + rounded[0] + "\nAg = " + (int) rounded[1]);
        }
        else {
            shipGrid.setText("[W]");
            shipPosition.setText("X = ?\nY = ?");
            shipVelocity.setText("Sp = ?\nAg = ?");
        }
    }


    /* Utility Methods */

    /**
     * Does stuff to load in ship that only needs to be done once when it's selected as opposed
     * to be being done in updateShipData.
     */
    private void shipSelectedLoading(Ship s){
        //update the name
        shipName.setText(s.getType().toString());
        shipName.setColor(state.players.get(s.owner).color.object);

        //update the weapon button visibility
        for(int i=0; i<weaponButtons.length; i++){
            //set visibility
            weaponButtons[i].setVisible(i < s.getWeaponSlots().length);

            //update the kind of each weapon
            if(weaponButtons[i].isVisible()){
                TextureRegionDrawable one=null, two=null;

                switch(s.weapons[i].getType()){
                    case Blaster:
                        //TODO make new images for this
                        one = new TextureRegionDrawable(new Texture(Gdx.files.internal("images/ui/buttons/blaster-off.png")));
                        two = new TextureRegionDrawable(new Texture(Gdx.files.internal("images/ui/buttons/blaster-on.png")));
                        break;
                }

                if(one != null){
                    weaponButtons[i].switchTextures(one, two);
                }
            }
        }

        //visibility based on ownership
        shipButtonGroup.setVisible(s.owner == state.myId);
        shipWeaponGroup.setVisible(s.owner == state.myId);
    }


    /* Enums */

    /**
     * What this sector is listening for from any given external sector.
     */
    private enum ExternalWait {
        //all
        NONE,
        //viewport
        MOVE,
        ORBIT_WHO,
        ORBIT_DIST,
        TARGET,
        //viewport & minimap
        ALIGN,
        //minimap
        WARP,
    }
}
