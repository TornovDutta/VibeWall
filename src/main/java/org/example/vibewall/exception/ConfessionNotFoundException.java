package org.example.vibewall.exception;

public class ConfessionNotFoundException extends Exception{
    public ConfessionNotFoundException(){
        super("Confession is not found");
    }
    public ConfessionNotFoundException(String msg){
        super(msg);
    }
    public ConfessionNotFoundException(String msg, Throwable throwable){
        super(msg,throwable);
    }

}
