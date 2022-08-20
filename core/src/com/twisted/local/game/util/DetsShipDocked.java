package com.twisted.local.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.twisted.Asset;
import com.twisted.local.game.SecDetails;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Gem;
import com.twisted.logic.entities.Barge;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.Station;
import com.twisted.logic.entities.attach.StationTransport;
import com.twisted.net.msg.gameReq.MPackedStationMoveReq;

public class DetsShipDocked extends DetsGroup {

    //tree
    private Label shipName, shipGrid, healthLabel;
    private Image healthFill;
    private final Group inventoryGroup;
    private VerticalGroup inventoryShip, inventoryStation; //TODO add listeners to the verticals
    private TextField invNumField;
    private ImageButton transferLeft, transferRight;

    //inventory trees (stored as non-gems, then gems)
    private InventoryHorGroup[] shipInvElements;
    private InventoryHorGroup[] stationInvElements;

    //selection
    private Ship sel;

    //input tracking
    private int invRowSelect = -1;


    /* Construction */

    public DetsShipDocked(SecDetails sector, Skin skin, GlyphLayout glyph, Vector2 size){
        super(sector, skin, glyph, size);

        Group topTextGroup = createTopTextGroup();
        topTextGroup.setPosition(6, 100);
        this.addActor(topTextGroup);

        Group healthGroup = createHealthGroup();
        healthGroup.setPosition(6, 89);
        this.addActor(healthGroup);

        inventoryGroup = createInventoryGroup();
        inventoryGroup.setPosition(6, 6);
        this.addActor(inventoryGroup);
    }

