package com.twisted.net.server;

import com.twisted.net.msg.Message;

/**
 * Interface that contains all methods needed for the Server to interact with either a LocalClient
 * or a NetworkClient (except for the constructor).
 */
interface RepClient {

    int getId();

    void writeToStream(Message message);

    void disconnect();

}
