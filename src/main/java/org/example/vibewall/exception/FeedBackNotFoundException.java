package org.example.vibewall.exception;

public class FeedBackNotFoundException extends Exception{

    public FeedBackNotFoundException(String msg){
        super(msg);
    }
    public FeedBackNotFoundException(){
        super("feedback not found");
    }
    public FeedBackNotFoundException(String msg,Throwable throwable){
        super(msg,throwable);
    }
}
