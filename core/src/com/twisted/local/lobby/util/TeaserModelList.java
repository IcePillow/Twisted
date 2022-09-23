package com.twisted.local.lobby.util;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.twisted.Asset;
import com.twisted.local.lobby.SecTeaser;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.ship.Ship;
import com.twisted.logic.entities.station.Station;

public class TeaserModelList extends Group {

    //copied fields
    private final Entity.Tier tier;
    private final SecTeaser sector;

    //created fields
    private Entity.Model currentModel;
    public Entity.Model getCurrentModel() {
        return currentModel;
    }

    private Image currentImage;
    public Image getCurrentImage() {
        return currentImage;
    }


    /* Methods */

    public TeaserModelList(Entity.Tier tier, SecTeaser sector){
        super();

        this.tier = tier;
        this.sector = sector;

        init();
    }

    private void init(){
        if(tier instanceof Ship.Tier){
            for(Ship.Model m : Ship.Model.values()){
                if(m.tier == tier){
                    //create the image
                    Image img = new Image(Asset.retrieveFactionIcon(m.faction));
                    img.setSize(32, 32);
                    img.setX(48*this.getChildren().size);
                    img.setColor(Color.GRAY);
                    this.addActor(img);

                    //check if it is first
                    if(currentModel == null){
                        currentModel = m;
                        currentImage = img;
                    }

                    //add the listener
                    img.addListener(new ClickListener(Input.Buttons.LEFT){
                        @Override
                        public void clicked(InputEvent event, float x, float y){
                            modelClicked(m, img);
                            event.handle();
                        }
                    });
                }
            }
        }
        else if(tier instanceof Station.Tier){
            for(Station.Model m : Station.Model.values()){
                if(m.tier == tier){
                    //create the image
                    Image img = new Image(Asset.retrieveEntityIcon(m));
                    img.setSize(32, 32);
                    img.setX(48*this.getChildren().size);
                    img.setColor(Color.GRAY);
                    this.addActor(img);

                    //check if it is first
                    if(currentModel == null){
                        currentModel = m;
                        currentImage = img;
                    }

                    //add the listener
                    img.addListener(new ClickListener(Input.Buttons.LEFT){
                        @Override
                        public void clicked(InputEvent event, float x, float y){
                            modelClicked(m, img);
                            event.handle();
                        }
                    });
                }
            }
        }
    }

    private void modelClicked(Entity.Model model, Image image){
        this.currentModel = model;
        this.currentImage = image;
        sector.selectEntityModel(model, image);
    }

}
