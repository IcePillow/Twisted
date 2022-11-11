package com.twisted.net.msg;

import com.twisted.local.game.cosmetic.Cosmetic;
import com.twisted.local.game.state.ClientGameState;

import java.util.List;

public interface CosmeticCheckable {

    List<Cosmetic> createNewCosmetics(ClientGameState state);

}
