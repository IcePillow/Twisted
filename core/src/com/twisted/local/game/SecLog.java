package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.twisted.Asset;

class SecLog extends Sector {

    //exterior references
    private Game game;

    //graphics utilities
    private Skin skin;

    //graphics tree
    private Group parent;
    private VerticalGroup vertical;
    private ScrollPane pane;


    /**
     * Constructor
     */
    SecLog(Game game) {
        this.game = game;
        this.skin = game.skin;
    }


    @Override
    Group init() {
        parent = new Group();
        parent.setBounds(0, 30, 300, 65);

        //create the decoration
        Group decoration = new Group();
        decoration.setSize(parent.getWidth(), parent.getHeight());
        parent.addActor(decoration);

        Image ribbon = new Image(Asset.retrieve(Asset.Shape.PIXEL_DARKPURPLE));
        ribbon.setSize(decoration.getWidth(), decoration.getHeight());
        decoration.addActor(ribbon);
        Image embedded = new Image(Asset.retrieve(Asset.Shape.PIXEL_BLACK));
        embedded.setBounds(3, 3, parent.getWidth()-6, parent.getHeight()-6);
        decoration.addActor(embedded);

        //create the scrollpane's child
        vertical = new VerticalGroup();
        vertical.bottom().left();
        vertical.columnAlign(Align.left);

        //create the scrollpane
        pane = new ScrollPane(vertical, skin);
        pane.setBounds(3, 3, parent.getWidth()-6,
                parent.getHeight()-6);
        pane.setScrollingDisabled(true, false);
        pane.setupFadeScrollBars(0.2f, 0.2f);
        pane.setSmoothScrolling(false);
        pane.setColor(Color.BLACK);
        parent.addActor(pane);


        //add listeners
        pane.addListener(event -> {
            if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.enter){
                game.scrollFocus(pane);
            }
            else if(event instanceof InputEvent && ((InputEvent) event).getType()== InputEvent.Type.exit) {
                game.scrollFocus(null);
            }
            return false;
        });

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


    /**
     * Adds a Label to the log with the designated string.
     * TODO log glitches with strings that are wider than the scrollpane
     */
    void addToLog(String string, LogColor logColor){

        //create and add the label
        Label label = new Label(string, skin, "small", logColor.col);
        label.setFontScale(0.7f);
        vertical.addActor(label);

        //cap the number of children allowed
        if(vertical.getChildren().size > 10){
            vertical.removeActorAt(0, true);
        }

        //snap the scrollpane
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


    enum LogColor {

        GRAY(0.7f, 0.7f, 0.7f, 1),
        YELLOW(0.7f, 0.7f, 0, 1),
        RED(0.7f, 0, 0, 1);

        final Color col;

        LogColor(float r, float g, float b, float a){
            this.col = new Color(r, g, b, a);
        }
    }
}
