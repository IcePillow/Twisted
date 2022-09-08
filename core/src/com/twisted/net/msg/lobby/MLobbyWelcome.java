package com.twisted.net.msg.lobby;

import com.twisted.logic.host.lobby.MatchSettings;
import com.twisted.net.msg.Message;

public class MLobbyWelcome implements Message {

    public final int yourId;
    public final int hostId;

    public MatchSettings settings;

    //hashmap wasn't working so this is what we have
    public final int[] playerIdList;
    public final String[] playerNameList;

    public MLobbyWelcome(int yourId, int hostId, int numPlayers, MatchSettings settings){
        this.yourId = yourId;
        this.hostId = hostId;

        this.playerIdList = new int[numPlayers];
        this.playerNameList = new String[numPlayers];

        this.settings = settings;
    }

}
