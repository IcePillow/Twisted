package com.twisted.logic.descriptors.events;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.twisted.util.Asset;
import com.twisted.local.game.state.ClientGameState;

public class EvGameEnd extends GameEvent {

    public final int winnerId;

    public EvGameEnd(int winnerId){
        this.winnerId = winnerId;
    }

    @Override
    public GameEvent.Type getType(){
        return Type.GAME_END;
    }


    /* Utility */

    @Override
    public HorizontalGroup describeForCurtain(ClientGameState state){
        HorizontalGroup group = new HorizontalGroup();

        //there was a winner
        if(winnerId != -1){
            //name label
            Label label1 = new Label(state.players.get(winnerId).getName(), Asset.labelStyle(Asset.Avenir.MEDIUM_14));
            label1.setColor(state.players.get(winnerId).getCollect().base.c);
            group.addActor(label1);

            //text label
            Label label2 = new Label(" has won the game!", Asset.labelStyle(Asset.Avenir.MEDIUM_14));
            label2.setColor(Color.LIGHT_GRAY);
            group.addActor(label2);
        }
        //there was no winner
        else {
            //text label
            Label label = new Label("The game ended without a winner!", Asset.labelStyle(Asset.Avenir.MEDIUM_14));
            label.setColor(Color.LIGHT_GRAY);
            group.addActor(label);
        }

        return group;
    }

}
