package com.twisted.net.msg;

/**
 * A message that represents a user sending a command.
 *
 * /start - Starts the game
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
        return (this.strings[0].equals("start"));
    }

}
