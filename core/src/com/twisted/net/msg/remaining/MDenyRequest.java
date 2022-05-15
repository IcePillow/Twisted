package com.twisted.net.msg.remaining;

import com.twisted.net.msg.Message;
import com.twisted.net.msg.gameRequest.MGameRequest;

public class MDenyRequest implements Message {

    //request being denied
    public final MGameRequest request;

    /**
     * Use depends on the request being denied.
     */
    public int integer;
    /**
     * Message to be displayed to the user.
     */
    public String reason;


    public MDenyRequest(MGameRequest request){
        this.request = request;
        this.reason = "";
    }

}