    private Group createTopTextGroup(){
        Group group = new Group();

        shipGrid = new Label("[?]", skin, "medium", Color.WHITE);
        group.addActor(shipGrid);

        shipName = new Label("[Ship Name]", skin, "medium", Color.WHITE);
        shipName.setPosition(30, 0);
        group.addActor(shipName);

        ImageButton undockButton = new ImageButton(Asset.retrieve(Asset.UiButton.UNDOCK));
        undockButton.setBounds(265, -4, 24, 24);
        group.addActor(undockButton);
        undockButton.addCaptureListener((Event event) -> {
            if(event.isHandled()) return true;

            if(event instanceof ChangeListener.ChangeEvent){
                sector.input(sel, SecDetails.Input.SHIP_UNDOCK);
            }

            return true;
        });

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

    private Group createInventoryGroup(){
        Group group = new Group();

        //ship pane
        Image shipBox = new Image(Asset.retrieve(Asset.Shape.PIXEL_GRAY));
        shipBox.setBounds(-1, -1, 102, 77);
        group.addActor(shipBox);
        inventoryShip = new VerticalGroup();
        inventoryShip.top().left();
        inventoryShip.columnAlign(Align.left);
        ScrollPane shipPane = new ScrollPane(inventoryShip, skin);
        shipPane.setBounds(shipBox.getX()+1, shipBox.getY()+1, shipBox.getWidth()-2, shipBox.getHeight()-2);
        shipPane.setScrollingDisabled(true, false);
        shipPane.setScrollbarsVisible(false);
        shipPane.setSmoothScrolling(false);
        shipPane.setColor(Color.BLACK);
        group.addActor(shipPane);

        //station pane
        Image stationBox = new Image(Asset.retrieve(Asset.Shape.PIXEL_GRAY));
        stationBox.setBounds(187, shipBox.getY(), shipBox.getWidth(), shipBox.getHeight());
        group.addActor(stationBox);
        inventoryStation = new VerticalGroup();
        inventoryStation.top().left();
        inventoryStation.columnAlign(Align.left);
        ScrollPane stationPane = new ScrollPane(inventoryStation, skin);
        stationPane.setBounds(stationBox.getX()+1, stationBox.getY()+1, stationBox.getWidth()-2, stationBox.getHeight()-2);
        stationPane.setScrollingDisabled(true, false);
        stationPane.setScrollbarsVisible(false);
        stationPane.setSmoothScrolling(false);
        stationPane.setColor(Color.BLACK);
        group.addActor(stationPane);

        //transfer input text field
        Image invNumBox = new Image(Asset.retrieve(Asset.Shape.PIXEL_GRAY));
        invNumBox.setBounds(shipBox.getX()+shipBox.getWidth()+3, 29,
                stationBox.getX()-shipBox.getWidth()-shipBox.getX()-6, 24);
        group.addActor(invNumBox);
        invNumField = new TextField("", skin);
        TextField.TextFieldStyle numberFieldStyle = new TextField.TextFieldStyle(invNumField.getStyle());
        numberFieldStyle.fontColor = Color.LIGHT_GRAY;
        invNumField.setMaxLength(5);
        invNumField.setStyle(numberFieldStyle);
        invNumField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        invNumField.setBounds(invNumBox.getX()-2, invNumBox.getY()-3,
                invNumBox.getWidth()+4, invNumBox.getHeight()+6); //bounds are weird because of the skin
        invNumField.setColor(Color.BLACK);
        group.addActor(invNumField);

        //transfer buttons
        transferLeft = new ImageButton(Asset.retrieve(Asset.UiButton.TRANSFER_LEFT));
        transferLeft.setX(150-6- transferLeft.getWidth()/2);
        transferLeft.setVisible(false);
        group.addActor(transferLeft);
        transferRight = new ImageButton(Asset.retrieve(Asset.UiButton.TRANSFER_RIGHT));
        transferRight.setX(150-6- transferRight.getWidth()/2);
        transferRight.setVisible(false);
        group.addActor(transferRight);

        //inventory elements
        shipInvElements = new InventoryHorGroup[Barge.weaponSlots.length + Gem.NUMBER_OF_GEMS];
        for(int i=0; i<shipInvElements.length; i++){
            shipInvElements[i] = new InventoryHorGroup(this, shipBox.getWidth()-5, i);
            inventoryShip.addActor(shipInvElements[i]);

            if(i > Barge.weaponSlots.length-1){
                shipInvElements[i].updateName(Gem.orderedGems[i-Barge.weaponSlots.length].name());
            }
        }
        stationInvElements = new InventoryHorGroup[Station.PACKED_STATION_SLOTS + Gem.NUMBER_OF_GEMS];
        for(int i=0; i<stationInvElements.length; i++){
            stationInvElements[i] = new InventoryHorGroup(this, stationBox.getWidth()-5, i);
            inventoryStation.addActor(stationInvElements[i]);

            if(i > Station.PACKED_STATION_SLOTS-1){
                stationInvElements[i].updateName(Gem.orderedGems[i-Station.PACKED_STATION_SLOTS].name());
            }
        }

        //pane listeners
        inventoryShip.addListener(event -> {
            if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.enter){
                sector.scrollFocus(inventoryShip);
            }
            else if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.exit) {
                sector.scrollFocus(null);
            }
            return true;
        });
        inventoryStation.addListener(event -> {
            if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.enter){
                sector.scrollFocus(inventoryStation);
            }
            else if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.exit) {
                sector.scrollFocus(null);
            }
            return true;
        });

        //text field listeners
        invNumField.addListener(event -> {
            invNumField.setCursorPosition(invNumField.getMaxLength());
            return false;
        });

        //button listeners
        transferLeft.addCaptureListener((Event event) -> {
            if(event.isHandled()) return true;
            if(event instanceof ChangeListener.ChangeEvent){
                transferButtonClicked();
            }
            return true;
        });
        transferRight.addCaptureListener((Event event) -> {
            if(event.isHandled()) return true;
            if(event instanceof ChangeListener.ChangeEvent){
                transferButtonClicked();
            }
            return true;
        });

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
        shipName.setText(sel.getType().toString());
        shipName.setColor(state.players.get(sel.owner).getFile().color);

        //update the grid
        shipGrid.setText("[" + state.grids[sel.grid].nickname + "]");

        //inventory prep
        inventoryGroup.setVisible(sel.getType() == Ship.Type.Barge);
    }

    @Override
    public void updateEntity() {
        //update the health
        healthLabel.setText(String.format("%" + (1+2*((int) Math.log10(sel.getMaxHealth())+1)) + "s",
                ((int) Math.ceil(sel.health)) + "/" + sel.getMaxHealth()));
        healthFill.setWidth(200 * sel.health / sel.getMaxHealth());

        //update the inventories
        if(sel.getType() == Ship.Type.Barge){
            //ship inventory
            for(int i=0; i<shipInvElements.length; i++){
                //weapon slots
                if(i < Barge.weaponSlots.length){
                    StationTransport st = (StationTransport) sel.weapons[i];

                    //update
                    if(st.cargo != null) {
                        shipInvElements[i].updateName(st.cargo.name());
                    }
                    else {
                        shipInvElements[i].unload();
                    }
                }
                //gems
                else {
                    shipInvElements[i].unload();
                }
            }
            //station inventory
            Station st = state.grids[sel.grid].station;
            for(int i=0; i<stationInvElements.length; i++){
                //packed slots
                if(i < Station.PACKED_STATION_SLOTS){
                    if(st.packedStations[i] != null){
                        stationInvElements[i].updateName(st.packedStations[i].name());
                    }
                    else {
                        stationInvElements[i].unload();
                    }
                }
                //gems
                else {
                    if(st.resources[i-Station.PACKED_STATION_SLOTS] > 0){
                        stationInvElements[i].updateAmount(st.resources[i-Station.PACKED_STATION_SLOTS]+"");
                    }
                    else {
                        stationInvElements[i].unload();
                    }
                }
            }
        }
    }

    @Override
    public Entity getSelectedEntity() {
        return sel;
    }


    /* Internal Events */

    void inventoryRowClicked(Group inventory, int index){
        //clear the old selection
        if(transferLeft.isVisible()){
            stationInvElements[invRowSelect].resetBackground();
        }
        else if(transferRight.isVisible()){
            shipInvElements[invRowSelect].resetBackground();
        }

        //make the selection
        if(inventory == inventoryShip){
            transferLeft.setVisible(false);
            transferRight.setVisible(true);
        }
        else if(inventory == inventoryStation){
            transferLeft.setVisible(true);
            transferRight.setVisible(false);
        }
        invRowSelect = index;

        //update inputs
        invNumField.setText("1");
    }

    private void transferButtonClicked(){
        int amount = 0;
        if(invNumField.getText().length() > 0){
            amount = Integer.parseInt(invNumField.getText());

            //figure out which resource is wanted
            if(transferLeft.isVisible()){
                //packed
                if(invRowSelect < Station.PACKED_STATION_SLOTS){
                    sector.input(new MPackedStationMoveReq(
                            EntPtr.createFromEntity(state.grids[sel.grid].station),
                            invRowSelect,
                            EntPtr.createFromEntity(sel)));
                }
                //gems
                else {
                    //TODO
                }
            }
            else if(transferRight.isVisible()){
                //packed
                if(invRowSelect < Barge.weaponSlots.length){
                    sector.input(new MPackedStationMoveReq(
                            EntPtr.createFromEntity(sel),
                            invRowSelect,
                            EntPtr.createFromEntity(state.grids[sel.grid].station)));
                }
                //gems
                else {
                    //TODO
                }
            }
        }

        //return input state to starting state
        if(transferLeft.isVisible()){
            stationInvElements[invRowSelect].resetBackground();
            transferLeft.setVisible(false);
        }
        else if(transferRight.isVisible()){
            shipInvElements[invRowSelect].resetBackground();
            transferRight.setVisible(false);
        }
        invNumField.setText("");
        invRowSelect = -1;
    }

}
