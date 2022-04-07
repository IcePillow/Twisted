package com.twisted.net.msg.remaining;

import com.twisted.net.msg.Message;

/**
 * A message that represents a user sending a chat message to other users.
 */
public class MChat implements Message {

    /**
     * Text to be shown
     */
    public String string;

    public Type type;


    /**
     * Constructor
     * @param string the text to be shown
     */
    public MChat(Type type, String string){
        this.string = string;
        this.type = type;
    }

    /**
     * Mostly used on the clientside to determine how to display something.
     */
    public enum Type {
        //a default type
        NONE,
        //chats from one player to another
        PLAYER_CHAT,
        //logistical messages
        LOGISTICAL,
        //a warning or an error
        WARNING_ERROR,
    }


}
