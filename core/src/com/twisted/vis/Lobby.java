package com.twisted.vis;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.twisted.Main;
import com.twisted.logic.LobbyHost;
import com.twisted.net.client.Client;
import com.twisted.net.client.ClientContact;
import com.twisted.net.msg.*;

import java.util.Arrays;

/**
 * The Lobby Screen.
 *
 * Stage
    decorationGroup
    initialGroup
        clientGroup
        serverGroup
    terminalGroup
 */
public class Lobby implements Screen, ClientContact {

    //exterior references
    private final Main main;
    private ClientsideContact contact;

    //graphics
    private Stage stage;
    private Skin skin;
    private Group initialGroup, clientGroup, serverGroup, terminalGroup;
    private TextButton joinButton, hostButton, terminateButton; //large buttons outside the groups
    private TextButton connectButton, launchButton; //buttons inside the groups
    private Label attemptingJoin, attemptingLaunch, disconnectedLabel;
    private Table terminalWidget; //terminal display
    private ScrollPane terminalPane;

    //styles
    private Label.LabelStyle terminalLabelStyle;
    private TextField.TextFieldStyle terminalTextFieldStyle;

    //graphics details
    private final int TERMINAL_WIDTH = 480;

    //user details
    private String desiredUsername;


    /* Constructor */

    public Lobby(Main main){
        this.main = main;

        loadGui();
    }


    /* Screen Methods */

    @Override
    public void show() {
    }
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override
    public void pause() {
    }
    @Override
    public void resume() {
    }
    @Override
    public void hide() {
    }
    @Override
    public void dispose() {
        skin.dispose();
    }


    /* External Client Methods */

    @Override
    public void connectedToServer() {
        //prepare the terminal group
        terminateButton.setText("Leave");
        terminateButton.setVisible(true);

        //fix the previous group
        attemptingJoin.setVisible(false);
        hostButton.setDisabled(false);
        connectButton.setDisabled(false);
        clientGroup.setVisible(false);
        initialGroup.setVisible(false);

        //make terminal group visible
        terminalGroup.setVisible(true);

        //name change
        contact.send(new MNameChange(desiredUsername));
    }

    @Override
    public void failedToConnect() {
        attemptingJoin.setVisible(false);
        connectButton.setDisabled(false);
    }

    @Override
    public void clientReceived(Message message) {

        if(message instanceof MChat){
            addToTerminal(((MChat) message).string);
        }
        else if(message instanceof MSceneChange){
            if(((MSceneChange) message).getChange() == MSceneChange.Change.GAME){
                //create game
                Gdx.app.postRunnable(() -> {
                        //create the new game
                        Game game = new Game(main);

                        //set the contact for the game, differently for host and not host
                        if(contact instanceof LobbyHost) {
                            game.setContact(((LobbyHost) contact).getGameHost());
                            ((LobbyHost) contact).getGameHost().getLocalClient().setContact(game);
                        }
                        else {
                            game.setContact(contact);
                            ((Client) contact).setContact(game);
                        }

                        //change screen
                        main.setScreen(game);
                        this.dispose();
                    }
                );
            }
            else {
                System.out.println("[Error] Unexpected scene change.");
                System.out.println(Arrays.toString(new Exception().getStackTrace()));
            }
        }

    }

