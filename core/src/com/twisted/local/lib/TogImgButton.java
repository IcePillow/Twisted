package com.twisted.local.lib;

import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class TogImgButton extends Group{

    private Image one, two;
    private ClickListener clickListener;
    private EventListener enterListener;

    /**
     * This group can have other Actors added to it.
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


    /* Updating */

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
        this.addActorAt(0, this.one);
        this.addActorAt(1, this.two);

        //set visibility
        this.one.setVisible(true);
        this.two.setVisible(false);

        //add listeners
        this.one.addListener(clickListener);
        this.two.addListener(clickListener);
        this.one.addListener(enterListener);
        this.two.addListener(enterListener);
    }

    /**
     * Changes the click listener.
     */
    public void changeClickListener(ClickListener listener){
        if(clickListener != null){
            one.removeListener(clickListener);
            two.removeListener(clickListener);
        }

        clickListener = listener;

        one.addListener(listener);
        two.addListener(listener);
    }

    /**
     * Changes the enter/exit listener.
     */
    public void changeEnterListener(EventListener listener){
        if(enterListener != null){
            one.removeListener(enterListener);
            two.removeListener(enterListener);
        }

        enterListener = listener;

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


    /* Data */

    /**
     * Whether it is currently drawing textureOne.
     */
    public boolean drawingTextureOne(){
        return one.isVisible();
    }

}
