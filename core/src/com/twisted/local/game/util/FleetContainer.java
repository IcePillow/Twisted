package com.twisted.local.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.twisted.logic.entities.Entity;

public abstract class FleetContainer<T extends Actor> extends Container<T> {

    //debugging constant
    private final static boolean SHOW_DEBUG_BOXES = false;


    /**
     * This is technically a different reference than the actor in the superclass. However, it
     * should always remain referencing the same object.
     */
    private T t;
    public T actor(){
        return t;
    }


    /* Constructors */

    /**
     * Full constructor.
     * @param evLeftClick If an event for left clicks should be created.
     * @param evRightClick If an event for right clicks should be created.
     */
    public FleetContainer(T actor, float width, boolean evLeftClick, boolean evRightClick){
        super(actor);

        //copy values
        this.t = actor;
        this.width(width);

        //add listeners
        if(evLeftClick){
            addListener(new ClickListener(Input.Buttons.LEFT){
                @Override
                public void clicked(InputEvent event, float x, float y){
                    eventLeftClick();
                }
            });
        }
        if(evRightClick){
            addListener(new ClickListener(Input.Buttons.LEFT){
                @Override
                public void clicked(InputEvent event, float x, float y){
                    eventLeftClick();
                }
            });
        }

        //debug background
        if(SHOW_DEBUG_BOXES){
            Gdx.app.postRunnable(() -> {
                if(this.getParent().getChildren().indexOf(this, false) % 2 == 0){
                    this.setBackground(new TextureRegionDrawable(new Texture(Gdx.files.internal("images/pixels/green.png"))));
                }
                else {
                    this.setBackground(new TextureRegionDrawable(new Texture(Gdx.files.internal("images/pixels/magenta.png"))));
                }
            });
        }
    }

    /**
     * Constructor with no listeners.
     */
    public FleetContainer(T actor, float width){
        this(actor, width, false, false);
    }


    /* Updating */

    public abstract void updateValuesFromEntity(Entity entity);

    @Override
    public void setActor(T actor){
        super.setActor(actor);
        this.t = actor;
    }


    /* Listeners */

    /**
     * Listener method. Only used if evLeftClick was true.
     */
    public void eventLeftClick(){}
    /**
     * Listener method. Only used if evRightClick was true.
     */
    public void eventRightClick(){}
}
