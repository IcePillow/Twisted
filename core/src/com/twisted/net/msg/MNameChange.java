package com.twisted.net.msg;

public class MNameChange implements Message{

    public String name;

    public MNameChange(String name){
        this.name = name;
    }

}
