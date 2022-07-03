package com.twisted.local.game.util;

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
 *
 * Layout of a given row is horizontal
 * Name (100)
    * Grid tag (ship only)
    * Name of the entity
 */
public class FleetRow {

    //constants
    private final static float NAME_WIDTH = 100;

    //storage
    Entity entity;
    Group group;
    private SecFleet.TabType tabType;

    //high level graphics
    private final Skin skin;

    //usable actors (spaces are named by the actor they come after)
    private final Actor nameFill;
    private Label shipGridTag, nameLabel, positionLabel;

    /**
     * Constructor.
     * Loads all the graphical elements but does not actually add them to the parent group.
     */
    public FleetRow(Entity entity, Skin skin, Color color, SecFleet sector){
        this.entity = entity;
        this.skin = skin;

        //ship exclusive
        if(entity instanceof Ship){
            group = createShipDisplay((Ship) entity, color);
        }
        //station exclusive
        else if(entity instanceof Station){
            group = createStationDisplay((Station) entity, color);
        }

        //padding
        nameFill = new Actor();

        //listeners
        assert nameLabel != null;
        nameLabel.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                sector.entityNameClicked(entity);
            }
        });
    }


    /* Utility Methods */

    /**
     * Creates the display group for a ship entity.
     */
    private Group createShipDisplay(Ship ship, Color color){
        HorizontalGroup group = new HorizontalGroup();

        shipGridTag = new Label("[]", skin, "small", Color.LIGHT_GRAY);
        shipGridTag.setFontScale(0.85f);
        nameLabel = new Label(ship.getType().toString(), skin, "small", color);

        positionLabel = new Label("pos", skin, "small", Color.LIGHT_GRAY);

        return group;
    }

    /**
     * Creates the display group for a station entity.
     */
    private Group createStationDisplay(Station station, Color color){
        HorizontalGroup group = new HorizontalGroup();
        nameLabel = new Label(station.shortNickname, skin, "small", color);

        return group;
    }

    /**
     * Resizes the layout to correctly align the actors.
     */
    private void resizeLayout(SecFleet.TabType type){
        float width;

        //name filler
        if(entity instanceof Ship){
            if(type == SecFleet.TabType.Fleet){
                width = shipGridTag.getWidth() + nameLabel.getWidth();
            }
            else {
                width = nameLabel.getWidth();
            }

            nameFill.setWidth(NAME_WIDTH-width);
        }
        else if(entity instanceof Station){
            //TODO
        }

        this.tabType = type;
    }


    /* External Methods */

    /**
     * Updates the UI layout to display correctly based on the tab type. Does not update based on
     * game state.
     */
    public void switchDisplayType(SecFleet.TabType type){

        //remove all actors from the parent
        while(group.getChildren().size > 0){
            group.removeActorAt(0, true);
        }

        //add the actors to the parent based on case
        if(entity instanceof Ship){
            //name
            if(type == SecFleet.TabType.Fleet){
                group.addActor(shipGridTag);
                group.addActor(nameLabel);
            }
            else {
                group.addActor(nameLabel);
            }
            group.addActor(nameFill);

            //position
            group.addActor(positionLabel);
        }
        else if(entity instanceof Station){
            group.addActor(nameLabel);
        }

        //fix the layout
        resizeLayout(type);
    }

    /**
     * Updates the graphical values based on the state.
     */
    public void updateDisplay(GameState state, int entityGrid){
        if(entity instanceof Ship){
            //change the grid tag
            if(entityGrid >= 0){
                shipGridTag.setText("[" + state.grids[entityGrid].nickname + "]");
                positionLabel.setText(String.format("%.2f", entity.pos.len()));
            }
            else {
                shipGridTag.setText("[W]");
                positionLabel.setText("Warp");
            }

            //position
        }

        resizeLayout(tabType);
    }

}
