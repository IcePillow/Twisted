package com.twisted.net.client;

import com.twisted.net.msg.MDisconnect;
import com.twisted.net.msg.Message;
import com.twisted.vis.ClientsideContact;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * The class that represents the client-side of the network connection.
 */
public class Client implements ClientsideContact {

    //exterior
    private ClientContact contact;
    public void setContact(ClientContact contact){
        this.contact = contact;
    }

    //state
    private boolean listen;

    //streams and sockets
    private Socket socket;
    private ObjectOutputStream output;


    /* Constructor */

    public Client(ClientContact contact, String hostname, int port){
        this.contact = contact;

        new Thread(() -> {
            socket = new Socket();

            try {
                socket.connect(new InetSocketAddress(hostname, port), 5*1000);

                //streams
                output = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

                //start conversing
                converse(input);

                //tell contact
                contact.connectedToServer();

            } catch (IOException e) {
                //send message back to lobby
                contact.failedToConnect();

                e.printStackTrace();
            }


        }).start();

    }


    /* Outwards Methods */

    /**
     * Send a message over the network to the server.
     */
    public void send(Message message){
        try {
            output.writeObject(message);
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

        listen = false;

        //send disconnect and close socket
        if(socket.isConnected()){
            send(new MDisconnect());

            //close socket
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /* Internal Methods */

    private void converse(ObjectInputStream input){
        listen = true;

        new Thread(() -> {
            while(listen){
                try {
                    Message message = (Message) input.readObject();

                    if(message instanceof MDisconnect){
                        listen = false;
                        contact.kickedFromServer(message);
                    }
                    else {
                        contact.clientReceived(message);
                    }
                }
                catch(EOFException | SocketException e){
                    //intentionally left
                    if(listen) {
                        contact.lostConnectionToServer();
                        listen = false;
                    }
                    break;
                }
                catch(IOException e) {
                    e.printStackTrace();
                    contact.lostConnectionToServer();
                    listen = false;
                    break;
                }
                catch(ClassNotFoundException e){
                    e.printStackTrace();
                    break;
                }
            }

            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
