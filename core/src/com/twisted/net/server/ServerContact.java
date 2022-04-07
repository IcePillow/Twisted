package com.twisted.net.server;

import com.twisted.net.msg.Message;

/**
 * Interface that provides the methods that the Server needs to communicate with a non-network
 * class, that will provide the logic for what messages are sent.
 */
public interface ServerContact {

    /**
     * Called when a message is received from a client that is not dealt with in the network sector.
     */
    void serverReceived(int clientId, Message message);

    /**
     * Called when a client connects to the server.
     */
    void clientConnected(int clientId);

    /**
     * Called when a client sends a disconnect message, closing the connection.
     */
    void clientDisconnected(int clientId, String reason);

    /**
     * Called when a client disconnects without sending Disconnect.
     */
    void clientLostConnection(int clientId);

}
