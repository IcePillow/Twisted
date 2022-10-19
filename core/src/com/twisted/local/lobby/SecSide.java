package com.twisted.local.lobby;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.twisted.Asset;
import com.twisted.Main;
import com.twisted.local.lib.RectTextButton;
import com.twisted.local.lib.Ribbon;
import com.twisted.logic.host.lobby.MatchSettings;
import com.twisted.net.msg.lobby.MSettingRequest;

import java.util.HashMap;

class SecSide extends Sector {

    //constants
    private final float WIDTH = 150;

    //graphics
    private Group buttonChild, playersChild, settingsChild;
    private VerticalGroup playersVertical;
    private Label mapNameLabel;

    //state
    private final HashMap<Integer, HorizontalGroup> playerMap;


    /**
     * Constructor
     */
    SecSide(Lobby lobby){
        super(lobby);

        playerMap = new HashMap<>();
    }


    @Override
    protected Group init() {
        Group parent = super.init();
        parent.setPosition(SecTerminal.TERMINAL_WIDTH+12, 3);

        playersChild = initPlayerList();
        playersChild.setVisible(false);
        parent.addActor(playersChild);

        settingsChild = initSettings();
        settingsChild.setVisible(false);
        settingsChild.setPosition(0, SecTerminal.TERMINAL_HEIGHT/2f+3+2);
        parent.addActor(settingsChild);

        buttonChild = initButton();
        buttonChild.setVisible(false);
        buttonChild.setPosition(0, SecTerminal.TERMINAL_HEIGHT+5+20);
        parent.addActor(buttonChild);

        return parent;
    }
    @Override
    void render(float delta) {

    }
    @Override
    void dispose() {

    }


    /* Graphics Utilities */

    private Group initPlayerList(){
        Group group = new Group();

        //ribbon of the terminal
        Ribbon ribbon = new Ribbon(Asset.retrieve(Asset.Pixel.DARKPURLE), 3);
        ribbon.setSize(WIDTH+6, SecTerminal.TERMINAL_HEIGHT/2f+3-2);
        group.addActor(ribbon);
        Image band = new Image(Asset.retrieve(Asset.Pixel.DARKPURLE));
        band.setBounds(0, ribbon.getHeight()-20-6, WIDTH+6, 3);
        group.addActor(band);

        //the pane
        playersVertical = new VerticalGroup();
        playersVertical.top().left();
        playersVertical.columnAlign(Align.left);
        ScrollPane pane = new ScrollPane(playersVertical, skin);
        pane.setBounds(3, 3, WIDTH, ribbon.getHeight()-6-23);
        pane.setColor(Color.BLACK);
        pane.setScrollingDisabled(true, false);
        pane.setFadeScrollBars(false);
        group.addActor(pane);

        //title decor
        Image titleBground = new Image(Asset.retrieve(Asset.Pixel.BLACK));
        titleBground.setBounds(3, ribbon.getHeight()-3-20, 150, 20);
        group.addActor(titleBground);

        //title
        Label title = new Label("Player List", Asset.labelStyle(Asset.Avenir.HEAVY_16));
        title.setColor(Color.GRAY);
        title.setPosition(titleBground.getX()+3, titleBground.getY()-1);
        group.addActor(title);

        return group;
    }

    private Group initSettings(){
        Group group = new Group();

        //ribbon of the terminal
        Ribbon ribbon = new Ribbon(Asset.retrieve(Asset.Pixel.DARKPURLE), 3);
        ribbon.setSize(WIDTH+6, SecTerminal.TERMINAL_HEIGHT/2f+3-2);
        group.addActor(ribbon);
        Image band = new Image(Asset.retrieve(Asset.Pixel.DARKPURLE));
        band.setBounds(0, ribbon.getHeight()-20-6, WIDTH+6, 3);
        group.addActor(band);

        //the pane
        Table settingsTable = new Table();
        settingsTable.top();
        ScrollPane pane = new ScrollPane(settingsTable, skin);
        pane.setBounds(3, 3, WIDTH, ribbon.getHeight()-6-23);
        pane.setColor(Color.BLACK);
        pane.setScrollingDisabled(true, false);
        pane.setFadeScrollBars(false);
        group.addActor(pane);

        //title decor
        Image titleBground = new Image(Asset.retrieve(Asset.Pixel.BLACK));
        titleBground.setBounds(3, ribbon.getHeight()-3-20, 150, 20);
        group.addActor(titleBground);
        //title
        Label title = new Label("Game Settings", Asset.labelStyle(Asset.Avenir.HEAVY_16));
        title.setColor(Color.GRAY);
        title.setPosition(titleBground.getX()+3, titleBground.getY()-1);
        group.addActor(title);

        //add to the vertical
        initSettingsTable(settingsTable);

        return group;
    }

