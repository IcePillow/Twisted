package com.twisted.net.server;

import com.twisted.net.msg.Disconnect;
import com.twisted.net.msg.Message;
import com.twisted.net.msg.Transmission;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;

public class Server {

    //contact
    private ServerContact contact;
    public void setContact(ServerContact contact){
        this.contact = contact;
    }

    //state tracking
    private int nextId;
    public int useNextId() {
        nextId++;
        return nextId-1;
    }
    private boolean listening;
    private boolean shutdown;
    boolean getShutdown(){
        return shutdown;
    }

    //client storage
    private HashMap<Integer, NetworkClient> clients;

    //server objects
    private ServerSocket serverSocket;


    /* Constructor */

    public Server(ServerContact contact){
        this.contact = contact;
        this.shutdown = false;
        nextId = 1;
        clients = new HashMap<>();
    }


    /* Outward Facing Methods */

    /**
     * Starts listening for clients to connect.
     * @return The port being listened on. Returns -1 if error.
     */
    public int startListening(){

        try {
            serverSocket = new ServerSocket(0);
            serverSocket.setSoTimeout(5000);
        }
        catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        listening = true;
        new Thread(() -> {

            while(listening){
                try {
                    //get the socket
                    Socket socket = serverSocket.accept();

                    NetworkClient client = new NetworkClient(this, nextId, socket);
                    clients.put(nextId, client);

                    //tell the contact
                    contact.clientConnected(nextId);

                    //increment id
                    nextId++;
                }
                catch (SocketTimeoutException | SocketException e){
                    assert true;
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                serverSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }).start();

        return serverSocket.getLocalPort();
    }

    /**
     * Stops listening for clients to connect.
     */
    public void stopListening(){
        listening = false;
    }

    /**
     * Sends a message to a particular client.
     */
    public void sendMessage(int id, Transmission t){
        clients.get(id).writeToStream(t);
    }

    /**
     * Sends a message to all clients.
     */
    public void broadcastMessage(Transmission t){
        for(Integer id : clients.keySet()){
            sendMessage(id, t);
        }
    }

    /**
     * Utility method to send a message to all clients save one.
     */
    public void sendMessageToAllButOne(int excludedId, Transmission t){
        for(NetworkClient client : clients.values()){
            if(client.getId() != excludedId) sendMessage(client.getId(), t);
        }
    }

    /**
     * Kicks a particular client.
     */
    public void kickClient(int id, String reason){
        //send the disconnect message
        Disconnect msg = new Disconnect();
        msg.reasonText = reason;
        sendMessage(id, msg);

        //clean up the client
        clients.get(id).shutdown();
        clients.remove(id);
    }

    /**
     * Closes the server.
     */
    public void closeServer(){
        //set shutdown flag
        shutdown = true;

        //stop adding new clients
        if(listening) stopListening();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //tell the clients
        Disconnect disconnect = new Disconnect();
        disconnect.reasonText = "Server closed";
        broadcastMessage(disconnect);

        //close the client connections
        for(NetworkClient client : clients.values()){
            client.shutdown();
        }

        //clear the map
        clients.clear();
        clients = null;
    }


    /* Internal Methods */

    /**
     * Called by a NetworkClient when a message is received.
     */
    void messageReceived(int id, Message message){
        contact.serverReceived(id, message);
    }

    /**
     * Called when a client disconnects cleanly.
     */
    void disconnectedFrom(int id, String reason){
        contact.clientDisconnected(id, reason);
    }

    /**
     * Called when a client loses connection with the server.
     */
    void lostConnectionWith(int id){
        if(clients != null) clients.remove(id);
        contact.clientLostConnection(id);
    }

}
