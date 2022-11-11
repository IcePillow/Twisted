package com.twisted.local.lobby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.twisted.util.Asset;
import com.twisted.Main;
import com.twisted.logic.host.lobby.LobbyHost;
import com.twisted.net.client.Client;
import com.twisted.net.client.ClientContact;
import com.twisted.net.msg.*;
import com.twisted.net.msg.lobby.*;
import com.twisted.net.msg.remaining.MSceneChange;
import com.twisted.net.msg.remaining.MChat;
import com.twisted.local.game.Game;
import com.twisted.util.Quirk;

import java.util.Arrays;

/**
 * The Lobby Screen
 */
public class Lobby implements Screen, ClientContact {

    //exterior references
    private final Main main;
    private Client client;
    private LobbyHost host;

    //networking
    private int myId;
    private boolean leavingServer;

    //graphics
    private final Stage stage;
    Skin skin;

    //sectors
    private Sector[] sectors;
    private SecDecor decorSec;
    private SecConnect connectSec;
    private SecTerminal terminalSec;
    private SecSide sideSec;
    private SecTeaser teaserSec;

    //styles
    TextField.TextFieldStyle textFieldStyle;


    /* Constructor */

    public Lobby(Main main){
        this.main = main;

        //graphics
        stage = new Stage(new FitViewport(Main.WIDTH, Main.HEIGHT));
        Gdx.input.setInputProcessor(stage);
        initSectors();
    }


    /* Screen Methods */