    private void initSettingsTable(Table table){
        //map title
        Label mapTitleLabel = new Label("Map", Asset.labelStyle(Asset.Avenir.MEDIUM_12));
        mapTitleLabel.setColor(Color.GRAY);
        mapTitleLabel.setAlignment(Align.center);
        table.add(mapTitleLabel).growX();
        table.row();

        //map settings actors
        Table mapTable = new Table();
        table.add(mapTable).growX();
        table.row();
        Image mapArrow1 = new Image(Asset.retrieve(Asset.UiBasic.ARROW_3));
        mapArrow1.setColor(Color.GRAY);
        mapArrow1.setOrigin(mapArrow1.getWidth()/2f, mapArrow1.getHeight()/2f);
        mapArrow1.rotateBy(180);
        mapTable.add(mapArrow1);
        mapNameLabel = new Label("??", Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        mapNameLabel.setColor(Color.LIGHT_GRAY);
        mapTable.add(mapNameLabel).expandX();
        Image mapArrow2 = new Image(Asset.retrieve(Asset.UiBasic.ARROW_3));
        mapArrow2.setColor(Color.GRAY);
        mapTable.add(mapArrow2);

        //map listeners
        mapArrow1.addListener(new ClickListener(Input.Buttons.LEFT){
            @Override
            public void clicked(InputEvent event, float x, float y){
                lobby.settingChange(new MSettingRequest(MatchSettings.Type.MAP, false));
                event.handle();
            }
        });
        mapArrow2.addListener(new ClickListener(Input.Buttons.LEFT){
            @Override
            public void clicked(InputEvent event, float x, float y){
                lobby.settingChange(new MSettingRequest(MatchSettings.Type.MAP, true));
                event.handle();
            }
        });
    }

    private Group initButton(){
        Group group = new Group();

        //tree
        RectTextButton startButton = new RectTextButton("Start", Asset.labelStyle(Asset.Avenir.MEDIUM_16),
                Asset.retrieve(Asset.Pixel.BLACK));
        startButton.setPadding(12, 8, 2);
        startButton.setX(WIDTH/2);
        group.addActor(startButton);

        //listeners
        startButton.addListener(new ClickListener(Input.Buttons.LEFT){
            @Override
            public void clicked(InputEvent event, float x, float y){
                lobby.terminalInput("/start");
                event.handle();
            }
        });

        return group;
    }


    /* Event Handling */

    void addPlayer(int id, String name, boolean isHost){
        HorizontalGroup group = new HorizontalGroup();

        Label nameLabel = new Label(name, Asset.labelStyle(Asset.Avenir.MEDIUM_14));
        nameLabel.setColor(Color.LIGHT_GRAY);
        group.addActor(nameLabel);

        if(isHost){
            Label hostLabel = new Label(" (H)", Asset.labelStyle(Asset.Avenir.MEDIUM_14));
            hostLabel.setColor(Color.GRAY);
            group.addActor(hostLabel);
        }

        playersVertical.addActor(group);
        playerMap.put(id, group);
    }
    void removePlayer(int id){
        HorizontalGroup group = playerMap.remove(id);
        playersVertical.removeActor(group);
    }
    void renamePlayer(int id, String name){
        HorizontalGroup group = playerMap.get(id);
        if(group == null) return;

        //remove the group
        ((Label) group.getChildren().get(0)).setText(name);

    }
    void clearAllPlayers(){
        playersVertical.clearChildren();
        playerMap.clear();
    }

    void setBottomVisible(boolean visible){
        buttonChild.setVisible(visible);
    }
    void setPlayersVisible(boolean visible){
        playersChild.setVisible(visible);
    }
    void setSettingsVisible(boolean visible){
        settingsChild.setVisible(visible);
    }

    void revertSettings(){
        settingMap("??");
    }
    void settingMap(String name){
        mapNameLabel.setText(name);
    }

}
