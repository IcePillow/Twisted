package com.twisted.net.msg.remaining;

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

    public String getCommandType(){
        return strings[0];
    }

    /* Command Checking */

    public boolean isStart(){
        return (this.strings.length >= 1 && this.strings[0].equals("start"));
    }

    public boolean isName(){
        return (this.strings.length >= 2 && this.strings[0].equals("name"));
    }

}
