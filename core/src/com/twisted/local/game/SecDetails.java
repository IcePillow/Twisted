package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.twisted.logic.entities.Ship;
import com.twisted.local.game.state.GameState;
import com.twisted.net.msg.gameRequest.MGameRequest;
import com.twisted.net.msg.gameRequest.MShipAlignRequest;
import com.twisted.net.msg.gameRequest.MShipMoveRequest;

/**
 * The details sector that shows the details of a particular entity when clicked on. Currently
 * displayed in the bottom left corner.
 *
 * Tree
    > parent
        > (0) decoration
        > (1) child parents (empty/ship/etc)
 */
public class SecDetails extends Sector{

    //reference variables
    private Game game;
    private GameState state;
    @Override
    public void setState(GameState state) {
        this.state = state;
    }

    //graphics utilities
    private Skin skin;

    //tree
    private Group parent, emptyParent, shipParent;
    private Group shipButtonGroup;
    private Label shipName;
    private Label shipMoveCommand;

    //graphics state
    int selectedGridId, selectedShipId;

    //input state
    private ViewportWait viewportWait;


    /**
     * Constructor
     */
    public SecDetails(Game game, Skin skin){
        this.game = game;
        this.skin = skin;

        viewportWait = ViewportWait.NONE;
    }

    /**
     * Initialize the group.
     */
    @Override
    Group init() {
        parent = new Group();
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

        //ship type name
        shipName = new Label("[Ship Name]", skin, "medium", Color.WHITE);
        shipName.setPosition(8, 100);
        shipParent.addActor(shipName);

        //ship movement command
        shipMoveCommand = new Label("[movement cmd]", skin, "small", Color.LIGHT_GRAY);
        shipMoveCommand.setPosition(8, 80);
        shipParent.addActor(shipMoveCommand);

        //create the buttons
        shipButtonGroup = new Group();
        shipButtonGroup.setPosition(8, 10);
        shipParent.addActor(shipButtonGroup);

        TextButton moveButton = new TextButton("M", skin, "emphasis");
        moveButton.setBounds(0, 0, 32, 32);
        shipButtonGroup.addActor(moveButton);

        TextButton alignButton = new TextButton("A", skin, "emphasis");
        alignButton.setBounds(40, 0, 32, 32);
        shipButtonGroup.addActor(alignButton);

        TextButton orbitButton = new TextButton("O", skin, "emphasis");
        orbitButton.setBounds(80, 0, 32, 32);
        shipButtonGroup.addActor(orbitButton);

        //listeners for the buttons
        moveButton.addCaptureListener((Event event) -> {
            if(event instanceof ChangeListener.ChangeEvent){

                this.viewportWait = ViewportWait.MOVE;
                game.startViewportListen(this);

                return true;
            }
            return false;
        });
        alignButton.addCaptureListener((Event event) -> {
            if(event instanceof ChangeListener.ChangeEvent){
                this.viewportWait = ViewportWait.ALIGN;
                game.startViewportListen(this);
                return true;
            }
            return false;
        });
        orbitButton.addCaptureListener((Event event) -> {
            if(event instanceof ChangeListener.ChangeEvent){
                this.viewportWait = ViewportWait.ORBIT;
                game.startViewportListen(this);
                return true;
            }
            return false;
        });
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

    void shipSelected(int gridId, int shipId){
        //set the selections
        this.selectedGridId = gridId;
        this.selectedShipId = shipId;

        //update the primary child
        parent.removeActorAt(1, true);
        parent.addActor(shipParent);

        //get the ship
        Ship ship = state.grids[gridId].ships.get(shipId);

        //update the name
        shipName.setText(ship.getType().toString());
        shipName.setColor(state.players.get(ship.owner).color.object);

        //update the movement
        shipMoveCommand.setText(ship.moveCommand);

        //visibility of command buttons
        shipButtonGroup.setVisible(ship.owner == state.myId);
    }

    /**
     * Used while listening for events from the viewport.
     */
    @Override
    void viewportClickEvent(int button, Vector2 screenPos, Vector2 gamePos,
                               SecViewport.ClickType type, int typeId) {

        //losing focus
        if(button == -1){
            viewportWait = ViewportWait.NONE;
        }
        //only take input if it is a left click
        else if(button == Input.Buttons.LEFT){

            //create them request
            MGameRequest req = null;
            if(viewportWait == ViewportWait.MOVE){
                req = new MShipMoveRequest(selectedGridId, selectedShipId, gamePos);
            }
            else if(viewportWait == ViewportWait.ALIGN){

                Ship s = state.grids[selectedGridId].ships.get(selectedShipId);
                req = new MShipAlignRequest(selectedGridId, selectedShipId,
                        (float) Math.atan2(gamePos.y-s.position.y, gamePos.x-s.position.x));
            }

            //send the message to the server
            if(req != null) game.sendGameRequest(req);

            game.startViewportListen(null);
        }
        //release if right click
        else if(button == Input.Buttons.RIGHT){
            game.startViewportListen(null);

        }
    }

    /**
     * Called when new data about the ship has come through.
     */
    void updateShipData(Ship s){
        shipMoveCommand.setText(s.moveCommand);
    }


    /* Enums */

    /**
     * What this sector is listening for from the viewport.
     */
    private enum ViewportWait {
        NONE,
        MOVE,
        ALIGN,
        ORBIT
    }

}