    @Override
    public void show() {
    }
    @Override
    public void render(float delta) {
        //reset background
        Gdx.gl.glClearColor(0, 0, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //live drawings
        for(Sector s : sectors){
            s.render(delta);
        }

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
        //tell sectors generally
        terminalSec.scrollToBottom();
        sideSec.setPlayersVisible(true);
        sideSec.setSettingsVisible(true);

        //hosting only things
        if(host != null) {
            //tell sectors
            sideSec.setBottomVisible(true);
            connectSec.connectedToServer(true);
        }
        //client only things
        else {
            //tell sectors
            connectSec.connectedToServer(false);

            //chat
            terminalSec.addToTerminal(MChat.Type.LOGISTICAL, "\n\n> You connected to a server");
        }

        //name change
        if(connectSec.getDesiredUsername().length() > 0) {
            client.send(new MCommand(new String[]{"name", connectSec.getDesiredUsername()}));
        }
    }
    @Override
    public void failedToConnect() {
        connectSec.failedToConnectToServer();
        terminalSec.addToTerminal(MChat.Type.LOGISTICAL, "> Failed to connect to server");
    }
    @Override
    public void clientReceived(Message msg) {
        if(client == null) return;

        if(msg instanceof MChat){
            terminalSec.addToTerminal(((MChat) msg).type, ((MChat) msg).string);
        }
        else if(msg instanceof MSceneChange){
            if(((MSceneChange) msg).getChange() == MSceneChange.Change.GAME){
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
                new Quirk(Quirk.Q.UnknownClientDataSpecification).print();
            }
        }
        else if(msg instanceof MLobbyPlayerChange){
            MLobbyPlayerChange m = (MLobbyPlayerChange) msg;

            switch (m.type){
                case JOIN:
                    terminalSec.addToTerminal(MChat.Type.LOGISTICAL, "> " + m.name + " has joined the lobby");
                    sideSec.addPlayer(m.id, m.name, m.isHost);
                    break;
                case LEAVE:
                    terminalSec.addToTerminal(MChat.Type.LOGISTICAL, "> " + m.name + " has left the lobby");
                    sideSec.removePlayer(m.id);
                    break;
                case RENAME:
                    if(myId == m.id) terminalSec.addToTerminal(MChat.Type.LOGISTICAL, "> You changed your name to " + m.name);
                    else terminalSec.addToTerminal(MChat.Type.LOGISTICAL, "> " + m.oldName + " changed their name to " + m.name);
                    sideSec.renamePlayer(m.id, m.name);
                    break;
            }
        }
        else if(msg instanceof MLobbyWelcome){
            MLobbyWelcome m = (MLobbyWelcome) msg;
            myId = m.yourId;

            //player ids
            for(int i=0; i<m.playerIdList.length; i++){
                if(m.playerIdList[i] == m.hostId){
                    if(m.yourId == m.hostId) terminalSec.addToTerminal(MChat.Type.LOGISTICAL, "\n\n> You started hosting on port " + host.getPort());
                    else terminalSec.addToTerminal(MChat.Type.LOGISTICAL, "> Welcome to the lobby hosted by " + m.playerNameList[i]);
                }
                sideSec.addPlayer(m.playerIdList[i], m.playerNameList[i], m.playerIdList[i]==m.hostId);
            }

            //state
            sideSec.settingMap(m.settings.map.name());
        }
        else if(msg instanceof MSettingChange){
            MSettingChange m = (MSettingChange) msg;
            switch(m.type){
                case MAP:
                    sideSec.settingMap(m.value.toString());
                    break;
                default:
                    new Quirk(Quirk.Q.UnknownClientDataSpecification).print();
            }
        }
    }
    @Override
    public void disconnected(String reason){
        if(!leavingServer) terminalSec.addToTerminal(MChat.Type.LOGISTICAL, "> Disconnected: " + reason);
        leftServer();
    }
    @Override
    public void lostConnection() {
        if(!leavingServer) terminalSec.addToTerminal(MChat.Type.LOGISTICAL, "> You lost connection");
        leftServer();
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
        connectSec.stopLaunchingServer();
    }


    /* Prep Graphics */

    private void initSectors(){
        //load the skin
        skin = new Skin(Gdx.files.internal("skins/sgx/skin/sgx-ui.json"));
        skin.getFont("small").getData().markupEnabled = true;
        skin.getFont("medium").getData().markupEnabled = true;
        skin.getFont("title").getData().markupEnabled = true;
        createStyles();

        //load the sectors
        decorSec = new SecDecor(this, stage);
        stage.addActor(decorSec.init());

        connectSec = new SecConnect(this);
        stage.addActor(connectSec.init());

        terminalSec = new SecTerminal(this);
        stage.addActor(terminalSec.init());

        sideSec = new SecSide(this);
        stage.addActor(sideSec.init());

        teaserSec = new SecTeaser(this);
        stage.addActor(teaserSec.init());

        //set the sectors array
        sectors = new Sector[]{decorSec, connectSec, terminalSec, sideSec, teaserSec};
    }

    private void createStyles(){
        Asset.labelStyle(Asset.Avenir.MEDIUM_14);

        textFieldStyle = new TextField.TextFieldStyle(
                Asset.retrieve(Asset.Avenir.MEDIUM_16),
                Color.WHITE,
                Asset.retrieve(Asset.UiBasic.CURSOR_1),
                null,
                Asset.retrieve(Asset.Pixel.BLACK)
        );
        textFieldStyle.messageFont = Asset.retrieve(Asset.Avenir.LIGHT_16);
        textFieldStyle.messageFontColor = new Color(0.3f, 0.3f, 0.3f, 1);
    }


    /* Sector Events */

    void connectAsClient(String[] address){
        client = new Client(this, address[0], Integer.parseInt(address[1]));
    }
    void cancelClientConnect(){
        if(client != null){
            client.shutdown();
            client = null;
        }
    }
    void launchServer(){
        host = new LobbyHost(this);
    }

    void terminateClicked(){
        leavingServer = true;

        //if you were hosting
        if(host != null){
            terminalSec.addToTerminal(MChat.Type.LOGISTICAL, "> Closing the server");

            host.shutdown();
            host = null;
        }
        //if you were a client
        else {
            terminalSec.addToTerminal(MChat.Type.LOGISTICAL, "> You left the server");

            client.send(new Disconnect());
        }
    }

    void scrollFocus(Actor actor){
        stage.setScrollFocus(actor);
    }
    void keyboardFocus(Actor actor){
        stage.setKeyboardFocus(actor);
    }

    void terminalInput(String string){
        //check it's not empty
        if(string.equals("")) return;

        //commands
        if(string.charAt(0) == '/' && string.length() > 1){
            MCommand command = new MCommand(string.substring(1).split(" "));

            switch(command.getType()){
                //handle locally
                case HELP: {
                    terminalSec.addToTerminal(MChat.Type.LOGISTICAL, SecTerminal.HELP_TEXT);
                    break;
                }
                //send over the network
                case START:
                case NAME:
                case KICK:
                    if(client != null) client.send(command);
                    else terminalSec.addToTerminal(MChat.Type.LOGISTICAL,
                            "> You are not connected to a server");
                    break;
                case NONE:
                    break;
                default:
                    new Quirk(Quirk.Q.UnknownClientDataSpecification).print();
            }
        }
        //basic chats
        else {
            //create the chat
            MChat chat = new MChat(MChat.Type.PLAYER_CHAT, string);

            //send the chat
            if(client != null) client.send(chat);
            else terminalSec.addToTerminal(MChat.Type.LOGISTICAL,
                    "> You are not connected to a server");
        }
    }

    void settingChange(MSettingRequest msg){
        client.send(msg);
    }


    /* Internal Events */

    private void leftServer(){
        //update sectors
        terminalSec.addConnectionEnded();
        connectSec.leaveCloseServer();
        sideSec.clearAllPlayers();
        sideSec.revertSettings();
        sideSec.setBottomVisible(false);
        sideSec.setPlayersVisible(false);
        sideSec.setSettingsVisible(false);

        //network stuff
        if(client != null) client.shutdown();
        client = null;

        //clean up
        leavingServer = false;
    }

}
