package com.twisted.local.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Legacy class. Not used right now. Saved in case it is wanted in the future.
 */
public class ActorHint {

    private Group group;
    public Group getGroup(){
        return group;
    }

    private VisibilityController visCon;


    /**
     * Constructor
     */
    public ActorHint(Actor actor, Skin skin, GlyphLayout glyph, String hintText){

        //create the display group
        float width = initGroup(glyph, skin, hintText);
        group.setPosition(actor.getX() + actor.getWidth()/2f - width/2f,
                actor.getY()+actor.getHeight()+2);
        group.setVisible(false);

        //create the enter/exit listener
        actor.addListener(event -> {
            if(event instanceof InputEvent && ((InputEvent) event).getType()==InputEvent.Type.enter){
                //cancel any current controllers
                if(visCon != null) {
                    visCon.cancel();
                }
                //create the new controller
                visCon = new VisibilityController(2000, true);
                visCon.start();
            }
            else if(event instanceof InputEvent && ((InputEvent) event).getType()==InputEvent.Type.exit){
                //cancel any current controllers
                if(visCon != null) {
                    visCon.cancel();
                }
                //set the visibility to false
                group.setVisible(false);
            }
            return true;
        });

    }

    /**
     * Utility method to create the group.
     * @return The width of the image.
     */
    private float initGroup(GlyphLayout glyph, Skin skin, String text){
        group = new Group();

        Label label = new Label(text, skin, "small", Color.LIGHT_GRAY);
        label.setPosition(4, 0);
        glyph.setText(skin.getFont("small"), text);

        Image image = new Image(new Texture(Gdx.files.internal("images/pixels/darkgray.png")));
        image.setSize(4+glyph.width+4, 2+glyph.height+2);

        group.addActor(image);
        group.addActor(label);

        return image.getWidth();
    }

    /**
     * Class used to set the group as visible after a delay.
     */
    private class VisibilityController extends Thread {

        private final int millis;
        private final boolean visible;

        private boolean cancelled;

        private VisibilityController(int millis, boolean visible) {
            this.millis = millis;
            this.visible = visible;
            this.cancelled = false;
        }

        @Override
        public void run(){
            try {
                Thread.sleep(millis);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(!cancelled){
                group.setVisible(visible);
            }
        }

        private void cancel() {
            cancelled = true;
        }
    }
}
