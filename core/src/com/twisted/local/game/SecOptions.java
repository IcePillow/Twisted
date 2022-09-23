package com.twisted.local.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.twisted.Asset;
import com.twisted.Main;
import com.twisted.local.lib.RectTextButton;
import com.twisted.net.msg.gameUpdate.MGameEnd;

class SecOptions extends Sector {

    //reference variables
    private Game game;

    //graphics utilities
    private Stage stage;
    private Skin skin;

    //tree
    private Group endChild, optChild;
    private Label resultLabel;

    //storage
    private MGameEnd endMessage;


    /**
     * Constructor
     */
    SecOptions(Game game, Stage stage){
        this.game = game;
        this.stage = stage;
        this.skin = game.skin;
    }


    /* Standard Methods */

    @Override
    Group init() {
        Group parent = super.init();

        optChild = initOptChild();
        parent.addActor(optChild);

        endChild = initEndChild();
        parent.addActor(endChild);

        initListeners();

        return parent;
    }
    @Override
    void load() {

    }
    @Override
    void render(float delta) {

    }
    @Override
    void dispose() {

    }


    /* Creation Utility */

    private Group initOptChild(){
        //create the group
        Group child = new Group();
        child.setBounds(420, 150, 600, 500);

        //set the background
        Image main = new Image(Asset.retrieve(Asset.Pixel.BLACK));
        main.setSize(child.getWidth(), child.getHeight());
        child.addActor(main);

        //make invisible and return
        child.setVisible(false);

        return child;
    }

    private Group initEndChild(){
        //create the group
        Group child = new Group();
        child.setBounds(570, 400, 300, 150);

        //set the decoration
        Image ribbon = new Image(Asset.retrieve(Asset.Pixel.DARKPURLE));
        ribbon.setSize(child.getWidth(), child.getHeight());
        child.addActor(ribbon);
        Image embedded = new Image(Asset.retrieve(Asset.Pixel.BLACK));
        embedded.setBounds(3, 3, child.getWidth()-6, child.getHeight()-6);
        child.addActor(embedded);

        //result
        resultLabel = new Label("", Asset.labelStyle(Asset.Avenir.BLACK_32));
        resultLabel.setPosition(child.getWidth()/2, 90);
        child.addActor(resultLabel);

        //continue button
        RectTextButton contButton = new RectTextButton("Continue", Asset.labelStyle(Asset.Avenir.HEAVY_16));
        contButton.setPosition(150, 30);
        contButton.setPadding(16, 10, 2);
        child.addActor(contButton);

        //add listeners
        contButton.setOnLeftClick(() -> {
            game.optionsClickEvent(OptionEvent.END_GAME, endMessage);
        });

        //make invisible and return
        child.setVisible(false);
        return child;
    }

    private void initListeners(){
        stage.addListener(new InputListener() {
            public boolean keyDown(InputEvent event, int keycode) {
            if(keycode == 111 && !state.ending){
                optChild.setVisible(!optChild.isVisible());
            }
            return true;
            }
        });
    }


    /* Switching to End */

    void ending(MGameEnd msg){
        //store
        endMessage = msg;

        //modify graphics
        if(msg.winnerId == state.myId){
            resultLabel.setText("VICTORY");
            resultLabel.setColor(Color.GREEN);
        }
        else {
            resultLabel.setText("DEFEAT");
            resultLabel.setColor(Color.RED);
        }
        Main.glyph.setText(resultLabel.getStyle().font, resultLabel.getText());
        resultLabel.setX(resultLabel.getParent().getWidth()/2 - Main.glyph.width/2);

        //enable visibility
        endChild.setVisible(true);
    }


    /* Enums */

    enum OptionEvent {
        END_GAME
    }
}
