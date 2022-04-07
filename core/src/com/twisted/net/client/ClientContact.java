package com.twisted.net.client;

import com.twisted.net.msg.Message;

/**
 * This is a clientside, graphical side class (such as Lobby or Game). This interfaces represents
 * the communication from the network-sector Client object to this graphical side.
 */
public interface ClientContact {

    /**
     * Called when the client connects to the server.
     */
    void connectedToServer();

    /**
     * Called if the client never establishes a connection.
     */
    void failedToConnect();

    /**
     * Called when a message is received from the server.
     */
    void clientReceived(Message message);

    /**
     * Called when formally disconnected (receives a Disconnect message).
     */
    void disconnected(String reason);

    /**
     * Called when the server disconnects without sending Disconnect.
     */
    void lostConnection();

}
