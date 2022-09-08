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
    private HorizontalGroup mapGroup;

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
        buttonChild.setPosition(0, SecTerminal.TERMINAL_HEIGHT+5+16);
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
        Ribbon ribbon = new Ribbon(Asset.retrieve(Asset.Shape.PIXEL_DARKPURPLE), 3);
        ribbon.setSize(WIDTH+6, SecTerminal.TERMINAL_HEIGHT/2f+3-2);
        group.addActor(ribbon);
        Image band = new Image(Asset.retrieve(Asset.Shape.PIXEL_DARKPURPLE));
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
        Image titleBground = new Image(Asset.retrieve(Asset.Shape.PIXEL_BLACK));
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
        Ribbon ribbon = new Ribbon(Asset.retrieve(Asset.Shape.PIXEL_DARKPURPLE), 3);
        ribbon.setSize(WIDTH+6, SecTerminal.TERMINAL_HEIGHT/2f+3-2);
        group.addActor(ribbon);
        Image band = new Image(Asset.retrieve(Asset.Shape.PIXEL_DARKPURPLE));
        band.setBounds(0, ribbon.getHeight()-20-6, WIDTH+6, 3);
        group.addActor(band);

        //the pane
        VerticalGroup settingsVertical = new VerticalGroup();
        settingsVertical.top().left();
        settingsVertical.columnAlign(Align.left);
        ScrollPane pane = new ScrollPane(settingsVertical, skin);
        pane.setBounds(3, 3, WIDTH, ribbon.getHeight()-6-23);
        pane.setColor(Color.BLACK);
        pane.setScrollingDisabled(true, false);
        pane.setFadeScrollBars(false);
        group.addActor(pane);

        //title decor
        Image titleBground = new Image(Asset.retrieve(Asset.Shape.PIXEL_BLACK));
        titleBground.setBounds(3, ribbon.getHeight()-3-20, 150, 20);
        group.addActor(titleBground);
        //title
        Label title = new Label("Game Settings", Asset.labelStyle(Asset.Avenir.HEAVY_16));
        title.setColor(Color.GRAY);
        title.setPosition(titleBground.getX()+3, titleBground.getY()-1);
        group.addActor(title);

        //add to the vertical
        initSettingsVertical(settingsVertical);

        return group;
    }

    private void initSettingsVertical(VerticalGroup group){
        //map title actors
        HorizontalGroup mapTitleGroup = new HorizontalGroup();
        group.addActor(mapTitleGroup);
        Actor mapFiller1 = new Actor();
        mapTitleGroup.addActor(mapFiller1);
        Label mapTitleLabel = new Label("Map", Asset.labelStyle(Asset.Avenir.MEDIUM_12));
        mapTitleLabel.setColor(Color.GRAY);
        mapTitleGroup.addActor(mapTitleLabel);
        Main.glyph.setText(mapTitleLabel.getStyle().font, mapTitleLabel.getText());
        mapFiller1.setWidth(WIDTH/2f - Main.glyph.width/2f);

        //map settings actors
        mapGroup = new HorizontalGroup();
        group.addActor(mapGroup);
        Image mapArrow1 = new Image(Asset.retrieve(Asset.UiBasic.ARROW_3));
        mapArrow1.setColor(Color.GRAY);
        mapArrow1.setOrigin(mapArrow1.getWidth()/2f, mapArrow1.getHeight()/2f);
        mapArrow1.rotateBy(180);
        mapGroup.addActor(mapArrow1);
        Actor mapFiller2 = new Actor();
        mapGroup.addActor(mapFiller2);
        Label mapNameLabel = new Label("??", Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        mapNameLabel.setColor(Color.LIGHT_GRAY);
        mapGroup.addActor(mapNameLabel);
        Main.glyph.setText(mapNameLabel.getStyle().font, mapNameLabel.getText());
        Actor mapFiller3 = new Actor();
        mapGroup.addActor(mapFiller3);
        Image mapArrow2 = new Image(Asset.retrieve(Asset.UiBasic.ARROW_3));
        mapArrow2.setColor(Color.GRAY);
        mapGroup.addActor(mapArrow2);
        positionMapSetting();

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
                Asset.retrieve(Asset.Shape.PIXEL_BLACK));
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

        Label nameLabel = new Label(name, skin, "small", Color.WHITE);
        nameLabel.setColor(Color.LIGHT_GRAY);
        group.addActor(nameLabel);

        if(isHost){
            Label hostLabel = new Label("(H)", skin, "small", Color.WHITE);
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
    void settingMap(String mapName){
        ((Label) mapGroup.getChildren().get(2)).setText(mapName);

        positionMapSetting();
    }


    /* Layout */

    private void positionMapSetting(){
        Label label = (Label) mapGroup.getChild(2);
        Main.glyph.setText(label.getStyle().font, label.getText());
        float amt = (WIDTH-2)/2f - Main.glyph.width/2f - mapGroup.getChild(0).getWidth();

        mapGroup.getChild(1).setWidth((float)Math.floor(amt));
        mapGroup.getChild(3).setWidth((float)Math.ceil(amt));
    }

}
