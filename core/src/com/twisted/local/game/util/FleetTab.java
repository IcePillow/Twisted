package com.twisted.local.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.twisted.util.Asset;
import com.twisted.Main;

public class FleetTab {

    /**
     * Title of tab that can be clicked to switch tabs.
     */
    private Group header;
    public Group getHeader() {
        return header;
    }

    //graphical stuff
    private Label label;
    private Image black;

    /**
     * Creates a header object for a SecFleet object. Does not add any listeners.
     * Also creates a vertical group for the scroll pane.
     */
    public FleetTab(String name, Vector2 headerPos,
                    Vector2 headerSize) {
        //graphics
        createGraphics(name, headerPos, headerSize);
    }

    private void createGraphics(String name, Vector2 headerPos, Vector2 headerSize) {
        //creates a group
        header = new Group();
        header.setPosition(headerPos.x, headerPos.y);

        //create the background images
        Image purple = new Image(new Texture(Gdx.files.internal("images/pixels/darkpurple.png")));
        purple.setSize(headerSize.x, headerSize.y);
        header.addActor(purple);
        black = new Image(new Texture(Gdx.files.internal("images/pixels/black.png")));
        black.setBounds(2, 0, headerSize.x - 4, headerSize.y - 2);
        header.addActor(black);

        //create the label
        label = new Label(name, Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        label.setColor(Color.LIGHT_GRAY);
        Main.glyph.setText(label.getStyle().font, name);
        label.setPosition(headerSize.x / 2f - Main.glyph.width / 2f, -2);
        header.addActor(label);
    }


    /* Exterior Methods */

    /**
     * Selecting or deselecting the tab at the top.
     * @param select True to select, false to deselect.
     */
    public void selectTab(boolean select) {
        black.setVisible(!select);
    }

}
