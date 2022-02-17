package com.twisted.net.server;

import com.twisted.net.msg.MDisconnect;
import com.twisted.net.msg.Message;

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
    public int getNextId() {
        return nextId;
    }
    private boolean listening;

    //client storage
    private HashMap<Integer, RepClient> clients;

    //server objects
    private ServerSocket serverSocket;


    /* Constructor */

    public Server(ServerContact contact){
        this.contact = contact;
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
     * Connects a new local client to the server
     */
    public void connectLocalClient(LocalClient client){
        clients.put(client.getId(), client);
        nextId++;
    }

    /**
     * Sends a message to a particular client.
     */
    public void sendMessage(int id, Message message){
        clients.get(id).writeToStream(message);
    }

    /**
     * Sends a message to all clients.
     */
    public void broadcastMessage(Message message){
        for(Integer id : clients.keySet()){
            sendMessage(id, message);
        }
    }

    /**
     * Kicks a particular client.
     */
    public void kickClient(int id){
        //send the disconnect message
        MDisconnect msg = new MDisconnect();
        msg.reasonText = "You have been kicked.";
        sendMessage(id, msg);

        clients.get(id).disconnect();
        clients.remove(id);
    }

    /**
     * Closes the server.
     */
    public void closeServer(){

        //stop adding new clients
        if(listening) stopListening();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //tell the clients
        MDisconnect message = new MDisconnect();
        message.reasonText = "Server closed";
        broadcastMessage(new MDisconnect());

        //close the client connections
        for(RepClient client : clients.values()){
            client.disconnect();
        }

        //clear the map
        clients.clear();
        clients = null;

    }


    /* Internal Methods */

    /**
     * Called by a Client when a message is received.
     */
    void messageReceived(int id, Message message){
        contact.serverReceived(id, message);
    }

    /**
     * Called when a client unexpectedly disconnects from the server. The Client object should
     * automatically close itself.
     */
    void lostConnection(int id){
        clients.remove(id);
        contact.clientLostConnection(id);
    }

}
