package com.twisted.vis;

import com.twisted.net.msg.Message;

/**
 * This interface represents the communication from the graphical side (like Lobby or Game) to something
 * on the network/logical side (such as LobbyHost or Client).
 */
public interface ClientsideContact {

    void send(Message message);

    void shutdown();

}
