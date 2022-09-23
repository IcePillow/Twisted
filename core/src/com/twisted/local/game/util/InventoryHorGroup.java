package com.twisted.local.game.util;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.twisted.Asset;
import com.twisted.Main;

/**
 * Represents a row in one of the inventory panes in the details sector DetsShipDocked
 */
class InventoryHorGroup extends Table {

    /* Fields */

    private final DetsShipDocked detsShipDocked;
    private boolean loaded = false;

    private final float parentWidth;

    private Label nameLabel = null;
    private Actor filler = null;
    private Label amountLabel = null;

    /**
     * Constructor
     */
    InventoryHorGroup(DetsShipDocked detsShipDocked, float parentWidth, int parentIndex) {
        super();
        this.detsShipDocked = detsShipDocked;

        //copy
        this.parentWidth = parentWidth;

        //listeners
        InventoryHorGroup ihg = this;
        this.addListener(new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                detsShipDocked.inventoryRowClicked(ihg.getParent(), parentIndex);
                ihg.setBackground(Asset.retrieve(Asset.Pixel.DARKGRAY));
            }
        });
    }


    /* Updating */

    /**
     * Utility method that calls updateName followed by updateAmount
     */
    void updateAll(String name, String amount) {
        updateName(name);
        updateAmount(amount);
    }

    /**
     * Must be called before updateAmount().
     */
    void updateName(String name) {
        if (loaded) {
            nameLabel.setText(name);
            updateFiller();
        } else {
            load(name);
        }
    }

    /**
     * Basic update the text method.
     */
    private void updateAmount(String amount) {
        if (loaded) {
            amountLabel.setText(amount);
            updateFiller();
        } else {
            System.out.println("Unexpected updating amount when unloaded");
            new Exception().printStackTrace();
        }
    }


    /* Loading and Unloading */

    /**
     * Should only be used internally. Will automatically be called if needed when update
     * methods are called
     */
    private void load(String name) {
        nameLabel = new Label(name, Asset.labelStyle(Asset.Avenir.LIGHT_12));
        this.add(nameLabel);

        filler = new Actor();
        this.add(filler);

        amountLabel = new Label("", Asset.labelStyle(Asset.Avenir.LIGHT_12));
        this.add(amountLabel);

        updateFiller();

        loaded = true;
    }

    /**
     * Calls clear children if needed.
     */
    void unload() {
        if (loaded) clearChildren();
    }

    /**
     * Resets the background
     */
    void resetBackground() {
        this.setBackground((Drawable) null);
    }

    /**
     * Removes all actors and resets this group.
     */
    @Override
    public void clearChildren() {
        super.clearChildren();

        nameLabel = null;
        loaded = false;
        resetBackground();
    }


    /* Utility */

    /**
     * Resizes the filler actor correctly based on the other actors.
     */
    private void updateFiller() {
        Main.glyph.setText(nameLabel.getStyle().font, nameLabel.getText());
        filler.setHeight(Main.glyph.height);
        float width = parentWidth - (Main.glyph.width);
        Main.glyph.setText(nameLabel.getStyle().font, amountLabel.getText());
        width -= Main.glyph.width;

        filler.setWidth(width);
    }
}
