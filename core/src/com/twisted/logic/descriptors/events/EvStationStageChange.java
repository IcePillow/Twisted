package com.twisted.logic.descriptors.events;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.twisted.Asset;
import com.twisted.local.game.state.ClientGameState;
import com.twisted.logic.entities.station.Station;

/**
 * Represents a station changing stage but only for specific stage changes
    * Vulnerable -> Rubble
    * Shielded -> Armored
    * Rubble -> Armored
 */
public class EvStationStageChange extends GameEvent {

    public final int stationId;

    public final Station.Stage prevStage;
    public final int prevOwnerId;

    public final Station.Stage nextStage;
    public final int nextOwnerId;


    public EvStationStageChange(int stationId, Station.Stage prevStage, int prevOwnerId,
                                Station.Stage nextStage, int nextOwnerId){
        this.stationId = stationId;
        this.prevStage = prevStage;
        this.prevOwnerId = prevOwnerId;
        this.nextStage = nextStage;
        this.nextOwnerId = nextOwnerId;
    }

    @Override
    public GameEvent.Type getType(){
        return Type.STATION_STAGE_CHANGE;
    }


    /* Utility */

    @Override
    public HorizontalGroup displayForCurtain(ClientGameState state, Skin skin){
        HorizontalGroup group = super.displayForCurtain(state, skin);

        //prev image
        Image image1 = new Image(Asset.retrieveEntityIcon(Station.Model.Extractor));
        image1.setColor(state.findColorForOwner(prevOwnerId));
        group.addActor(image1);

        //prev name label
        Label label1 = new Label(" " + state.grids[stationId].station.getFullName(), Asset.labelStyle(Asset.Avenir.MEDIUM_14));
        label1.setColor(state.findColorForOwner(prevOwnerId));
        group.addActor(label1);

        //descriptive label
        Label label2 = new Label("", Asset.labelStyle(Asset.Avenir.MEDIUM_14));
        label2.setColor(Color.LIGHT_GRAY);
        switch(prevStage){
            case VULNERABLE: {
                label2.setText(" was reduced to rubble");
                break;
            }
            case SHIELDED: {
                label2.setText(" had its shields broken");
                break;
            }
            case RUBBLE: {
                label2.setText(" was rebuilt by ");
                break;
            }
        }
        group.addActor(label2);

        //next name label
        if(prevStage == Station.Stage.RUBBLE){
            Label label3 = new Label(state.players.get(nextOwnerId).getName(), skin, "small", Color.WHITE);
            label3.setColor(state.findColorForOwner(nextOwnerId));
            group.addActor(label3);
        }

        return group;
    }

}
