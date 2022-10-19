package com.twisted.local.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
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
    private com.badlogic.gdx.scenes.scene2d.ui.Label shipName, shipGrid, healthLabel, gemStorageLabel, holdSizeLabel;
    private Image healthFill, shipIcon;
    private final Group inventoryGroup;
    private TextField invNumField;
    private ImageButton transferLeft, transferRight;

    //inventory trees (stored as non-gems, then gems)
    private Table[] shipChildrenTables, stationChildrenTables;
    private Label[][] shipInvActors, stationInvActors;

    //selection
    private Ship sel;

    //input tracking
    private boolean selectShNotSt;
    private int selectInvRow;


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

        shipGrid = new com.badlogic.gdx.scenes.scene2d.ui.Label("[?]", Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        shipGrid.setPosition(18, 0);
        group.addActor(shipGrid);

        shipName = new com.badlogic.gdx.scenes.scene2d.ui.Label("[Ship Name]", Asset.labelStyle(Asset.Avenir.HEAVY_16));
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

        healthLabel = new com.badlogic.gdx.scenes.scene2d.ui.Label("[health]", skin, "small",
                new Color(0,0.49f,0,1));
        healthLabel.setPosition(204, -7);
        group.addActor(healthLabel);

        return group;
    }

    private Group createInventoryGroup(){
        Group group = new Group();

        //ship inventory table
        Table shipInvTable = new Table().top().left();
        shipChildrenTables = new Table[3+Gem.NUM_OF_GEMS];
        shipInvActors = new Label[(3+Gem.NUM_OF_GEMS)][2];
        for(int i=0; i<3+Gem.NUM_OF_GEMS; i++){
            //create the name label
            if(i < 3) shipInvActors[i][0] = new Label("[St]", Asset.labelStyle(Asset.Avenir.MEDIUM_12));
            else shipInvActors[i][0] = new Label(Gem.orderedGems[i-3].name(), Asset.labelStyle(Asset.Avenir.MEDIUM_12));
            shipInvActors[i][0].setColor(Color.LIGHT_GRAY);

            //create the amount label
            shipInvActors[i][1] = new Label("0", Asset.labelStyle(Asset.Avenir.MEDIUM_12));
            shipInvActors[i][1].setColor(Color.LIGHT_GRAY);
            shipInvActors[i][1].setAlignment(Align.right);

            //child table
            shipChildrenTables[i] = new Table();
            shipChildrenTables[i].add(shipInvActors[i][0]);
            shipChildrenTables[i].add(shipInvActors[i][1]).growX();

            //add and create the new row
            shipInvTable.add(shipChildrenTables[i]).growX();
            shipInvTable.row();
        }

        //station inventory table
        Table stationInvTable = new Table().top().left();
        stationChildrenTables = new Table[Station.PACKED_STATION_SLOTS+Gem.NUM_OF_GEMS];
        stationInvActors = new Label[(Station.PACKED_STATION_SLOTS+Gem.NUM_OF_GEMS)][2];
        for(int i=0; i<Station.PACKED_STATION_SLOTS+Gem.NUM_OF_GEMS; i++){
            //create the name label
            if(i < 3) stationInvActors[i][0] = new Label("[St]", Asset.labelStyle(Asset.Avenir.MEDIUM_12));
            else stationInvActors[i][0] = new Label(Gem.orderedGems[i-3].name(), Asset.labelStyle(Asset.Avenir.MEDIUM_12));
            stationInvActors[i][0].setColor(Color.LIGHT_GRAY);

            //create the amount label
            stationInvActors[i][1] = new Label("0", Asset.labelStyle(Asset.Avenir.MEDIUM_12));
            stationInvActors[i][1].setColor(Color.LIGHT_GRAY);
            stationInvActors[i][1].setAlignment(Align.right);

            //child table
            stationChildrenTables[i] = new Table();
            stationChildrenTables[i].add(stationInvActors[i][0]);
            stationChildrenTables[i].add(stationInvActors[i][1]).growX();

            //add and create the new row
            stationInvTable.add(stationChildrenTables[i]).growX();
            stationInvTable.row();
        }

        //ship pane
        Image shipBox = new Image(Asset.retrieve(Asset.Pixel.GRAY));
        shipBox.setBounds(-1, -1, 102, 77);
        group.addActor(shipBox);
        ScrollPane shipPane = new ScrollPane(shipInvTable, skin);
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
        ScrollPane stationPane = new ScrollPane(stationInvTable, skin);
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
        Table gemSpaceTable = new Table();
        gemSpaceTable.setBounds(shipBox.getX()+shipBox.getWidth(), 62,
                stationBox.getX()-(shipBox.getX()+shipBox.getWidth()), 0);
        group.addActor(gemSpaceTable);
        gemStorageLabel = new com.badlogic.gdx.scenes.scene2d.ui.Label("0", Asset.labelStyle(Asset.Avenir.MEDIUM_14));
        gemStorageLabel.setColor(Color.LIGHT_GRAY);
        gemSpaceTable.add(gemStorageLabel);
        holdSizeLabel = new com.badlogic.gdx.scenes.scene2d.ui.Label("/", Asset.labelStyle(Asset.Avenir.MEDIUM_14));
        holdSizeLabel.setColor(Color.GRAY);
        gemSpaceTable.add(holdSizeLabel);

        //transfer buttons
        transferLeft = new ImageButton(Asset.retrieve(Asset.UiButton.TRANSFER_LEFT));
        transferLeft.setX(150-6- transferLeft.getWidth()/2);
        transferLeft.setVisible(false);
        group.addActor(transferLeft);
        transferRight = new ImageButton(Asset.retrieve(Asset.UiButton.TRANSFER_RIGHT));
        transferRight.setX(150-6-transferRight.getWidth()/2);
        transferRight.setVisible(false);
        group.addActor(transferRight);

        //initial selection values
        selectShNotSt = false;
        selectInvRow = -1;

        //inventory element listeners
        for(int i=0; i<shipChildrenTables.length; i++){
            int iSave = i;
            shipChildrenTables[i].addListener(new ClickListener(Input.Buttons.LEFT){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if(event.isHandled()) return;
                    inventoryRowClicked(true, iSave);
                    event.handle();
                }
            });
        }
        for(int i = 0; i< stationInvActors.length; i++){
            int iSave = i;
            stationChildrenTables[i].addListener(new ClickListener(Input.Buttons.LEFT){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if(event.isHandled()) return;
                    inventoryRowClicked(false, iSave);
                    event.handle();
                }
            });
        }

        //pane listeners
        shipPane.addListener(event -> {
            if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.enter){
                sector.scrollFocus(shipPane);
            }
            else if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.exit) {
                sector.scrollFocus(null);
            }
            return true;
        });
        stationPane.addListener(event -> {
            if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.enter){
                sector.scrollFocus(stationPane);
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

        //reset selections
        selectInvRow = -1;
        selectShNotSt = false;

        //hold size
        if(sel.entityModel().getTier() == Ship.Tier.Barge){
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
        if(sel.entityModel().getTier() == Ship.Tier.Barge){
            //ship inventory
            Barge sh = (Barge) sel;
            for(int i=0; i<shipInvActors.length; i++){
                //packed stations
                if(i < 3){
                    if(sh.weapons.length > i && sh.weapons[i] instanceof StationTrans &&
                            ((StationTrans) sh.weapons[i]).cargo != null){
                        shipInvActors[i][0].setText(((StationTrans) sh.weapons[i]).cargo.name());

                        shipChildrenTables[i].getCells().get(0).setActor(shipInvActors[i][0]);
                    }
                    else {
                        shipChildrenTables[i].getCells().get(0).clearActor();
                        shipChildrenTables[i].getCells().get(1).clearActor();
                    }
                }
                //gems
                else {
                    if(sh.resources[i-3] > 0){
                        shipInvActors[i][1].setText(sh.resources[i-3]);

                        shipChildrenTables[i].getCells().get(0).setActor(shipInvActors[i][0]);
                        shipChildrenTables[i].getCells().get(1).setActor(shipInvActors[i][1]);
                    }
                    else {
                        shipChildrenTables[i].getCells().get(0).clearActor();
                        shipChildrenTables[i].getCells().get(1).clearActor();
                    }
                }
            }
            //station inventory
            Station st = state.grids[sel.grid].station;
            for(int i=0; i<stationInvActors.length; i++){
                //packed stations
                if(i < 3){
                    if(st.packedStations.length > i && st.packedStations[i] != null){
                        stationInvActors[i][0].setText(st.packedStations[i].name());

                        stationChildrenTables[i].getCells().get(0).setActor(stationInvActors[i][0]);
                    }
                    else {
                        stationChildrenTables[i].getCells().get(0).clearActor();
                        stationChildrenTables[i].getCells().get(1).clearActor();
                    }
                }
                //gems
                else {
                    if(st.resources[i-3] > 0){
                        stationInvActors[i][1].setText(st.resources[i-3]);

                        stationChildrenTables[i].getCells().get(0).setActor(stationInvActors[i][0]);
                        stationChildrenTables[i].getCells().get(1).setActor(stationInvActors[i][1]);
                    }
                    else {
                        stationChildrenTables[i].getCells().get(0).clearActor();
                        stationChildrenTables[i].getCells().get(1).clearActor();
                    }
                }
            }

            //gem storage
            gemStorageLabel.setText(Float.toString(Gem.calcVolume(((Barge) sel).resources)));
        }
    }
    @Override
    public Entity getSelectedEntity() {
        return sel;
    }


    /* Internal Events */

    void inventoryRowClicked(boolean shipNotStation, int row){
        //remove the background of the previous selection
        if(selectInvRow >= 0){
            shipChildrenTables[selectInvRow].setBackground((Drawable) null);
            stationChildrenTables[selectInvRow].setBackground((Drawable) null);
        }

        //update the selection
        selectShNotSt = shipNotStation;
        selectInvRow = row;

        //update the background of the new selection
        if(selectShNotSt){
            shipChildrenTables[row].setBackground(Asset.retrieve(Asset.Pixel.GRAY));
        }
        else {
            stationChildrenTables[row].setBackground(Asset.retrieve(Asset.Pixel.GRAY));
        }

        //transfer buttons
        int amount;
        if(selectShNotSt){
            transferLeft.setVisible(false);
            transferRight.setVisible(true);

            //set the amount
            if(row < 3) amount = 1;
            else amount = ((Barge) sel).resources[row - 3];
        }
        else {
            transferLeft.setVisible(true);
            transferRight.setVisible(false);

            //set the amount
            if(row < Station.PACKED_STATION_SLOTS){
                amount = 1;
            }
            else {
                amount = (int) Math.min(state.grids[sel.grid].station.resources[row - Station.PACKED_STATION_SLOTS],
                        ((Barge) sel).maxGemsCanFit(Gem.orderedGems[row - Station.PACKED_STATION_SLOTS]));
            }
        }

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
                if(selectInvRow < Station.PACKED_STATION_SLOTS){
                    sector.input(new MPackedStationMoveReq(
                            EntPtr.createFromEntity(state.grids[sel.grid].station),
                            selectInvRow,
                            EntPtr.createFromEntity(sel)));
                }
                //gems
                else {
                    sector.input(new MGemMoveReq(
                            EntPtr.createFromEntity(state.grids[sel.grid].station),
                            Gem.orderedGems[selectInvRow-Station.PACKED_STATION_SLOTS],
                            amount,
                            EntPtr.createFromEntity(sel)));
                }
            }
            else if(transferRight.isVisible()){
                //packed
                if(selectInvRow < Ship.Model.Heron.weapons.length){
                    sector.input(new MPackedStationMoveReq(
                            EntPtr.createFromEntity(sel),
                            selectInvRow,
                            EntPtr.createFromEntity(state.grids[sel.grid].station)));
                }
                //gems
                else {
                    sector.input(new MGemMoveReq(
                            EntPtr.createFromEntity(sel),
                            Gem.orderedGems[selectInvRow-3],
                            amount,
                            EntPtr.createFromEntity(state.grids[sel.grid].station)));
                }
            }
        }

        //return input state to starting state
        if(transferLeft.isVisible()){
            stationChildrenTables[selectInvRow].setBackground((Drawable) null);
            transferLeft.setVisible(false);
        }
        else if(transferRight.isVisible()){
            shipChildrenTables[selectInvRow].setBackground((Drawable) null);
            transferRight.setVisible(false);
        }

        invNumField.setText("");
        selectInvRow = -1;
    }

}