    @Override
    public void kickedFromServer(Message message) {
        //visual groups
        terminalGroup.setVisible(false);
        initialGroup.setVisible(true);

        //update terminal
        addToTerminal("> You were kicked from the server.");

        //tell user what happened
        disconnectedLabel.setText("You were kicked from the server.");
        disconnectedLabel.setPosition(600, 200);
        disconnectedLabel.setVisible(true);
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            disconnectedLabel.setVisible(false);
        }).start();

        //logical stuff
        contact.shutdown();
        contact = null;
    }

    @Override
    public void lostConnectionToServer() {
        //visual groups
        terminalGroup.setVisible(false);
        initialGroup.setVisible(true);

        //update terminal
        addToTerminal("> You lost connection with the server.");

        //tell user what happened
        disconnectedLabel.setText("Lost connection to the server.");
        disconnectedLabel.setPosition(600, 200);
        disconnectedLabel.setVisible(true);
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            disconnectedLabel.setVisible(false);
        }).start();

        //logical stuff
        contact.shutdown();
        contact = null;
    }


    /* External Server Methods */

    /**
     * Called by LobbyHost when hosting has begun.
     */
    public void serverLaunched(){
        //prepare the terminal group
        terminateButton.setText("Close");
        terminateButton.setVisible(true);

        //fix the previous group
        attemptingLaunch.setVisible(false);
        joinButton.setDisabled(false);
        launchButton.setDisabled(false);
        serverGroup.setVisible(false);
        initialGroup.setVisible(false);

        //make the terminal group visible
        terminalGroup.setVisible(true);
    }

    /**
     * Called by LobbyHost if creating and starting a server fails.
     */
    public void serverLaunchFailed(){
        attemptingLaunch.setVisible(false);
        launchButton.setDisabled(false);
    }


    /* Internal Events */

    /**
     * Called when the user presses return while in the terminal text field.
     */
    private void terminalInput(String string){

        //check it's not empty
        if(string.equals("")) return;

        //commands
        if(string.charAt(0) == '/' && string.length() > 1){

            MCommand command = new MCommand(string.substring(1).split(" "));

            contact.send(command);
        }
        //basic chats
        else if(string.charAt(0) != '/'){
            //create the chat
            MChat chat = new MChat(0, string);

            //send the chat
            contact.send(chat);
        }

    }

    /**
     * Utility method to add a string to the terminal output.
     */
    public void addToTerminal(String string){

        //create the label
        Label label = new Label(string, skin, "small");
        label.setWrap(true);
        label.setStyle(terminalLabelStyle);
        label.setFontScale(0.7f);

        //add the label to the widget
        terminalWidget.row();
        terminalWidget.add(label).width(TERMINAL_WIDTH);

        //had to do thread for some reason
        new Thread(() -> {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            terminalPane.setScrollPercentY(1);

        }).start();

    }


    /* Loading the Stage */

    private void loadGui(){

        //load the skin
        skin = new Skin(Gdx.files.internal("skins/sgx/skin/sgx-ui.json"));

        //create styles from skin
        createStyles();

        //create the stage
        stage = new Stage(new FitViewport(Main.WIDTH, Main.HEIGHT));
        Gdx.input.setInputProcessor(stage);

        //set the background
        Image image = new Image(new Texture(Gdx.files.internal("images/pixels/navy.png")));
        image.setBounds(0, 0, Main.WIDTH, Main.HEIGHT);
        stage.addActor(image);

        //groups
        Group decorationGroup = createDecoration();
        initialGroup = createInitialGui();
        terminalGroup = createTerminalGui();
        terminalGroup.setVisible(false);

        //groups
        stage.addActor(decorationGroup);
        stage.addActor(initialGroup);
        stage.addActor(terminalGroup);
    }

    /**
     * Creates alternate styles for use later.
     */
    private void createStyles(){
        Label label = new Label("", skin);
        terminalLabelStyle = new Label.LabelStyle(label.getStyle());
        terminalLabelStyle.fontColor = new Color(0.9f, 0.9f, 0.9f, 1);

        TextField textField = new TextField("", skin);
        terminalTextFieldStyle = new TextField.TextFieldStyle(textField.getStyle());
        terminalTextFieldStyle.fontColor = new Color(0.9f, 0.9f, 0.9f, 1);
    }

    /**
     * Modularity method.
     */
    private Group createDecoration(){
        Group group = new Group();

        //title text
        Label titleText = new Label("TWISTED", skin, "title");
        titleText.setPosition(-titleText.getWidth()/2 + 720, 600);
        group.addActor(titleText);

        return group;
    }

    /**
     * Modularity method that takes care of the client, server, and title text.
     */
    private Group createInitialGui(){

        Group group = new Group();

        //join as a client
        joinButton = new TextButton("Join", skin, "emphasis");
        joinButton.setBounds(550, 500, 100, 50);
        group.addActor(joinButton);

        clientGroup = createJoinGui();
        group.addActor(clientGroup);
        clientGroup.setVisible(false);

        //create a server
        hostButton = new TextButton("Host", skin, "emphasis");
        hostButton.setBounds(790, 500, 100, 50);
        group.addActor(hostButton);

        serverGroup = createHostGui();
        group.addActor(serverGroup);
        serverGroup.setVisible(false);

        //disconnected label, not shown initially
        disconnectedLabel = new Label("[???]", skin);
        disconnectedLabel.setPosition(-disconnectedLabel.getWidth()/2 + 720, 200);
        disconnectedLabel.setVisible(false);
        group.addActor(disconnectedLabel);

        //listeners
        joinButton.addCaptureListener((Event event) -> {
            if(event instanceof ChangeListener.ChangeEvent){
                if(!hostButton.isDisabled()){
                    hostButton.setDisabled(true);
                    clientGroup.setVisible(true);
                }
                return true;
            }
            return false;
        });
        hostButton.addCaptureListener((Event event) -> {
            if(event instanceof ChangeListener.ChangeEvent){
                if(!joinButton.isDisabled()){
                    joinButton.setDisabled(true);
                    serverGroup.setVisible(true);
                }
                return true;
            }
            return false;
        });

        //return
        return group;
    }

    /**
     * Modularity method that creates and returns the group responsible for the client selection.
     */
    private Group createJoinGui(){
        Group group = new Group();

        //address
        TextArea addressArea = new TextArea("", skin);
        addressArea.setBounds(650, 442, 200, 36);
        addressArea.setAlignment(Align.bottom);
        Label addressLabel = new Label("Address", skin, "small");
        addressLabel.setPosition(650-addressLabel.getWidth(), 450);

        //username
        TextArea userArea = new TextArea("", skin);
        userArea.setBounds(650, 392, 200, 36);
        userArea.setAlignment(Align.bottom);
        Label userLabel = new Label("User", skin, "small");
        userLabel.setPosition(650-userLabel.getWidth(), 400);

        //connect button
        connectButton = new TextButton("Connect", skin, "small");
        connectButton.setBounds(720-45+55, 335, 90, 40);

        //cancel button
        TextButton cancelButton = new TextButton("Cancel", skin, "small");
        cancelButton.setBounds(720-45-55, 335, 90, 40);

        //attempting label
        attemptingJoin = new Label("Attempting to join...", skin, "small");
        attemptingJoin.setPosition(630, 300);
        attemptingJoin.setVisible(false);

        //listeners
        connectButton.addCaptureListener((Event event) -> {
            if(event instanceof ChangeListener.ChangeEvent){
                desiredUsername = userArea.getText();
                String[] address = addressArea.getText().split(":");

                if(address.length == 2){
                    contact = new Client(this, address[0], Integer.parseInt(address[1]));
                    attemptingJoin.setVisible(true);
                    connectButton.setDisabled(true);
                }
                return true;
            }
            return false;
        });
        cancelButton.addCaptureListener((Event event) -> {
            if(event instanceof ChangeListener.ChangeEvent){
                clientGroup.setVisible(false);
                attemptingJoin.setVisible(false);
                connectButton.setDisabled(false);
                hostButton.setDisabled(false);

                contact.shutdown();
                contact = null;

                return true;
            }
            return false;
        });

        //add all the actors
        group.addActor(addressLabel);
        group.addActor(addressArea);
        group.addActor(userLabel);
        group.addActor(userArea);
        group.addActor(connectButton);
        group.addActor(cancelButton);
        group.addActor(attemptingJoin);

        return group;
    }

    /**
     * Modularity method that creates and returns the group responsible for the server selection.
     */
    private Group createHostGui(){
        Group group = new Group();

        //address
        TextArea userArea = new TextArea("", skin);
        userArea.setBounds(650, 442, 200, 36);
        userArea.setAlignment(Align.bottom);
        Label userLabel = new Label("User", skin, "small");
        userLabel.setPosition(650-userLabel.getWidth(), 450);

        //connect button
        launchButton = new TextButton("Launch", skin, "small");
        launchButton.setBounds(720-45+55, 385, 90, 40);

        //cancel button
        TextButton cancelButton = new TextButton("Cancel", skin, "small");
        cancelButton.setBounds(720-45-55, 385, 90, 40);

        //attempting label
        attemptingLaunch = new Label("Launching...", skin, "small");
        attemptingLaunch.setPosition(680, 300);
        attemptingLaunch.setVisible(false);

        //listeners
        launchButton.addCaptureListener((Event event) -> {
            if(event instanceof ChangeListener.ChangeEvent){

                attemptingLaunch.setVisible(true);
                launchButton.setDisabled(true);

                if(userArea.getText().length() > 0){
                    contact = new LobbyHost(this, userArea.getText());
                }
                else {
                    contact = new LobbyHost(this, "Host");
                }

                return true;
            }
            return false;
        });
        cancelButton.addCaptureListener((Event event) -> {
            if(event instanceof ChangeListener.ChangeEvent){
                serverGroup.setVisible(false);
                joinButton.setDisabled(false);
                attemptingLaunch.setVisible(false);
                launchButton.setDisabled(false);

                return true;
            }
            return false;
        });

        //add actors
        group.addActor(userArea);
        group.addActor(userLabel);
        group.addActor(launchButton);
        group.addActor(cancelButton);
        group.addActor(attemptingLaunch);

        return group;
    }

    /**
     * Modularity method that creates and returns the group responsible for the terminal and
     * leave or close button.
     */
    private Group createTerminalGui(){
        Group group = new Group();

        //close the server button
        terminateButton = new TextButton("", skin, "emphasis");
        terminateButton.setBounds(670, 500, 100, 50);

        //create the widget
        terminalWidget = new Table();
        terminalWidget.left().bottom();

        //create the pane
        terminalPane = new ScrollPane(terminalWidget, skin);
        terminalPane.setBounds(720-TERMINAL_WIDTH/2f, 175, TERMINAL_WIDTH, 300);
        terminalPane.setScrollingDisabled(true, false);
        terminalPane.setFadeScrollBars(false);

        //text field input
        TextField textField = new TextField("", terminalTextFieldStyle);
        textField.setBounds(720-TERMINAL_WIDTH/2f-4, 135+6, TERMINAL_WIDTH+8, 40);
        textField.setColor(new Color(1.2f*54/255f, 1.2f*56/255f, 1.2f*68/255f, 1));

        //listeners
        terminateButton.addCaptureListener((Event event) -> {
            if(event instanceof ChangeListener.ChangeEvent) {

                //if you were hosting
                if(contact instanceof LobbyHost){
                    addToTerminal("> You closed the server.");
                    addToTerminal(" ");
                    addToTerminal(" ");
                }
                //if you were just a client
                else {
                    addToTerminal("> You left the server.");
                    addToTerminal(" ");
                    addToTerminal(" ");
                    contact.send(new MDisconnect());
                }

                //shutdown
                contact.shutdown();
                contact = null;

                textField.setText("");
                terminalGroup.setVisible(false);
                initialGroup.setVisible(true);

                return true;
            }
            return false;
        });
        textField.addCaptureListener((Event event) -> {
            if(event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.keyUp){
                if((((InputEvent) event).getKeyCode()) == 66){
                    terminalInput(textField.getText());
                    textField.setText("");
                }
            }
            return false;
        });

        //add everything to the group
        group.addActor(terminateButton);
        group.addActor(terminalPane);
        group.addActor(textField);
        return group;
    }
}
