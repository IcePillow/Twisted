package com.twisted.local.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.twisted.Asset;
import com.twisted.local.lib.Ribbon;

class SecLog extends Sector {

    //constants
    private final float LOG_WIDTH = 294;

    //exterior references
    private final Game game;

    //graphics utilities
    private final Skin skin;

    //graphics tree
    private Group parent;
    private Table table;
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
        parent.setBounds(0, 30, LOG_WIDTH+6, 65);

        //create the decoration
        Group decoration = new Group();
        decoration.setSize(parent.getWidth(), parent.getHeight());
        parent.addActor(decoration);

        Ribbon ribbon = new Ribbon(Asset.retrieve(Asset.Pixel.DARKPURLE), 3);
        ribbon.setSize(decoration.getWidth(), decoration.getHeight());
        decoration.addActor(ribbon);

        //create the scrollpane's child
        table = new Table();
        table.bottom().left();

        //create the scrollpane
        pane = new ScrollPane(table, skin);
        pane.setBounds(3, 3, LOG_WIDTH,
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
    void render(float delta, ShapeRenderer shape, SpriteBatch sprite) {

    }
    @Override
    void dispose() {

    }


    /**
     * Adds a Label to the log with the designated string.
     */
    void addToLog(String string, LogColor logColor){
        //create and add the label
        Label label = new Label("> " + string, Asset.labelStyle(Asset.Avenir.LIGHT_12));
        label.setColor(logColor.col);
        label.setWrap(true);
        table.row();
        table.add(label).width(LOG_WIDTH);

        //cap the number of children allowed
        if(table.getChildren().size > 20) table.removeActorAt(0, true);

        //snap the scrollpane
        Gdx.app.postRunnable(() -> pane.setScrollPercentY(1));
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
