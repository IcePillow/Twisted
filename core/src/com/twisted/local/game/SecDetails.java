package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.net.msg.gameRequest.*;

/**
 * The details sector that shows the details of a particular entity when clicked on. Currently
 * displayed in the bottom left corner.
 *
 * Tree
    > parent
        > (0) decoration
        > (1) child parents (empty/ship/etc)
 */
class SecDetails extends Sector{

    //reference variables
    private Game game;

    //graphics utilities
    private Skin skin;

    //tree
    private Group parent, emptyParent, shipParent;
    private Group shipButtonGroup, shipModeGroup;
    private Label shipName, shipMoveCommand, shipGrid, shipPosition, shipVelocity, healthLabel,
                  aggressiveLabel, targetLabel;
    private Image healthFill;

    //graphics state
    int selectGridId, selectShipId; //note gridId can be -1 for in warp

    //input state
    private ExternalWait externalWait;


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

        //ship grid
        shipGrid = new Label("[?]", skin, "medium", Color.WHITE);
        shipGrid.setPosition(6, 100);
        shipParent.addActor(shipGrid);

        //ship name
        shipName = new Label("[Ship Name]", skin, "medium", Color.WHITE);
        shipName.setPosition(36, 100);
        shipParent.addActor(shipName);

        //ship movement command
        shipMoveCommand = new Label("[movement cmd]", skin, "small", Color.LIGHT_GRAY);
        shipMoveCommand.setFontScale(0.9f);
        game.glyph.setText(skin.getFont("small"), shipMoveCommand.getText());
        shipMoveCommand.setPosition(295 - game.glyph.width*shipMoveCommand.getFontScaleX(), 100);
        shipParent.addActor(shipMoveCommand);

        //ship position & velocity
        shipPosition = new Label("[x]\n[y]", skin, "small", Color.LIGHT_GRAY);
        shipPosition.setPosition(6, 42);
        shipParent.addActor(shipPosition);
        shipVelocity = new Label("[sp]\n[ag]", skin, "small", Color.LIGHT_GRAY);
        shipVelocity.setPosition(90, 42);
        shipParent.addActor(shipVelocity);

        //ship health
        Image healthOutline = new Image(new Texture(Gdx.files.internal("images/pixels/gray.png")));
        healthOutline.setBounds(6, 85, 202, 10);
        shipParent.addActor(healthOutline);
        Image healthInline = new Image(new Texture(Gdx.files.internal("images/pixels/darkgray.png")));
        healthInline.setBounds(7, 86, 200, 8);
        shipParent.addActor(healthInline);

        healthFill = new Image(new Texture(Gdx.files.internal("images/pixels/green.png")));
        healthFill.setBounds(7, 86, 200, 8);
        shipParent.addActor(healthFill);
        healthLabel = new Label("[health]", skin, "small",
                new Color(0,0.49f,0,1));
        healthLabel.setPosition(210, 78);
        shipParent.addActor(healthLabel);

        //create the buttons
        shipButtonGroup = new Group();
        shipButtonGroup.setPosition(8, 10);
        shipParent.addActor(shipButtonGroup);

        TextButton moveButton = new TextButton("M", skin, "emphasis");
        moveButton.setBounds(0, -6, 32, 32);
        shipButtonGroup.addActor(moveButton);
        TextButton alignButton = new TextButton("A", skin, "emphasis");
        alignButton.setBounds(38, -6, 32, 32);
        shipButtonGroup.addActor(alignButton);
        TextButton warpButton = new TextButton("W", skin, "emphasis");
        warpButton.setBounds(76, -6, 32, 32);
        shipButtonGroup.addActor(warpButton);
        TextButton aggroButton = new TextButton("G", skin, "emphasis");
        aggroButton.setBounds(124, -6, 32, 32);
        shipButtonGroup.addActor(aggroButton);
        TextButton targetButton = new TextButton("T", skin, "emphasis");
        targetButton.setBounds(162, -6, 32, 32);
        shipButtonGroup.addActor(targetButton);

