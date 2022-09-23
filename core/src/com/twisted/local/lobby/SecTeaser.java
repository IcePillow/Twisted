package com.twisted.local.lobby;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.twisted.Asset;
import com.twisted.Main;
import com.twisted.Paint;
import com.twisted.local.lib.Ribbon;
import com.twisted.local.lobby.util.TeaserModelList;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.entities.station.Station;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class SecTeaser extends Sector {

    //constants
    private static final float HEIGHT=330, TEXT_WIDTH=320, CUSHION=8, WIDTH=4*CUSHION+TEXT_WIDTH+35;

    //tree
    private Image selectTierImage, selectModelImage;
    private final Label[] text;
    private Group modelBar, textGroup;
    private final HashMap<Entity.Tier, TeaserModelList> modelLists;


    /**
     * Construction
     */
    public SecTeaser(Lobby lobby){
        super(lobby);

        text = new Label[4];
        modelLists = new HashMap<>();
    }


    /* Default Graphics */
    
    @Override
    protected Group init(){
        Group parent = super.init();
        parent.setPosition(1040, 20);

        Group decorGroup = initDecor();
        decorGroup.setPosition(-3, -3);
        parent.addActor(decorGroup);

        Group tierGroup = initTiers();
        tierGroup.setPosition(TEXT_WIDTH+3*CUSHION+3, HEIGHT);
        parent.addActor(tierGroup);

        Group modelGroup = initModels();
        modelGroup.setPosition(CUSHION+TEXT_WIDTH/2f, HEIGHT-40);
        parent.addActor(modelGroup);

        textGroup = initText();
        textGroup.setPosition(CUSHION, HEIGHT-65);
        parent.addActor(textGroup);

        return parent;
    }
    @Override
    void render(float delta) {

    }
    @Override
    void dispose() {

    }


    /* Initializing */

    private Group initDecor(){
        Group group = new Group();

        Image background = new Image(Asset.retrieve(Asset.Pixel.MENU_A2));
        background.setSize(WIDTH+6, HEIGHT+6);
        group.addActor(background);

        Ribbon ribbon = new Ribbon(Asset.retrieve(Asset.Pixel.DARKPURLE), 3);
        ribbon.setSize(WIDTH+6, HEIGHT+6);
        group.addActor(ribbon);

        Image strip = new Image(Asset.retrieve(Asset.Pixel.DARKPURLE));
        strip.setBounds(3+CUSHION+TEXT_WIDTH+CUSHION, 0, 3, HEIGHT+6);
        group.addActor(strip);

        return group;
    }

    private Group initTiers(){
        Group group = new Group();

        float yPos = -42;

        ArrayList<Entity.Tier> tiers = new ArrayList<>();
        tiers.addAll(Arrays.asList(Ship.Tier.values()).subList(0, Ship.Tier.values().length-1));
        tiers.addAll(Arrays.asList(Station.Tier.values()));
        tiers.add(Ship.Tier.Titan);

        for(Entity.Tier t : tiers){
            Image img = new Image(Asset.retrieveEntityIcon(t));
            img.setColor(Color.GRAY);
            img.setSize(32, 32);
            img.setPosition(0, yPos);
            group.addActor(img);

            img.addListener(new ClickListener(Input.Buttons.LEFT){
                @Override
                public void clicked(InputEvent event, float x, float y){
                    selectEntityTier(t, img);
                    event.handle();
                }
            });

            yPos -= 42;
        }

        return group;
    }

    private Group initModels(){
        Group group = new Group();

        //prep
        TeaserModelList subgroup;

        //loop through tiers
        for(Ship.Tier t : Ship.Tier.values()){
            subgroup = new TeaserModelList(t, this);

            subgroup.setX(-48*subgroup.getChildren().size/2f+8);
            subgroup.setVisible(false);
            group.addActor(subgroup);
            modelLists.put(t, subgroup);
        }
        for(Station.Tier t : Station.Tier.values()){
            subgroup = new TeaserModelList(t, this);

            subgroup.setX(-48*subgroup.getChildren().size/2f+8);
            subgroup.setVisible(false);
            group.addActor(subgroup);
            modelLists.put(t, subgroup);
        }

        return group;
    }

    private Group initText(){
        Group group = new Group();

        Label titleLab = new Label("", Asset.labelStyle(Asset.Avenir.HEAVY_20));
        titleLab.setPosition(TEXT_WIDTH/2f, 0);
        group.addActor(titleLab);
        text[0] = titleLab;

        Label tierLab = new Label("", Asset.labelStyle(Asset.Avenir.MEDIUM_12));
        tierLab.setColor(Color.LIGHT_GRAY);
        tierLab.setAlignment(Align.top);
        tierLab.setWidth(TEXT_WIDTH);
        group.addActor(tierLab);
        text[1] = tierLab;

        Label bodyLab = new Label("", Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        bodyLab.setColor(Color.LIGHT_GRAY);
        bodyLab.setAlignment(Align.top);
        bodyLab.setWrap(true);
        bodyLab.setWidth(TEXT_WIDTH);
        group.addActor(bodyLab);
        text[2] = bodyLab;

        Label flavorLab = new Label("", Asset.labelStyle(Asset.Avenir.MEDIUM_12_ITALIC));
        flavorLab.setColor(Color.GRAY);
        flavorLab.setAlignment(Align.top);
        flavorLab.setWrap(true);
        flavorLab.setWidth(TEXT_WIDTH);
        group.addActor(flavorLab);
        text[3] = flavorLab;

        return group;
    }


    /* Internal Events */

    private void selectEntityTier(Entity.Tier tier, Image tierImage){
        //update the image colors
        if(selectTierImage != null) selectTierImage.setColor(Color.GRAY);
        tierImage.setColor(Paint.VERY_LIGHT_GRAY.col);
        selectTierImage = tierImage;

        //model bar
        if(modelBar != null) modelBar.setVisible(false);
        modelBar = modelLists.get(tier);
        modelBar.setVisible(true);

        //model selection
        TeaserModelList modelList = modelLists.get(tier);
        selectEntityModel(modelList.getCurrentModel(), modelList.getCurrentImage());
    }

    /**
     * Accepts null values for its parameters.
     */
    public void selectEntityModel(Entity.Model model, Image modelImage){
        //update the image
        if(selectModelImage != null) selectModelImage.setColor(Color.GRAY);
        if(modelImage != null) modelImage.setColor(Paint.VERY_LIGHT_GRAY.col);
        selectModelImage = modelImage;

        if(model == null){
            textGroup.setVisible(false);
        }
        else {
            //visibility
            textGroup.setVisible(true);

            //grab the blurb
            String[] blurb = Asset.retrieveEntityBlurb(model).split("<>");

            //update the title text
            if(model instanceof Ship.Model){
                text[0].setText(((Ship.Model) model).name());
            }
            else if(model instanceof Station.Model){
                text[0].setText(((Station.Model) model).name());
            }
            Main.glyph.setText(text[0].getStyle().font, text[0].getText());
            text[0].setX(TEXT_WIDTH/2f-Main.glyph.width/2f);

            //update the tier text
            if(model instanceof Ship.Model){
                text[1].setText(((Ship.Model) model).tier.name() + " Class");
            }
            else if(model instanceof Station.Model) {
                text[1].setText("Station Class");
            }
            Main.glyph.setText(text[0].getStyle().font, text[0].getText());
            text[1].setY(text[0].getY() - 4 - Main.glyph.height/2f);

            //update the body text
            text[2].setText(blurb[0].trim());
            Main.glyph.setText(text[1].getStyle().font, text[1].getText());
            text[2].setY(text[1].getY() - 25 - Main.glyph.height/2f);

            //update the flavor text
            text[3].setText(blurb[1].trim());
            Main.glyph.setText(text[2].getStyle().font, text[2].getText(), text[2].getColor(), TEXT_WIDTH,
                    text[2].getLabelAlign(), text[2].getWrap());
            text[3].setY(text[2].getY() - 30 - Main.glyph.height);
        }
    }
}
