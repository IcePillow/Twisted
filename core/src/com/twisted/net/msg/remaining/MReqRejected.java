package com.twisted.net.msg.remaining;

import com.twisted.net.msg.Message;
import com.twisted.net.msg.gameRequest.MGameRequest;

/**
 * Sent from server to client to say that a request message was rejected.
 */
public class MReqRejected implements Message {

    public final MGameRequest rejectedRequest;

    public MReqRejected(MGameRequest rejectedRequest){
        this.rejectedRequest = rejectedRequest;
    }

}
