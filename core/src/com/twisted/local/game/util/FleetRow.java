package com.twisted.local.game.util;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.twisted.local.game.SecFleet;
import com.twisted.local.game.state.GameState;
import com.twisted.logic.entities.Entity;
import com.twisted.logic.entities.Ship;
import com.twisted.logic.entities.Station;

/**
 * Class that handles the visuals of a row.
 */
public class FleetRow {

    //storage
    Entity entity;
    Group group;

    //high level graphics
    private final Skin skin;

    //usable actors
    private Label shipGridTag;
    private Label nameLabel;


    /**
     * Constructor.
     * Loads all the graphical elements but does not actually add them to the parent group.
     */
    public FleetRow(Entity entity, Skin skin, Color color, SecFleet sector){
        this.entity = entity;
        this.skin = skin;

        //ship exclusive
        if(entity instanceof Ship){
            group = createShipDisplay((Ship) entity, color, sector);
        }
        //station exclusive
        else if(entity instanceof Station){
            group = createStationDisplay((Station) entity, color, sector);
        }

        //general
        assert nameLabel != null;
        nameLabel.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                sector.entityNameClicked(entity);
            }
        });
    }


    /* Utility Methods */

    private Group createShipDisplay(Ship ship, Color color, SecFleet sector){
        HorizontalGroup group = new HorizontalGroup();
        shipGridTag = new Label("[]", skin, "small", Color.LIGHT_GRAY);
        shipGridTag.setFontScale(0.8f);
        nameLabel = new Label(ship.getType().toString(), skin, "small", color);

        return group;
    }

    private Group createStationDisplay(Station station, Color color, SecFleet sector){
        HorizontalGroup group = new HorizontalGroup();
        nameLabel = new Label(station.nickname, skin, "small", color);

        return group;
    }


    /* External Methods */

    public void switchDisplayType(SecFleet.TabType type){
        //remove all actors from the parent
        while(group.getChildren().size > 0){
            group.removeActorAt(0, true);
        }

        //add the actors to the parent based on case
        if(entity instanceof Ship){
            if(type == SecFleet.TabType.Fleet){
                group.addActor(shipGridTag);
                group.addActor(nameLabel);
            }
            else {
                group.addActor(nameLabel);
            }
        }
        else if(entity instanceof Station){
            group.addActor(nameLabel);
        }
    }

    public void updateDisplay(GameState state, int entityGrid){
        if(entity instanceof Ship){
            //change the grid tag
            if(entityGrid >= 0){
                shipGridTag.setText("[" + state.grids[entityGrid].nickname + "]");
            }
            else {
                shipGridTag.setText("[W]");
            }
        }
    }

}
