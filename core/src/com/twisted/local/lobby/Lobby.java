package com.twisted.local.lobby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.twisted.Asset;
import com.twisted.Main;
import com.twisted.local.lib.RectTextButton;
import com.twisted.logic.host.lobby.LobbyHost;
import com.twisted.net.client.Client;
import com.twisted.net.client.ClientContact;
import com.twisted.net.msg.*;
import com.twisted.net.msg.remaining.MSceneChange;
import com.twisted.net.msg.remaining.MChat;
import com.twisted.net.msg.remaining.MCommand;
import com.twisted.local.game.Game;

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
    private Client client;
    private LobbyHost host;

    //graphics
    private Stage stage;
    private Skin skin;
    private OrthographicCamera camera;
    private ShapeRenderer shape;

    //tree
    private Group initialGroup, clientGroup, serverGroup, terminalGroup;
    private RectTextButton joinButton, hostButton, terminateButton; //buttons outside groups
    private RectTextButton connectButton, launchButton; //buttons inside the groups
    private Label attemptingJoin, attemptingLaunch, disconnectedLabel;
    private Table terminalWidget; //terminal display
    private ScrollPane pane;

    //styles
    private Label.LabelStyle terminalLabelStyle;
    private TextField.TextFieldStyle textFieldStyle;

    //rendering
    private float[][] stars;

    //graphics details
    private final int TERMINAL_WIDTH = 480;

    //user details
    private String desiredUsername;


    /* Constructor */

    public Lobby(Main main){
        this.main = main;

        loadGui();
        loadRender();
    }


    /* Screen Methods */

    @Override
    public void show() {
    }
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //rendering
        camera.update();
        shape.setProjectionMatrix(camera.combined);
        frameRender(delta);

        //scene2d
        Main.glyph.reset();
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
        stage.dispose();
    }


    /* External Client Methods */

    @Override
    public void connectedToServer() {
        //if hosting
        if(host != null) {
            //prepare the terminal group
            terminateButton.setText("Close");
            terminateButton.setVisible(true);

            //fix the previous group
            attemptingLaunch.setVisible(false);
            joinButton.setDisabled(false);
            launchButton.setDisabled(false);
            serverGroup.setVisible(false);

            //chat
            addToTerminal(MChat.Type.LOGISTICAL, "> You started hosting a server on port " + host.getPort());
        }
        //if just a client
        else {
            //prepare the terminal group
            terminateButton.setText("Leave");
            terminateButton.setVisible(true);

            //fix the previous group
            attemptingJoin.setVisible(false);
            hostButton.setDisabled(false);
            connectButton.setDisabled(false);
            clientGroup.setVisible(false);
        }

        //switch which high level group is visible
        initialGroup.setVisible(false);
        terminalGroup.setVisible(true);

        //scroll the terminal
        pane.setScrollPercentY(1);

        //name change
        client.send(new MCommand(new String[]{"name", desiredUsername}));
    }

    @Override
    public void failedToConnect() {
        attemptingJoin.setVisible(false);
        connectButton.setDisabled(false);

        disconnectedLabel.setText("Failed to connect");
        disconnectedLabel.setVisible(true);
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            disconnectedLabel.setVisible(false);
        }).start();
    }

    @Override
    public void clientReceived(Message message) {
        if(message instanceof MChat){
            addToTerminal(((MChat) message).type, ((MChat) message).string);
        }
        else if(message instanceof MSceneChange){
            if(((MSceneChange) message).getChange() == MSceneChange.Change.GAME){
                //create game
                Gdx.app.postRunnable(() -> {
                    //create the new game
                    Game game = new Game(main);

                    //set the client for the game, set the contact for the client
                    game.setClient(client);
                    client.setContact(game);

                    //set the host
                    if(host != null) game.setHost(host.getGameHost());

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
    public void disconnected(String reason){
        //visual groups
        terminalGroup.setVisible(false);
        initialGroup.setVisible(true);

        //tell user what happened
        disconnectedLabel.setText("Disconnected: " + reason);
        disconnectedLabel.setPosition(600, 200);
        disconnectedLabel.setVisible(true);
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            disconnectedLabel.setVisible(false);
        }).start();

        //logical stuff
        if(client != null) client.shutdown();
        client = null;
    }

    @Override
    public void lostConnection() {
        //visual groups
        terminalGroup.setVisible(false);
        initialGroup.setVisible(true);

        //update terminal
        addToTerminal(MChat.Type.WARNING_ERROR,"> You lost connection with the server.");

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
        client.shutdown();
        client = null;
    }


    /* External Server Methods */

    /**
     * Called by LobbyHost when hosting has begun.
     */
    public void serverLaunched(int port){
        client = new Client(this, "localhost", port);
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

            client.send(command);
        }
        //basic chats
        else if(string.charAt(0) != '/'){
            //create the chat
            MChat chat = new MChat(MChat.Type.PLAYER_CHAT, string);

            //send the chat
            client.send(chat);
        }

    }

    /**
     * Utility method to add a string to the terminal output.
     */
    public void addToTerminal(MChat.Type type, String string){

        //create the label
        Label label = new Label(string, skin, "small");
        label.setWrap(true);
        label.setStyle(terminalLabelStyle);
        label.setFontScale(0.7f);

        //set label color
        switch(type){
            case LOGISTICAL:
                label.setColor(Color.GRAY);
                break;
            case WARNING_ERROR:
                label.setColor(1, 0.5f, 0.5f, 1);
                break;
        }

        //add the label to the widget
        terminalWidget.row();
        terminalWidget.add(label).width(TERMINAL_WIDTH);

        //had to do thread for some reason
        new Thread(() -> {
            try {
                Thread.sleep(20);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            pane.setScrollPercentY(1);

        }).start();

    }


    /* Rendering */

    private void loadRender(){
        //create objects
        camera = new OrthographicCamera(stage.getWidth(), stage.getHeight());
        camera.translate(stage.getWidth()/2, stage.getHeight()/2);
        shape = new ShapeRenderer();

        //prepare stars
        stars = new float[150][4];
        for(float[] s : stars){
            s[0] = (float) (Math.random()) * Main.WIDTH;
            s[1] = (float) (Math.random()) * Main.HEIGHT;
            s[2] = (float) Math.floor((Math.random()) * 1.99f) + 1;
            s[3] = (float) (Math.random()*0.4f + 0.2f);
        }
    }

    private void frameRender(float delta){
        shape.begin(ShapeRenderer.ShapeType.Filled);

        //draw background
        shape.setColor(Main.SPACE);
        shape.rect(0, 0, stage.getWidth(), stage.getHeight());

        for(float[] s : stars){
            shape.setColor(s[3], s[3], s[3], 1f);
            shape.circle(s[0], s[1], s[2]);
        }

        shape.end();
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
        textFieldStyle = new TextField.TextFieldStyle(textField.getStyle());
        textFieldStyle.fontColor = new Color(0.9f, 0.9f, 0.9f, 1);
        textFieldStyle.background = Asset.retrieve(Asset.Shape.PIXEL_BLACK);
        textFieldStyle.cursor = Asset.retrieve(Asset.UiBasic.CURSOR_1);
    }

    /**
     * Modularity method.
     */
    private Group createDecoration(){
        Group group = new Group();

        //title text
        Label titleText = new Label("TWISTED", skin, "title", Color.WHITE);
        titleText.setColor(Color.LIGHT_GRAY);
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
        joinButton = new RectTextButton("Join", skin, "medium");
        joinButton.setPadding(30, 24, 2);
        joinButton.setPosition(600, 525);
        group.addActor(joinButton);

        clientGroup = createJoinGui();
        group.addActor(clientGroup);
        clientGroup.setVisible(false);

        //create a server
        hostButton = new RectTextButton("Host", skin, "medium");
        hostButton.setPadding(30, 24, 2);
        hostButton.setPosition(840, 525);
        group.addActor(hostButton);

        serverGroup = createHostGui();
        group.addActor(serverGroup);
        serverGroup.setVisible(false);

        //disconnected label, not shown initially
        disconnectedLabel = new Label("[???]", skin, "small", Color.WHITE);
        disconnectedLabel.setColor(Color.LIGHT_GRAY);
        disconnectedLabel.setPosition(640, 300);
        disconnectedLabel.setVisible(false);
        group.addActor(disconnectedLabel);

        //listeners
        joinButton.setOnLeftClick(() -> {
            hostButton.setDisabled(true);
            clientGroup.setVisible(true);
        });
        hostButton.setOnLeftClick(() -> {
            joinButton.setDisabled(true);
            serverGroup.setVisible(true);
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
        TextField addressField = new TextField("", skin);
        addressField.setStyle(textFieldStyle);
        addressField.setBounds(650, 442, 200, 30);
        addressField.setColor(new Color(1.2f*54/255f, 1.2f*56/255f, 1.2f*68/255f, 1));
        Image addressDecor = new Image(Asset.retrieve(Asset.Shape.PIXEL_DARKPURPLE));
        addressDecor.setBounds(addressField.getX()-3, addressField.getY()-3,
                addressField.getWidth()+6, addressField.getHeight()+6);
        Label addressLabel = new Label("Address", skin, "small", Color.WHITE);
        addressLabel.setColor(Color.GRAY);
        addressLabel.setPosition(650-addressLabel.getWidth()-6, addressField.getY()+3);

        //username
        TextField userField = new TextField("", skin);
        userField.setStyle(textFieldStyle);
        userField.setBounds(650, 392, 200, 30);
        userField.setColor(new Color(1.2f*54/255f, 1.2f*56/255f, 1.2f*68/255f, 1));
        Image userDecor = new Image(Asset.retrieve(Asset.Shape.PIXEL_DARKPURPLE));
        userDecor.setBounds(userField.getX()-3, userField.getY()-3,
                userField.getWidth()+6, userField.getHeight()+6);
        Label userLabel = new Label("User", skin, "small", Color.WHITE);
        userLabel.setColor(Color.GRAY);
        userLabel.setPosition(650-userLabel.getWidth()-6, userField.getY()+3);

        //connect button
        connectButton = new RectTextButton("Connect", skin, "small");
        connectButton.setPosition(720+55, 355);
        connectButton.setPadding(24, 16, 2);

        //cancel button
        RectTextButton cancelButton = new RectTextButton("Cancel", skin, "small");
        cancelButton.setPosition(720-55, 355);
        cancelButton.setPadding(24, 16, 2);

        //attempting label
        attemptingJoin = new Label("Attempting to connect", skin, "small");
        attemptingJoin.setPosition(640, 300);
        attemptingJoin.setVisible(false);

        //listeners
        Lobby thisSave = this;
        connectButton.setOnLeftClick(() -> {
            desiredUsername = userField.getText();
            String[] address = addressField.getText().split(":");

            if(address.length == 2){
                client = new Client(thisSave, address[0], Integer.parseInt(address[1]));
                attemptingJoin.setVisible(true);
                connectButton.setDisabled(true);
            }
        });
        cancelButton.setOnLeftClick(() -> {
            clientGroup.setVisible(false);
            attemptingJoin.setVisible(false);
            connectButton.setDisabled(false);
            hostButton.setDisabled(false);

            if(client != null){
                client.shutdown();
                client = null;
            }
        });

        //add all the actors
        group.addActor(addressDecor);
        group.addActor(addressLabel);
        group.addActor(addressField);
        group.addActor(userDecor);
        group.addActor(userLabel);
        group.addActor(userField);
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
        TextField userField = new TextField("", skin);
        userField.setStyle(textFieldStyle);
        userField.setBounds(650, 442, 200, 30);
        userField.setColor(new Color(1.2f*54/255f, 1.2f*56/255f, 1.2f*68/255f, 1));
        Image userDecor = new Image(Asset.retrieve(Asset.Shape.PIXEL_DARKPURPLE));
        userDecor.setBounds(userField.getX()-3, userField.getY()-3,
                userField.getWidth()+6, userField.getHeight()+6);
        Label userLabel = new Label("User", skin, "small", Color.WHITE);
        userLabel.setColor(Color.GRAY);
        userLabel.setPosition(650-userLabel.getWidth()-6, userField.getY()+3);

        //connect button
        launchButton = new RectTextButton("Launch", skin, "small");
        launchButton.setPosition(720+55, 405);
        launchButton.setPadding(24, 16, 2);

        //cancel button
        RectTextButton cancelButton = new RectTextButton("Cancel", skin, "small");
        cancelButton.setPosition(720-55, 405);
        cancelButton.setPadding(24, 16, 2);

        //attempting label
        attemptingLaunch = new Label("Launching...", skin, "small");
        attemptingLaunch.setPosition(680, 300);
        attemptingLaunch.setVisible(false);

        //listeners
        Lobby thisSave = this;
        launchButton.setOnLeftClick(() -> {
            attemptingLaunch.setVisible(true);
            launchButton.setDisabled(true);

            desiredUsername = userField.getText();
            if(desiredUsername.equals("")){
                desiredUsername = "Host";
            }

            //create the host
            host = new LobbyHost(thisSave);
        });
        cancelButton.setOnLeftClick(() -> {
            serverGroup.setVisible(false);
            joinButton.setDisabled(false);
            attemptingLaunch.setVisible(false);
            launchButton.setDisabled(false);
        });

        //add actors
        group.addActor(userDecor);
        group.addActor(userField);
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
        group.setPosition(720-TERMINAL_WIDTH/2f-3, 138);

        //close the server button
        terminateButton = new RectTextButton("", skin, "medium");
        terminateButton.setPadding(16, 10, 2);
        terminateButton.setPosition(TERMINAL_WIDTH/2f, 387);

        //ribbon of the terminal
        Image ribbon = new Image(Asset.retrieve(Asset.Shape.PIXEL_DARKPURPLE));
        ribbon.setSize(TERMINAL_WIDTH+6, 300+36+9);
        group.addActor(ribbon);

        //create the widget
        terminalWidget = new Table();
        terminalWidget.left().bottom();

        //create the pane
        pane = new ScrollPane(terminalWidget, skin);
        pane.setBounds(3, 36+6, TERMINAL_WIDTH, 300);
        pane.setColor(Color.BLACK);
        pane.setScrollingDisabled(true, false);
        pane.setFadeScrollBars(false);

        //text field input
        TextField textField = new TextField("", skin);
        textField.setStyle(textFieldStyle);
        textField.setBlinkTime(0.4f);
        textField.setBounds(3, 3, TERMINAL_WIDTH, 36);
        textField.setColor(new Color(1.2f*54/255f, 1.2f*56/255f, 1.2f*68/255f, 1));

        //listeners
        terminateButton.setOnLeftClick(() -> {
            //if you were hosting
            if(host != null){
                host.shutdown();
                host = null;

                addToTerminal(MChat.Type.LOGISTICAL, "> You closed the server.");
            }
            //if you were a client
            else {
                client.shutdown();
                client = null;

                addToTerminal(MChat.Type.LOGISTICAL, "> You left the server.");
            }

            addToTerminal(MChat.Type.LOGISTICAL,"--Connection Ended--\n\n");

            //reset graphics
            textField.setText("");
            terminalGroup.setVisible(false);
            initialGroup.setVisible(true);

            //shutdown the networking
            if(host != null){
                host.shutdown();
                host = null;
            }
            else {
                client.shutdown();
                client = null;
            }
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
        pane.addListener(event -> {
            if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.enter){
                stage.setScrollFocus(pane);
            }
            else if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.exit) {
                stage.setScrollFocus(null);
            }
            return true;
        });

        //add everything to the group
        group.addActor(terminateButton);
        group.addActor(pane);
        group.addActor(textField);
        return group;
    }
}
