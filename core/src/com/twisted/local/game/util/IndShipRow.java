package com.twisted.local.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.twisted.Asset;
import com.twisted.Main;
import com.twisted.local.game.SecIndustry;
import com.twisted.logic.entities.ship.Ship;

public class IndShipRow extends IndustryRow {

    //reference
    private final Ship ship;

    //ui tree
    private Label nameLabel;
    private Image undockImage;


    /**
     * Constructor
     */
    public IndShipRow(SecIndustry sector, float width, Ship ship){
        super(sector, width);

        //copy values
        this.ship = ship;

        //initialize
        initGraphics();
        initHandling();
    }

    private void initGraphics(){
        nameLabel = new Label(ship.getFullName(), Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        nameLabel.setColor(Color.LIGHT_GRAY);
        this.addActor(nameLabel);

        Actor filler = new Actor();
        Main.glyph.setText(nameLabel.getStyle().font, nameLabel.getText());
        filler.setWidth(width- Main.glyph.width-19);
        this.addActor(filler);

        undockImage = new Image(new Texture(Gdx.files.internal("images/ui/icons/undock.png")));
        undockImage.setColor(Color.LIGHT_GRAY);
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
