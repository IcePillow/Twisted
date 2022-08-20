package com.twisted.local.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.twisted.local.game.SecIndustry;
import com.twisted.logic.entities.Ship;

public class IndShipRow extends IndustryRow {

    //reference
    private final Ship ship;

    //ui tree
    private Label nameLabel;
    private Image undockImage;


    /**
     * Constructor
     */
    public IndShipRow(SecIndustry sector, Skin skin, GlyphLayout glyph, float width, Ship ship){
        super(sector, skin, glyph, width);

        //copy values
        this.ship = ship;

        //initialize
        initGraphics(skin);
        initHandling();
    }

    private void initGraphics(Skin skin){
        nameLabel = new Label(ship.getFullName(), skin, "small", Color.LIGHT_GRAY);
        this.addActor(nameLabel);

        Actor filler = new Actor();
        glyph.setText(skin.getFont("small"), nameLabel.getText());
        filler.setWidth(width-glyph.width-19);
        this.addActor(filler);

        undockImage = new Image(new Texture(Gdx.files.internal("images/ui/icons/undock.png")));
        this.addActor(undockImage);
    }

    private void initHandling(){
        nameLabel.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                sector.focusShipRequest(ship);
                event.handle();
            }
        });

        undockImage.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                sector.undockButtonClicked(ship);
                event.handle();
            }
        });
    }

    @Override
    public boolean matches(Ship ship){
        return this.ship.matches(ship);
    }

    @Override
    public void update(){}

}
