package com.twisted.net.msg.lobby;

import com.twisted.logic.host.lobby.MatchSettings;
import com.twisted.net.msg.Message;

public class MSettingChange implements Message {

    public final MatchSettings.Type type;
    public final MatchSettings.Setting value;


    public MSettingChange(MatchSettings.Type type, MatchSettings.Setting value){
        this.type = type;
        this.value = value;
    }

}
