package com.twisted.local.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.twisted.local.game.SecIndustry;
import com.twisted.logic.entities.Ship;

public class DockedShipRow extends HorizontalGroup {

    //reference
    private final SecIndustry sector;
    private final Ship ship;

    //ui tools
    private final float width;
    private final GlyphLayout glyph;
    private final Skin skin;

    //ui tree
    private Label name;
    private Actor filler1;
    private Image undockImage;


    /**
     * Constructor
     */
    public DockedShipRow(SecIndustry sector, Skin skin, GlyphLayout glyph, float width, Ship ship){
        super();

        //copy values
        this.sector = sector;
        this.ship = ship;
        this.width = width;
        this.skin = skin;
        this.glyph = glyph;

        //initialize
        initGraphics(skin);
        initHandling();
    }

    private void initGraphics(Skin skin){
        name = new Label("X", skin, "small", Color.LIGHT_GRAY);
        this.addActor(name);

        filler1 = new Actor();
        this.addActor(filler1);

        undockImage = new Image(new Texture(Gdx.files.internal("images/ui/icons/undock.png")));
        this.addActor(undockImage);
    }

    private void initHandling(){
        undockImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y){
                sector.undockButtonClicked(ship);
                event.handle();
            }
        });
    }

    public void updateName(String text){
        name.setText(text);

        glyph.setText(skin.getFont("small"), text);
        filler1.setWidth(width-glyph.width-19);
    }

}
