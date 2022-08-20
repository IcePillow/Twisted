package com.twisted.local.lib;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.twisted.Asset;
import com.twisted.Main;

public class RectTextButton extends Group {

    //tree
    private Image trimLight, trimMed, trimDark, embedded;
    private Label label;

    //state
    private boolean disabled;
    private float padHor, padVer;
    private float trimThick;
    private ClickListener onLeftClick;


    /* Construction */

    /**
     * This Actor is always center aligned (center of the label is at x, y).
     */
    public RectTextButton(String text, Skin skin, String styleName){
        super();

        //set values
        padHor = 0;
        padVer = 0;
        trimThick = 1;

        //initialize everything
        createActors(skin, styleName);
        createListeners();
        setText(text);
    }

    private void createActors(Skin skin, String styleName){
        //create trims and embedded
        trimLight = new Image(Asset.retrieve(Asset.Shape.PIXEL_LIGHTGRAY));
        this.addActor(trimLight);
        trimMed = new Image(Asset.retrieve(Asset.Shape.PIXEL_GRAY));
        this.addActor(trimMed);
        trimDark = new Image(Asset.retrieve(Asset.Shape.PIXEL_DARKGRAY));
        trimDark.setVisible(false);
        this.addActor(trimDark);
        embedded = new Image(Asset.retrieve(Asset.Shape.PIXEL_BLACK));
        embedded.setPosition(0, 0);
        this.addActor(embedded);

        //create label
        label = new Label("", skin, styleName, Color.WHITE);
        label.setAlignment(Align.center);
        label.setColor(Color.GRAY);
        label.setPosition(0, 0);
        this.addActor(label);
    }

    private void createListeners(){
        this.addListener(new InputListener(){
            /*
            [Copied from SecIndustry]
            These extra complications were necessary because of what seems like a bug in
            the library. Clicking on the actor without moving causes an enter event then an
            exit event to occur. These two booleans track and account for this.
             */
            boolean entered = false;
            boolean extraEnter = false;
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, @Null Actor fromActor){
                if(entered){
                    extraEnter = true;
                }
                else if(!disabled) {
                    label.setColor(Color.LIGHT_GRAY);
                    trimLight.setVisible(true);
                    trimMed.setVisible(false);
                    entered = true;
                }
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, @Null Actor fromActor){
                if(extraEnter){
                    extraEnter = false;
                }
                else if(!disabled) {
                    label.setColor(Color.GRAY);
                    trimLight.setVisible(false);
                    trimMed.setVisible(true);
                    entered = false;
                }
            }
        });
    }


    /* Access */

    public String getText(){
        return label.getText().toString();
    }

    public boolean isDisabled(){
        return disabled;
    }

    public float getHorizontalPad(){
        return padHor;
    }
    public float getVerticalPad(){
        return padVer;
    }


    /* Updating */

    public void setText(String text){
        //copy values
        label.setText(text);
        Main.glyph.setText(label.getStyle().font, text);

        //resize images
        trimLight.setBounds(-(Main.glyph.width+2*trimThick+padHor)/2, -(Main.glyph.height+2*trimThick+padVer)/2,
                Main.glyph.width+2*trimThick+padHor, Main.glyph.height+2*trimThick+padVer);
        trimMed.setBounds(-(Main.glyph.width+2*trimThick+padHor)/2, -(Main.glyph.height+2*trimThick+padVer)/2,
                Main.glyph.width+2*trimThick+padHor, Main.glyph.height+2*trimThick+padVer);
        trimDark.setBounds(-(Main.glyph.width+2*trimThick+padHor)/2, -(Main.glyph.height+2*trimThick+padVer)/2,
                Main.glyph.width+2*trimThick+padHor, Main.glyph.height+2*trimThick+padVer);
        embedded.setBounds(-(Main.glyph.width+padHor)/2, -(Main.glyph.height+padVer)/2,
                Main.glyph.width+padHor, Main.glyph.height+padVer);
    }

    public void setDisabled(boolean disabled){
        this.disabled = disabled;

        trimMed.setVisible(!disabled);
        trimMed.setVisible(!disabled);
        trimDark.setVisible(disabled);

        if(disabled) label.setColor(Color.DARK_GRAY);
        else label.setColor(Color.GRAY);
    }

    /**
     * Setting cosmetics values.
     * @param padHor Padding in horizontal direction.
     * @param padVer Padding in vertical direction.
     * @param thick Thickness of the ribbon.
     */
    public void setPadding(float padHor, float padVer, float thick){
        this.padHor = padHor;
        this.padVer = padVer;
        this.trimThick = thick;

        setText(getText());
    }

    /**
     * Will replace a previous on left click.
     * @param runnable Will be run when this button is clicked if !disabled.
     */
    public void setOnLeftClick(Runnable runnable){
        if(onLeftClick != null){
            this.removeListener(onLeftClick);
        }

        onLeftClick = new ClickListener(Input.Buttons.LEFT){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(disabled) return;

                runnable.run();
            }
        };
        this.addListener(onLeftClick);
    }

}
