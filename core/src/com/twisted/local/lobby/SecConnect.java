package com.twisted.local.lobby;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.twisted.Asset;
import com.twisted.Main;
import com.twisted.local.lib.RectTextButton;
import com.twisted.local.lib.Ribbon;

class SecConnect extends Sector {

    //graphics
    private Group parent, peakGroup, hostGroup, joinGroup, valleyGroup;
    private RectTextButton joinButton, hostButton, connectButton, launchButton, terminateButton;
    private TextField joinAddressField, joinUserField;

    //state
    private String desiredUsername;
    public String getDesiredUsername(){
        return desiredUsername;
    }


    /**
     * Constructor
     */
    SecConnect(Lobby lobby){
        super(lobby);
    }


    /* Standard Graphics */

    @Override
    protected Group init(){
        parent = super.init();
        parent.setPosition(3+SecTerminal.TERMINAL_WIDTH/2f-120-70, 150);

        peakGroup = initPeak();
        peakGroup.setPosition(120, 240);
        parent.addActor(peakGroup);

        hostGroup = initHost();
        hostGroup.setPosition(0, 230);
        hostGroup.setVisible(false);
        parent.addActor(hostGroup);

        joinGroup = initJoin();
        joinGroup.setPosition(0, 230);
        joinGroup.setVisible(false);
        parent.addActor(joinGroup);

        valleyGroup = initValley();
        valleyGroup.setPosition(190, 250);
        valleyGroup.setVisible(false);
        parent.addActor(valleyGroup);

        return parent;
    }
    @Override
    void render(float delta) {

    }
    @Override
    void dispose() {

    }


    /* Graphics Utilities */

    private Group initPeak(){
        Group group = new Group();

        //join as a client
        joinButton = new RectTextButton("Join", Asset.labelStyle(Asset.Avenir.HEAVY_16),
                Asset.retrieve(Asset.Shape.PIXEL_BLACK));
        joinButton.setPadding(30, 24, 2);
        group.addActor(joinButton);

        //create a server
        hostButton = new RectTextButton("Host", Asset.labelStyle(Asset.Avenir.HEAVY_16),
                Asset.retrieve(Asset.Shape.PIXEL_BLACK));
        hostButton.setPadding(30, 24, 2);
        hostButton.setPosition(140, 0);
        group.addActor(hostButton);

        //listeners
        joinButton.setOnLeftClick(() -> {
            peakGroup.setVisible(false);
            hostButton.setDisabled(true);
            joinGroup.setVisible(true);
        });
        hostButton.setOnLeftClick(() -> {
            peakGroup.setVisible(false);
            joinButton.setDisabled(true);
            hostGroup.setVisible(true);
        });

        return group;
    }

    private Group initJoin(){
        Group group = new Group();

        //cancel button
        RectTextButton cancelButton = new RectTextButton("Cancel", Asset.labelStyle(Asset.Avenir.MEDIUM_16),
                Asset.retrieve(Asset.Shape.PIXEL_BLACK));
        cancelButton.setPosition(130, 0);
        cancelButton.setPadding(24, 16, 2);

        //connect button
        connectButton = new RectTextButton("Connect", Asset.labelStyle(Asset.Avenir.MEDIUM_16),
                Asset.retrieve(Asset.Shape.PIXEL_BLACK));
        connectButton.setPosition(250, 0);
        connectButton.setPadding(24, 16, 2);

        //address
        joinAddressField = new TextField("", lobby.textFieldStyle);
        joinAddressField.setBounds(90, 90, 200, 30);
        joinAddressField.setColor(new Color(1.2f*54/255f, 1.2f*56/255f, 1.2f*68/255f, 1));
        joinAddressField.setMessageText("Address");
        Ribbon addressRibbon = new Ribbon(Asset.retrieve(Asset.Shape.PIXEL_DARKPURPLE), 3);
        addressRibbon.setBounds(joinAddressField.getX()-3, joinAddressField.getY()-3,
                joinAddressField.getWidth()+6, joinAddressField.getHeight()+6);

        //username
        joinUserField = new TextField("", lobby.textFieldStyle);
        joinUserField.setBounds(90, 40, 200, 30);
        joinUserField.setColor(new Color(1.2f*54/255f, 1.2f*56/255f, 1.2f*68/255f, 1));
        joinUserField.setMessageText("Username");
        Ribbon userRibbon = new Ribbon(Asset.retrieve(Asset.Shape.PIXEL_DARKPURPLE), 3);
        userRibbon.setBounds(joinUserField.getX()-3, joinUserField.getY()-3,
                joinUserField.getWidth()+6, joinUserField.getHeight()+6);

        //listeners
        connectButton.setOnLeftClick(() -> {
            desiredUsername = joinUserField.getText();
            String[] address = joinAddressField.getText().split(":");

            if(address.length == 2){
                lobby.connectAsClient(address);
                connectButton.setDisabled(true);
            }
        });
        cancelButton.setOnLeftClick(() -> {
            joinGroup.setVisible(false);
            connectButton.setDisabled(false);
            hostButton.setDisabled(false);
            lobby.keyboardFocus(null);
            peakGroup.setVisible(true);

            lobby.cancelClientConnect();
        });

        //add all the actors
        group.addActor(addressRibbon);
        group.addActor(joinAddressField);
        group.addActor(userRibbon);
        group.addActor(joinUserField);
        group.addActor(connectButton);
        group.addActor(cancelButton);

        return group;
    }

