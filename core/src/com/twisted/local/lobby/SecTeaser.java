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
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.Station;

import java.util.ArrayList;
import java.util.Arrays;

class SecTeaser extends Sector {

    //constants
    private static final Color LIGHT_GRAY = new Color(0.9f, 0.9f, 0.9f, 1f);
    private static final float TEXT_WIDTH=320;

    //tree
    private Image selectedImage, selectionArrow;
    private Label titleLabel;
    private final Label[] text; //body, flavor



    /**
     * Construction
     */
    public SecTeaser(Lobby lobby){
        super(lobby);

        text = new Label[2];
    }


    /* Default Graphics */
    
    @Override
    protected Group init(){
        Group parent = super.init();
        parent.setPosition(1010, 380);
        
        Group entityGroup = initEntitySprites();
        entityGroup.setPosition(350, 0);
        parent.addActor(entityGroup);

        Group textGroup = initText();
        textGroup.setPosition(0, 0);
        parent.addActor(textGroup);

        return parent;
    }
    @Override
    void render(float delta) {

    }
    @Override
    void dispose() {

    }
    
    private Group initEntitySprites(){
        Group group = new Group();

        //selection arrow
        selectionArrow = new Image(Asset.retrieve(Asset.UiBasic.ARROW_2));
        selectionArrow.setOrigin(selectionArrow.getWidth()/2f, selectionArrow.getHeight()/2f);
        selectionArrow.rotateBy(180);
        selectionArrow.setColor(LIGHT_GRAY);
        selectionArrow.setPosition(40, -30);
        selectionArrow.setVisible(false);
        group.addActor(selectionArrow);

        float yPos = 0;

        ArrayList<Entity.Subtype> subtypes = new ArrayList<>();
        subtypes.addAll(Arrays.asList(Ship.Model.values()).subList(0, Ship.Model.values().length-1));
        subtypes.addAll(Arrays.asList(Station.Model.values()));
        subtypes.add(Ship.Model.Titan);

        for(Entity.Subtype s : subtypes){
            Image img = new Image(Asset.retrieveEntityIcon(s));
            img.setColor(Color.GRAY);
            img.setScale(2f);
            img.setPosition(0, yPos);
            group.addActor(img);

            float tempPos = yPos;
            img.addListener(new ClickListener(Input.Buttons.LEFT){
                @Override
                public void clicked(InputEvent event, float x, float y){
                    selectEntity(s, img, tempPos);
                    event.handle();
                }
            });

            yPos -= 50;
        }

        return group;
    }

    private Group initText(){
        Group group = new Group();

        titleLabel = new Label("", Asset.labelStyle(Asset.Avenir.HEAVY_20));
        titleLabel.setPosition(TEXT_WIDTH/2f, 0);
        group.addActor(titleLabel);

        text[0] = new Label("", Asset.labelStyle(Asset.Avenir.MEDIUM_16));
        text[0].setColor(Color.LIGHT_GRAY);
        text[0].setAlignment(Align.top);
        text[0].setWrap(true);
        text[0].setWidth(TEXT_WIDTH);
        text[0].setPosition(0, -20);
        group.addActor(text[0]);

        text[1] = new Label("", Asset.labelStyle(Asset.Avenir.MEDIUM_12_ITALIC));
        text[1].setColor(Color.GRAY);
        text[1].setAlignment(Align.top);
        text[1].setWrap(true);
        text[1].setWidth(TEXT_WIDTH);
        group.addActor(text[1]);

        return group;
    }


    /* Internal Events */

    private void selectEntity(Entity.Subtype subtype, Image image, float height){
        //update the image colors
        if(selectedImage != null) selectedImage.setColor(Color.GRAY);
        image.setColor(LIGHT_GRAY);
        selectedImage = image;

        //update the arrow
        selectionArrow.setVisible(true);
        selectionArrow.setY(height+7);

        //update the title text
        if(subtype instanceof Ship.Model){
            titleLabel.setText(((Ship.Model) subtype).name());
        }
        else if(subtype instanceof Station.Model){
            titleLabel.setText(((Station.Model) subtype).name());
        }
        Main.glyph.setText(titleLabel.getStyle().font, titleLabel.getText());
        titleLabel.setX(TEXT_WIDTH/2f-Main.glyph.width/2f);

        //update the body of the text
        String[] blurb = Asset.retrieveEntityBlurb(subtype).split("<>");
        for(int i=0; i< text.length; i++){
            if(blurb.length > i){
                text[i].setText(blurb[i].trim());
                if(i > 0){
                    Main.glyph.setText(text[i-1].getStyle().font, text[i-1].getText(), text[i-1].getColor(),
                            TEXT_WIDTH, text[i-1].getLabelAlign(), text[i-1].getWrap());
                    text[i].setY(text[i-1].getY() - 30 - Main.glyph.height);
                }
            }
            else {
                text[i].setText("");
            }
        }
    }
}
