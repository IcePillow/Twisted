package com.twisted.net.msg.lobby;

import com.twisted.net.msg.Message;

/**
 * A message that represents a user sending a command from the terminal.
 *
 * /start - Starts the game
 * /name [requested name] - Attempts to rename this user. TODO test this
 */
public class MCommand implements Message {

    public String[] strings;


    /**
     * Constructor
     */
    public MCommand(String[] strings){
        this.strings = strings;

        this.strings[0] = this.strings[0].toLowerCase();
    }


    /* Serverside Utility */

    public int getCommandLength(){
        return strings.length;
    }


    /* Command Checking */

    public Type getType(){
        if(this.strings.length >= 1 && this.strings[0].equals("start")) return Type.START;
        else if(this.strings.length >= 2 && this.strings[0].equals("name")) return Type.NAME;
        else if(this.strings.length >= 1 && this.strings[0].equals("help")) return Type.HELP;
        else if(this.strings.length >= 2 && this.strings[0].equals("kick")) return Type.KICK;
        else return Type.NONE;
    }

    public enum Type {
        START,
        NAME,
        HELP,
        KICK,
        NONE,
    }

}
