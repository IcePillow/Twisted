package com.twisted.net.server;

import com.twisted.net.msg.Disconnect;
import com.twisted.net.msg.Message;
import com.twisted.net.msg.Transmission;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * A representation of a Client connection to a Server on the server-side.
 */
public class NetworkClient {

    //outward references
    private final Server server;

    //id
    private final int id;

    //state
    private boolean listen;
    private boolean closing;

    //streams
    private final ObjectOutputStream output;
    private final ObjectInputStream input;


    /* Constructor */

    public NetworkClient(Server server, int id, Socket socket) throws IOException {
        //set directly
        this.id = id;
        this.server = server;
        this.listen = true;
        this.closing = false;

        //streams
        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(socket.getInputStream());

        //listening
        new Thread(() -> {

            while(listen){
                try {
                    Transmission transmission = (Transmission) input.readObject();

                    //handle non-messages
                    if(transmission instanceof Disconnect) disconnected((Disconnect) transmission);
                    //tell server if a message was received
                    else server.messageReceived(id, (Message) transmission);
                }
                catch(EOFException | SocketException e){
                    if(!closing) lostConnection();
                    break;
                }
                catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            //close the socket
            try {
                socket.close();
            }
            catch (IOException ignored) {}

            //close streams
            try {
                output.close();
                input.close();
            }
            catch (IOException ignored) {}
        }).start();
    }


    /* Utility Methods */

    /**
     * Called when a disconnect message is received.
     */
    private void disconnected(Disconnect disconnect) {
        listen = false;
        closing = true;

        server.disconnectedFrom(id, disconnect.reasonText);
    }

    /**
     * Called when connection is lost unexpectedly.
     */
    private void lostConnection() {
        listen = false;
        closing = true;

        server.lostConnectionWith(id);
    }


    /* Client Representation Methods */

    public int getId() {
        return id;
    }

    public void writeToStream(Transmission transmission) {
        try {
            output.writeObject(transmission);
        } catch (IOException e) {
            if(!server.getShutdown() && !closing) e.printStackTrace();
        }
    }

    /**
     * Called when the server initiates a disconnect.
     */
    public void shutdown(){
        //stop listening and close streams
        listen = false;
        closing = true;
        try {
            output.close();
            input.close();
        }
        catch (IOException ignored) {}
    }


}