        //listeners for the buttons
        moveButton.addCaptureListener((Event event) -> {
            if(event.isHandled()) return true;

            if(event instanceof ChangeListener.ChangeEvent){
                game.updateCrossSectorListening(this, "Move command...");
                this.externalWait = ExternalWait.MOVE;
                return true;
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
        aggroButton.addCaptureListener((Event event) -> {
            if(event.isHandled()) return true;

            if(event instanceof ChangeListener.ChangeEvent){
                MGameRequest req = new MShipAggro(selectGridId, selectShipId,
                        !state.grids[selectGridId].ships.get(selectShipId).aggro);
                game.sendGameRequest(req);
            }

            return true;
        });
        targetButton.addCaptureListener((Event event) -> {
            if(event.isHandled()) return true;

            if(event instanceof ChangeListener.ChangeEvent){
                game.updateCrossSectorListening(this, "Target command...");
                this.externalWait = ExternalWait.TARGET;
            }

            return true;
        });

        //create the mode group
        shipModeGroup = new Group();
        shipModeGroup.setPosition(190, 40);
        shipParent.addActor(shipModeGroup);

        aggressiveLabel = new Label("[aggro]", skin, "small", Color.LIGHT_GRAY);
        aggressiveLabel.setPosition(0, 20);
        shipModeGroup.addActor(aggressiveLabel);

        targetLabel = new Label("[firing]", skin, "small", Color.WHITE);
        targetLabel.setPosition(0, 0);
        shipModeGroup.addActor(targetLabel);
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

    //TODO consolidate most of shipSelected and updateShipData to one method

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

        //update the name
        shipName.setText(s.getType().toString());
        shipName.setColor(state.players.get(s.owner).color.object);

        //update the movement
        shipMoveCommand.setText(s.moveCommand);
        game.glyph.setText(skin.getFont("small"), shipMoveCommand.getText());
        shipMoveCommand.setX(295 - game.glyph.width*shipMoveCommand.getFontScaleX());

        //update the aggro display
        if(s.aggro){
            aggressiveLabel.setText("Aggressive");
        }
        else {
            aggressiveLabel.setText("Passive");
        }

        //update the targeting display
        if(s.targetingState != null){
            switch(s.targetingState){
                case Locking:
                    targetLabel.setText("Locking...");
                    break;
                case Locked:
                    targetLabel.setText("Locked");
                    break;
                case Firing:
                    targetLabel.setText("Firing");
                    break;
            }
        }

        //update the grid and position
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

        //update the health
        healthLabel.setText(String.format("%" + (1+2*((int) Math.log10(s.getMaxHealth())+1)) + "s",
                ((int) Math.ceil(s.health)) + "/" + s.getMaxHealth()));
        healthFill.setWidth(200 * s.health / s.getMaxHealth());

        //visibility based on ownership
        shipButtonGroup.setVisible(s.owner == state.myId);
        shipModeGroup.setVisible(s.owner == state.myId);
    }

    @Override
    void viewportClickEvent(Vector2 screenPos, Vector2 gamePos, Entity.Type type,
                            int typeId) {

        if(game.getGrid() == selectGridId){
            //create the request
            MGameRequest req = null;
            if(externalWait == ExternalWait.MOVE){
                req = new MShipMoveRequest(selectGridId, selectShipId, gamePos);
            }
            else if(externalWait == ExternalWait.ALIGN){
                Ship s = state.grids[selectGridId].ships.get(selectShipId);
                req = new MShipAlignRequest(selectGridId, selectShipId,
                        (float) Math.atan2(gamePos.y-s.pos.y, gamePos.x-s.pos.x));
            }
            else if(externalWait == ExternalWait.TARGET){
                if(type != null){
                    req = new MTargetRequest(selectGridId, selectShipId, type, typeId);
                }
            }

            //send the message to the server
            if(req != null) game.sendGameRequest(req);
        }
        else {
            game.addToLog("Can't cmd a ship on a dif grid", SecLog.LogColor.YELLOW);
        }

        game.updateCrossSectorListening(null, null);
    }

    @Override
    void minimapClickEvent(int grid){
        if(selectGridId != -1){
            //create the request
            MGameRequest req = null;
            if(externalWait == ExternalWait.WARP){
                req = new MShipWarpRequest(selectGridId, selectShipId, grid);
            }
            else if(externalWait == ExternalWait.ALIGN){
                //calculate the angle
                Vector2 g2 = state.grids[grid].pos;
                Vector2 g1 = state.grids[selectGridId].pos;
                float angle = (float) Math.atan2(g2.y-g1.y, g2.x-g1.x);

                //create the request
                req = new MShipAlignRequest(selectGridId, selectShipId, angle);
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
    void crossSectorListeningCancelled(){
        externalWait = ExternalWait.NONE;
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
        shipMoveCommand.setX(295 - game.glyph.width*shipMoveCommand.getFontScaleX());

        //update the health
        healthLabel.setText(String.format("%" + (1+2*((int) Math.log10(s.getMaxHealth())+1)) + "s",
                ((int) Math.ceil(s.health)) + "/" + s.getMaxHealth()));
        healthFill.setWidth(200 * s.health / s.getMaxHealth());

        //update aggressive
        if(s.aggro){
            aggressiveLabel.setText("Aggressive");
        }
        else {
            aggressiveLabel.setText("Passive");
        }

        //update targeting
        if(s.targetingType != null){
            switch(s.targetingState){
                case Locking:
                    targetLabel.setText("Locking...");
                    break;
                case Locked:
                    targetLabel.setText("Locked");
                    break;
                case Firing:
                    targetLabel.setText("Firing");
                    break;
            }
        }
        else {
            targetLabel.setText("");
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


    /* Enums */

    /**
     * What this sector is listening for from any given external sector.
     */
    private enum ExternalWait {
        //all
        NONE,
        //viewport
        MOVE,
        TARGET,
        //viewport & minimap
        ALIGN,
        //minimap
        WARP,
    }
}
