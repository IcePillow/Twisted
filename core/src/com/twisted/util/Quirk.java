package com.twisted.util;

public class Quirk extends Exception {

    //constants
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    //data
    private final String formatId;
    private final String description;

    //constructors
    private Quirk(int id, String description){
        this.formatId = String.format("%03d", id);
        this.description = description;
    }
    public Quirk(Q q){
        this(q.id, q.d);
    }


    /* Output Methods */

    public void print(){
        printDescription();
        printTrace();
    }
    public void printWithMessage(String string){
        printDescription();
        System.out.println(ANSI_YELLOW + "    " + string + ANSI_RESET);
        printTrace();
    }

    private void printDescription(){
        System.out.println(ANSI_YELLOW + "Quirk " + formatId + ": " + description + ANSI_RESET);
    }
    private void printTrace(){
        boolean first = true;
        for(StackTraceElement s : this.getStackTrace()){
            if(first) {
                System.out.println(ANSI_RED + s.toString() + ANSI_RESET);
                first = false;
            }
            else {
                System.out.println(ANSI_RED + "\t" + s.toString() + ANSI_RESET);
            }
        }
    }


    /* Enums */

    public enum Q {
        Empty(0, "Reserved"),
        Unexpected(1, "Unexpected state"),
        Inaccessible(2, "An inaccessible state has been accessed"),

        UnknownGameData(11, "Unknown game data was provided"),
        NetworkIllegalGameState(12, "Attempting to create an illegal game state based on message from network"),
        MissingDataAfterInput(13, "User input requires data that is missing"),
        IncorrectClassType(14, "Unable to cast object to twisted-specified type"),
        UnknownClientDataSpecification(15, "Client specified data of an unknown type to server"),
        MessageFromClientImprecise(16, "Message from client is missing data or is has uninterpretable data or state is not synchronized over network"),
        UnexpectedMessageAtThisTime(17, "Received message of a type that is not expected at this time"),
        ErrorDuringThreadSleep(18, "Error during thread sleep"),
        MessageFromClientMismatch(19, "Message from client received that does not match serverside game state")
        ;

        //data
        public final int id; //the id of the quirk
        public final String d; //the description of the quirk

        //constructor
        Q(int id, String d){
            this.id = id;
            this.d = d;
        }
    }

}
