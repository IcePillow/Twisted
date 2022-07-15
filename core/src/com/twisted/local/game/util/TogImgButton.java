package com.twisted.local.game.util;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class TogImgButton extends Group{

    private Image one, two;
    private ClickListener listener;

    /**
     * Textures must have length at least one.
     */
    public TogImgButton(TextureRegionDrawable one, TextureRegionDrawable two){
        super();

        //create the images
        this.one = new Image(one);
        this.two = new Image(two);

        //add images to the group
        this.addActor(this.one);
        this.addActor(this.two);

        //set visibility and checked
        this.two.setVisible(false);
    }

    /**
     * Switches to new two new textures for checked and unchecked.
     */
    public void switchTextures(TextureRegionDrawable one, TextureRegionDrawable two){
        this.removeActor(this.one);
        this.removeActor(this.two);

        //create the images
        this.one = new Image(one);
        this.two = new Image(two);

        //add images to the group
        this.addActor(this.one);
        this.addActor(this.two);

        //set visibility
        this.one.setVisible(true);
        this.two.setVisible(false);

        //add listeners
        this.one.addListener(listener);
        this.two.addListener(listener);
    }

    /**
     * Changes the listener
     */
    public void changeClickListener(ClickListener listener){
        one.removeListener(listener);
        two.removeListener(listener);

        this.listener = listener;

        one.addListener(listener);
        two.addListener(listener);
    }

    /**
     * Toggles the two images.
     * @param oneVisible True for the first image to be visible.
     */
    public void updateVisible(boolean oneVisible){
        one.setVisible(oneVisible);
        two.setVisible(!oneVisible);
    }

    /**
     * Whether it is currently drawing textureOne.
     */
    public boolean drawingTextureOne(){
        return one.isVisible();
    }

}
