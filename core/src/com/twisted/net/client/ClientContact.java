package com.twisted.net.client;

import com.twisted.net.msg.Message;

/**
 * This is a clientside, graphical side class (such as Lobby or Game). This interfaces represents
 * the communication from the network-sector Client object to this graphical side.
 */
public interface ClientContact {

    void connectedToServer();

    /**
     * Called if the client never establishes a connection.
     */
    void failedToConnect();

    /**
     * Called when a message is received from the server that is not dealt with my the network
     * sector.
     */
    void clientReceived(Message message);

    /**
     * Called when purposefully kicked from the server.
     */
    void kickedFromServer(Message message);

    /**
     * Called when something went wrong to lose the server connection.
     */
    void lostConnectionToServer();

}
