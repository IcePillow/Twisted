package com.twisted.local.game.util;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.twisted.Asset;

/**
 * Represents a row in one of the inventory panes in the details sector DetsShipDocked
 */
class InventoryHorGroup extends Table {

    /* Fields */

    private final DetsShipDocked detsShipDocked;
    private boolean loaded = false;

    private final float parentWidth;
    private final int parentIndex;

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
        this.parentIndex = parentIndex;

        //listeners
        InventoryHorGroup ihg = this;
        this.addListener(new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                detsShipDocked.inventoryRowClicked(ihg.getParent(), parentIndex);
                ihg.setBackground(Asset.retrieve(Asset.Shape.PIXEL_DARKGRAY));
            }
        });
    }


    /* Updating */

    /**
     * Must be called before any of the other update methods
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
    void updateAmount(String amount) {
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
    protected void load(String name) {
        nameLabel = new Label(name, detsShipDocked.skin, "small", Color.LIGHT_GRAY);
        nameLabel.setFontScale(0.8f);
        this.add(nameLabel);

        filler = new Actor();
        this.add(filler);

        amountLabel = new Label("", detsShipDocked.skin, "small", Color.LIGHT_GRAY);
        amountLabel.setFontScale(0.8f);
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
        detsShipDocked.glyph.setText(detsShipDocked.skin.getFont("small"), nameLabel.getText());
        filler.setHeight(detsShipDocked.glyph.height * 0.8f);
        float width = parentWidth - (detsShipDocked.glyph.width * 0.8f);
        detsShipDocked.glyph.setText(detsShipDocked.skin.getFont("small"), amountLabel.getText());
        width -= detsShipDocked.glyph.width * 0.8f;

        filler.setWidth(width);
    }
}
