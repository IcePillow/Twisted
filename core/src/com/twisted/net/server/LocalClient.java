package com.twisted.net.server;

import com.twisted.net.client.ClientContact;
import com.twisted.net.msg.Message;

/**
 * A serverside representation of the connection between a local user and the server.
 * It's messages go like
 *
 * [client]                       [server]
 * SceneHost -> LocalClient -> Server
 * or
 * SceneHost <- LocalClient <- Server
 */
public class LocalClient implements RepClient {

    //outward references
    private Server server;
    private ClientContact contact;
    public void setContact(ClientContact contact) {
        this.contact = contact;
    }

    //id
    private final int id;

    //state
    private boolean disconnected;


    /* Constructor */

    public LocalClient(Server server, ClientContact contact, int id){
        this.server = server;
        this.contact = contact;
        this.id = id;

        this.disconnected = false;
    }


    /* Local Methods */

    public void send(Message message){
        if(!disconnected) server.messageReceived(id, message);
    }


    /* RepClient Methods (serverside) */

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void writeToStream(Message message) {

        if(!disconnected) contact.clientReceived(message);
    }

    @Override
    public void disconnect() {
        disconnected = true;
    }
}
