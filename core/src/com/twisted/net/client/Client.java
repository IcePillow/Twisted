package com.twisted.net.client;

import com.twisted.net.msg.Disconnect;
import com.twisted.net.msg.Message;
import com.twisted.net.msg.Transmission;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * The class that represents the client-side of the network connection.
 */
public class Client {

    //exterior
    private ClientContact contact;
    public void setContact(ClientContact contact){
        this.contact = contact;
    }

    //state
    private boolean listen;
    private boolean closing;

    //streams and sockets
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;


    /* Constructor */

    public Client(ClientContact contact, String hostname, int port){
        this.contact = contact;

        closing = false;

        new Thread(() -> {
            socket = new Socket();

            try {
                socket.connect(new InetSocketAddress(hostname, port), 5*1000);

                //streams
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());

                //start conversing
                converse(input);

                //tell contact
                contact.connectedToServer();

            }
            catch (IOException | IllegalArgumentException  e) {
                //send message back
                contact.failedToConnect();
            }

        }).start();

    }


    /* Outwards Methods */

    /**
     * Send a message over the network to the server.
     */
    public void send(Transmission t){
        try {
            output.writeObject(t);
        }
        catch (IOException e) {
            //ignore exceptions
            assert true;
        }
    }

    /**
     * Disconnect from the server, if connected, and close everything of the client's.
     */
    public void shutdown(){
        //send disconnect
        if(socket.isConnected()){
            send(new Disconnect());
        }

        closing = true;
        listen = false;
    }


    /* Internal Methods */

    private void converse(ObjectInputStream input){
        listen = true;

        new Thread(() -> {
            while(listen){
                try {
                    Transmission transmission = (Transmission) input.readObject();

                    //handle non-messages
                    if(transmission instanceof Disconnect) disconnected((Disconnect) transmission);
                    //tell higher ups about messages
                    else contact.clientReceived((Message) transmission);
                }
                catch(EOFException | SocketException e){
                    if(!closing) lostConnection();
                    break;
                }
                catch(IOException e) {
                    e.printStackTrace();
                    if(!closing) lostConnection();
                    break;
                }
                catch(ClassNotFoundException e){
                    e.printStackTrace();
                    break;
                }
            }

            //close the socket
            try {
                if(socket.isConnected()) socket.close();
            }
            catch (IOException ignored) {}

            //close the streams
            try {
                output.close();
                input.close();
            }
            catch (IOException ignored) {}
        }).start();
    }

    private void disconnected(Disconnect disconnect){
        listen = false;
        closing = true;
        contact.disconnected(disconnect.reasonText);
    }

    private void lostConnection(){
        listen = false;
        closing = true;
        contact.lostConnection();
    }

}
