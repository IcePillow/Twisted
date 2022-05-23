package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.twisted.logic.entities.Ship;
import com.twisted.local.game.state.GameState;

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
    private Group parent, shipParent, shipButtonGroup;
    private Label shipName;

    //graphics state
    private int selectedGridId, selectedShipId;


    /**
     * Constructor
     */
    public SecDetails(Game game, Skin skin){
        this.game = game;
        this.skin = skin;
    }

    @Override
    public Group init() {
        parent = new Group();
        parent.setBounds(0, 100, 300, 125);

        //add the main window background
        Image ribbon = new Image(new Texture(Gdx.files.internal("images/pixels/darkpurple.png")));
        ribbon.setSize(parent.getWidth(), parent.getHeight());
        parent.addActor(ribbon);
        Image embedded = new Image(new Texture(Gdx.files.internal("images/pixels/black.png")));
        embedded.setBounds(3, 3, parent.getWidth()-6, parent.getHeight()-6);
        parent.addActor(embedded);

        //create the ship details group
        parent.addActor(initShipSubgroup());

        return parent;
    }

    private Group initShipSubgroup(){

        //initialize the details group
        shipParent = new Group();

        //ship type name
        shipName = new Label("[Ship Name]", skin, "medium", Color.WHITE);
        shipName.setPosition(8, 100);
        shipParent.addActor(shipName);

        //create the buttons
        shipButtonGroup = new Group();
        shipButtonGroup.setPosition(8, 10);
        shipParent.addActor(shipButtonGroup);

        Image moveButton = new Image(new Texture(Gdx.files.internal("images/ui/white-arrow.png")));
        moveButton.setSize(24, 24);
        shipButtonGroup.addActor(moveButton);

        return shipParent;

    }

    @Override
    public void load() {

    }

    @Override
    public void render() {

    }

    @Override
    public void dispose() {

    }


    /* Event Methods */

    void shipSelected(int gridId, int shipId){
        //set the selections
        this.selectedGridId = gridId;
        this.selectedShipId = shipId;

        //get the ship
        Ship ship = state.grids[gridId].ships.get(shipId);

        //update the name
        shipName.setText(ship.getType().toString());
        shipName.setColor(state.players.get(ship.owner).color.object);

        //visibility of command buttons
        shipButtonGroup.setVisible(ship.owner == state.myId);
    }

}
