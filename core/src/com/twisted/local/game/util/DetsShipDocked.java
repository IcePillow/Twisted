package com.twisted.local.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.twisted.Asset;
import com.twisted.Main;
import com.twisted.local.game.SecDetails;
import com.twisted.logic.descriptors.EntPtr;
import com.twisted.logic.descriptors.Gem;
import com.twisted.logic.entities.ship.Barge;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.entities.station.Station;
import com.twisted.logic.entities.attach.StationTrans;
import com.twisted.net.msg.gameReq.MGemMoveReq;
import com.twisted.net.msg.gameReq.MPackedStationMoveReq;

public class DetsShipDocked extends DetsGroup {

    //tree
    private Label shipName, shipGrid, healthLabel, gemStorageLabel, holdSizeLabel;
    private Image healthFill, shipIcon;
    private final Group inventoryGroup;
    private VerticalGroup inventoryShip, inventoryStation;
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

    public DetsShipDocked(SecDetails sector, Skin skin, Vector2 size){
        super(sector, skin, size);

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

        shipIcon = new Image(Asset.retrieveEntityIcon(Ship.Tier.Frigate));
        shipIcon.setColor(Color.GRAY);
        shipIcon.setPosition(0, 2);
        group.addActor(shipIcon);

        shipGrid = new Label("[?]", Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        shipGrid.setPosition(18, 0);
        group.addActor(shipGrid);

        shipName = new Label("[Ship Name]", Asset.labelStyle(Asset.Avenir.HEAVY_16));
        shipName.setPosition(40, 0);
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
        Image shipBox = new Image(Asset.retrieve(Asset.Pixel.GRAY));
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
        Image stationBox = new Image(Asset.retrieve(Asset.Pixel.GRAY));
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
        Image invNumBox = new Image(Asset.retrieve(Asset.Pixel.GRAY));
        invNumBox.setBounds(shipBox.getX()+shipBox.getWidth()+3, 29,
                stationBox.getX()-shipBox.getWidth()-shipBox.getX()-6, 24);
        group.addActor(invNumBox);
        invNumField = new TextField("", new TextField.TextFieldStyle(
                Asset.retrieve(Asset.Avenir.MEDIUM_14), Color.LIGHT_GRAY, null, null,
                Asset.retrieve(Asset.Pixel.BLACK)
        ));
        TextField.TextFieldStyle numberFieldStyle = new TextField.TextFieldStyle(invNumField.getStyle());
        invNumField.setStyle(numberFieldStyle);
        invNumField.setMaxLength(5);
        invNumField.setBlinkTime(0.4f);
        invNumField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        invNumField.setBounds(invNumBox.getX()+1, invNumBox.getY()+1,
                invNumBox.getWidth()-2, invNumBox.getHeight()-2);
        group.addActor(invNumField);

        //storage labels
        gemStorageLabel = new Label("0", Asset.labelStyle(Asset.Avenir.MEDIUM_14));
        gemStorageLabel.setColor(Color.LIGHT_GRAY);
        gemStorageLabel.setPosition(shipBox.getX()+shipBox.getWidth(), 50);
        group.addActor(gemStorageLabel);
        holdSizeLabel = new Label("/", Asset.labelStyle(Asset.Avenir.MEDIUM_14));
        holdSizeLabel.setColor(Color.GRAY);
        holdSizeLabel.setPosition(shipBox.getX()+shipBox.getWidth()+invNumBox.getWidth()/2, 50);
        group.addActor(holdSizeLabel);

        //transfer buttons
        transferLeft = new ImageButton(Asset.retrieve(Asset.UiButton.TRANSFER_LEFT));
        transferLeft.setX(150-6- transferLeft.getWidth()/2);
        transferLeft.setVisible(false);
        group.addActor(transferLeft);
        transferRight = new ImageButton(Asset.retrieve(Asset.UiButton.TRANSFER_RIGHT));
        transferRight.setX(150-6-transferRight.getWidth()/2);
        transferRight.setVisible(false);
        group.addActor(transferRight);

        //inventory elements
        shipInvElements = new InventoryHorGroup[Ship.Model.Heron.weapons.length + Gem.NUM_OF_GEMS];
        for(int i=0; i<shipInvElements.length; i++){
            shipInvElements[i] = new InventoryHorGroup(this, shipBox.getWidth()-5, i);
            inventoryShip.addActor(shipInvElements[i]);

            if(i > Ship.Model.Heron.weapons.length-1){
                shipInvElements[i].updateAll(Gem.orderedGems[i-Ship.Model.Heron.weapons.length].name(), "0");
            }
        }
        stationInvElements = new InventoryHorGroup[Station.PACKED_STATION_SLOTS + Gem.NUM_OF_GEMS];
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

        //update the name, grid, and icon
        shipName.setText(sel.entityModel().toString());
        shipName.setColor(state.players.get(sel.owner).getPaint().col);
        shipGrid.setText("[" + state.grids[sel.grid].nickname + "]");
        shipIcon.setDrawable(Asset.retrieveEntityIcon(sel.model.tier));

        //inventory prep
        inventoryGroup.setVisible(sel.entityModel() == Ship.Model.Heron);

        //hold size
        if(sel.entityModel() == Ship.Model.Heron){
            holdSizeLabel.setText("/" + (int) ((Barge) sel).getHoldSize());
        }
    }

    @Override
    public void updateEntity() {
        //update the health
        healthLabel.setText(String.format("%" + (1+2*((int) Math.log10(sel.model.maxHealth)+1)) + "s",
                ((int) Math.ceil(sel.health)) + "/" + sel.model.maxHealth));
        healthFill.setWidth(200 * sel.health / sel.model.maxHealth);

        //update the inventories
        if(sel.entityModel() == Ship.Model.Heron){
            //ship inventory
            for(int i=0; i<shipInvElements.length; i++){
                //weapon slots
                if(i < Ship.Model.Heron.weapons.length){
                    StationTrans sh = (StationTrans) sel.weapons[i];

                    //update
                    if(sh.cargo != null) {
                        shipInvElements[i].updateName(sh.cargo.name());
                    }
                    else {
                        shipInvElements[i].unload();
                    }
                }
                //gems
                else {
                    Barge sh = (Barge) sel;

                    if(sh.resources[i-sh.model.weapons.length] > 0){
                        shipInvElements[i].updateAll(
                                Gem.orderedGems[i-sh.model.weapons.length].name(),
                                sh.resources[i-sh.model.weapons.length]+"");
                    }
                    else {
                        shipInvElements[i].unload();
                    }
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
                        stationInvElements[i].updateAll(Gem.orderedGems[i-Station.PACKED_STATION_SLOTS].name(),
                                st.resources[i-Station.PACKED_STATION_SLOTS]+"");
                    }
                    else {
                        stationInvElements[i].unload();
                    }
                }
            }

            //gem storage
            gemStorageLabel.setText(Float.toString(Gem.calcVolume(((Barge) sel).resources)));
            Main.glyph.setText(gemStorageLabel.getStyle().font, gemStorageLabel.getText());
            gemStorageLabel.setX(holdSizeLabel.getX() - Main.glyph.width);
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
        int amount = 0;
        if(inventory == inventoryShip){
            transferLeft.setVisible(false);
            transferRight.setVisible(true);

            if(index < Ship.Model.Heron.weapons.length) amount = 1;
            else {
                amount = ((Barge) sel).resources[index - Ship.Model.Heron.weapons.length];
            }

        }
        else if(inventory == inventoryStation){
            transferLeft.setVisible(true);
            transferRight.setVisible(false);

            if(index < Station.PACKED_STATION_SLOTS) amount = 1;
            else {
                amount = (int) Math.min(state.grids[sel.grid].station.resources[index - Station.PACKED_STATION_SLOTS],
                        ((Barge) sel).maxGemsCanFit(Gem.orderedGems[index - Station.PACKED_STATION_SLOTS]));
            }
        }
        invRowSelect = index;

        //update inputs
        sector.keyboardFocus(invNumField);
        invNumField.setText(Integer.toString(amount));
    }

    private void transferButtonClicked(){
        if(invNumField.getText().length() > 0){
            int amount = Integer.parseInt(invNumField.getText());

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
                    sector.input(new MGemMoveReq(
                            EntPtr.createFromEntity(state.grids[sel.grid].station),
                            Gem.orderedGems[invRowSelect-Station.PACKED_STATION_SLOTS],
                            amount,
                            EntPtr.createFromEntity(sel)));
                }
            }
            else if(transferRight.isVisible()){
                //packed
                if(invRowSelect < Ship.Model.Heron.weapons.length){
                    System.out.println(invRowSelect);
                    sector.input(new MPackedStationMoveReq(
                            EntPtr.createFromEntity(sel),
                            invRowSelect,
                            EntPtr.createFromEntity(state.grids[sel.grid].station)));
                }
                //gems
                else {
                    sector.input(new MGemMoveReq(
                            EntPtr.createFromEntity(sel),
                            Gem.orderedGems[invRowSelect-Ship.Model.Heron.weapons.length],
                            amount,
                            EntPtr.createFromEntity(state.grids[sel.grid].station)));
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
