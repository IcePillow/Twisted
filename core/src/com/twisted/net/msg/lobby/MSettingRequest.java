package com.twisted.net.msg.lobby;

import com.twisted.logic.host.lobby.MatchSettings;
import com.twisted.net.msg.Message;

/**
 * Sent from client to user as a request to change the current settings.
 */
public class MSettingRequest implements Message {

    public final MatchSettings.Type type;
    public final boolean forward;


    public MSettingRequest(MatchSettings.Type type, boolean forward){
        this.type = type;
        this.forward = forward;
    }


}
