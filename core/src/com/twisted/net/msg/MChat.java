package com.twisted.net.msg;

/**
 * A message that represents a user sending a chat message to other users.
 */
public class MChat implements Message{

    /**
     * Use 0 to send to everyone.
     */
    public int recipient;

    /**
     * Text to be shown
     */
    public String string;


    /**
     * Constructor
     * @param recipient 0 for everyone, otherwise clientId
     * @param string the text to be shown
     */
    public MChat(int recipient, String string){
        this.recipient = recipient;
        this.string = string;
    }


}
