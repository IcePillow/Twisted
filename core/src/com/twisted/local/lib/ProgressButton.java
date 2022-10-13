package com.twisted.local.lib;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class ProgressButton extends Group {

    //state
    private Image image;
    private float progress;
    private Object textureKey;
    public Object getTextureKey(){
        return textureKey;
    }

    //drawing
    private final ShapeRenderer shape;
    private boolean matricesSet;
    private ClickListener clickListener;
    private EventListener enterListener;
    private final float padX, padY;
    private Color progressColor;


    /* Basic Overrides */

    /**
     * Constructor
     * @param padX Padding in x direction for the progress bar. Applied to both sides.
     * @param padY Padding in y direction for the progress bar. Applied to both sides.
     */
    public ProgressButton(TextureRegionDrawable texture, Object textureKey, float padX, float padY){
        super();

        this.shape = new ShapeRenderer();
        this.padX = padX;
        this.padY = padY;
        this.progressColor = Color.WHITE;

        this.image = new Image(texture);
        this.addActor(image);
        this.textureKey = textureKey;
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        //main draw
        super.draw(batch, parentAlpha);

        //draw the progress
        batch.end();
        if(!matricesSet){
            shape.setProjectionMatrix(batch.getProjectionMatrix());
            shape.setTransformMatrix(batch.getTransformMatrix());
            matricesSet = true;
        }
        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin(ShapeRenderer.ShapeType.Filled);

        shape.setColor(progressColor);
        shape.rect(getX()+padX, getY()+padY, this.getWidth()-2*padX,
                (this.getHeight()-2*padY)*progress);

        shape.end();
        Gdx.gl.glDisable(GL30.GL_BLEND);
        batch.begin();
    }



    /* Updating */

    /**
     * @param progress Values [0, 1], otherwise clamped.
     */
    public void setProgress(float progress){
        this.progress = progress;
        if(progress > 1) this.progress = 1;
        else if(progress < 0) this.progress = 0;
    }

    /**
     * Will switch texture unless the passed in textureKey matches the already existing texture key.
     */
    public void switchTexture(TextureRegionDrawable texture, Object textureKey){
        if(this.textureKey == null || !this.textureKey.equals(textureKey)){
            this.removeActor(this.image);
            this.image = new Image(texture);
            this.addActorAt(0, image);

            this.textureKey = textureKey;
        }
    }

    public void setProgressColor(Color color){
        this.progressColor = color;
    }

    public void changeClickListener(ClickListener listener){
        if(clickListener != null){
            this.removeListener(clickListener);
        }
        clickListener = listener;
        this.addListener(listener);
    }

    public void changeEnterListener(EventListener listener){
        if(enterListener != null){
            this.removeListener(enterListener);
        }
        enterListener = listener;
        this.addListener(listener);
    }


}