    private Group initHost(){
        Group group = new Group();

        //cancel button
        RectTextButton cancelButton = new RectTextButton("Cancel",
                Asset.labelStyle(Asset.Avenir.MEDIUM_16), Asset.retrieve(Asset.Shape.PIXEL_BLACK));
        cancelButton.setPosition(140, 0);
        cancelButton.setPadding(24, 16, 2);

        //connect button
        launchButton = new RectTextButton("Launch", Asset.labelStyle(Asset.Avenir.MEDIUM_16),
                Asset.retrieve(Asset.Shape.PIXEL_BLACK));
        launchButton.setPosition(240, 0);
        launchButton.setPadding(24, 16, 2);

        //address
        TextField userField = new TextField("", lobby.textFieldStyle);
        userField.setBounds(90, 37, 200, 30);
        userField.setColor(new Color(1.2f*54/255f, 1.2f*56/255f, 1.2f*68/255f, 1));
        userField.setMessageText("Username");
        Ribbon userRibbon = new Ribbon(Asset.retrieve(Asset.Shape.PIXEL_DARKPURPLE), 3);
        userRibbon.setBounds(userField.getX()-3, userField.getY()-3,
                userField.getWidth()+6, userField.getHeight()+6);

        //listeners
        launchButton.setOnLeftClick(() -> {
            launchButton.setDisabled(true);

            desiredUsername = userField.getText();
            if(desiredUsername.equals("")){
                desiredUsername = "Host";
            }

            //create the host
            lobby.launchServer();
        });
        cancelButton.setOnLeftClick(() -> {
            hostGroup.setVisible(false);
            joinButton.setDisabled(false);
            launchButton.setDisabled(false);
            lobby.keyboardFocus(null);
            peakGroup.setVisible(true);
        });

        //add actors
        group.addActor(userRibbon);
        group.addActor(userField);
        group.addActor(launchButton);
        group.addActor(cancelButton);

        return group;
    }

    private Group initValley(){
        Group group = new Group();

        terminateButton = new RectTextButton("Terminate", Asset.labelStyle(Asset.Avenir.MEDIUM_16),
                Asset.retrieve(Asset.Shape.PIXEL_BLACK));
        terminateButton.setPadding(24, 16, 2);
        group.addActor(terminateButton);

        //listeners
        terminateButton.setOnLeftClick(() -> lobby.terminateClicked());

        return group;
    }


    /* Events */

    void stopLaunchingServer(){
        launchButton.setDisabled(false);
    }

    void connectedToServer(boolean isHost){
        //host stuff
        joinButton.setDisabled(false);
        launchButton.setDisabled(false);
        hostGroup.setVisible(false);

        //join stuff
        hostButton.setDisabled(false);
        connectButton.setDisabled(false);
        joinGroup.setVisible(false);

        //terminate button
        valleyGroup.setVisible(true);
        if(isHost) terminateButton.setText("Close Server");
        else terminateButton.setText("Disconnect");
    }

    void failedToConnectToServer(){
        connectButton.setDisabled(false);
        lobby.keyboardFocus(joinAddressField);
    }

    void leaveCloseServer(){
        valleyGroup.setVisible(false);
        peakGroup.setVisible(true);
    }

}
