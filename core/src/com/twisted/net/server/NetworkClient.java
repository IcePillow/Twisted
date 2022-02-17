package com.twisted.net.server;

import com.twisted.net.msg.MDisconnect;
import com.twisted.net.msg.Message;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * A representation of a Client connection to a Server on the server-side.
 */
public class NetworkClient implements RepClient {

    //outward references
    private Server server;

    //id
    private final int id;

    //state
    private boolean listen;

    //streams
    private ObjectOutputStream output;
    private ObjectInputStream input;


    /* Constructor */

    public NetworkClient(Server server, int id, Socket socket) throws IOException {
        //set directly
        this.id = id;
        this.server = server;
        this.listen = true;

        //streams
        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(socket.getInputStream());

        //listening
        new Thread(() -> {

            while(listen){
                try {
                    Message message = (Message) input.readObject();

                    if(message instanceof MDisconnect) listen = false;

                    server.messageReceived(id, message);
                }
                catch(EOFException | SocketException e){
                    server.lostConnection(id);
                    lostConnectionShutdown();
                    break;
                }
                catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }


    /* Utility Methods */

    private void lostConnectionShutdown(){

        listen = false;
        try {
            input.close();
            output.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    /* Client Methods */

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void writeToStream(Message message) {
        try {
            output.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        listen = false;

        try {
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
