package com.twisted.logic.descriptors.events;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
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
    public HorizontalGroup displayForCurtain(ClientGameState state, Skin skin){
        HorizontalGroup group = super.displayForCurtain(state, skin);

        //name label
        Label label1 = new Label(state.players.get(winnerId).getName(), skin, "small", Color.WHITE);
        label1.setColor(state.players.get(winnerId).getFile().color);
        group.addActor(label1);

        //text label
        Label label2 = new Label(" has won the game!", skin, "small", Color.WHITE);
        label2.setColor(Color.LIGHT_GRAY);
        group.addActor(label2);

        return group;
    }

}
