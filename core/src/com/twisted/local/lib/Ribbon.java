package com.twisted.local.lib;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.twisted.Asset;

/**
 * Four images that make a ribbon around a box.
 */
public class Ribbon extends Group {

    private float thick;

    private final Image top, bottom, left, right;


    /**
     * Constructor
     * @param drawable Should be of a pixel.
     */
    public Ribbon(TextureRegionDrawable drawable, float thick){
        this.thick = thick;

        top = new Image(drawable);
        this.addActor(top);
        bottom = new Image(drawable);
        this.addActor(bottom);
        left = new Image(drawable);
        this.addActor(left);
        right = new Image(drawable);
        this.addActor(right);
    }

    private void updateBounds(){
        top.setBounds(0, getHeight()-thick, getWidth(), thick);
        right.setBounds(getWidth()-thick, 0, thick, getHeight());

        bottom.setSize(getWidth(), thick);
        left.setSize(thick, getHeight());
    }


    @Override
    public void sizeChanged(){
        updateBounds();
    }

    public void setThickness(float thick){
        this.thick = thick;
        updateBounds();
    }
}
