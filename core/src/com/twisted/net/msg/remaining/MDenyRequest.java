package com.twisted.net.msg.remaining;

import com.twisted.net.msg.Message;
import com.twisted.net.msg.gameReq.MGameReq;

public class MDenyRequest implements Message {

    //request being denied
    public final MGameReq request;

    /**
     * Use depends on the request being denied.
     */
    public int integer;
    /**
     * Message to be displayed to the user.
     */
    public String reason;


    public MDenyRequest(MGameReq request){
        this.request = request;
        this.reason = "";
    }

}
