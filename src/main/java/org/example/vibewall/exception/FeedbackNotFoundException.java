package org.example.vibewall.exception;

public class FeedbackNotFoundException extends Exception{

    public FeedbackNotFoundException(String msg){
        super(msg);
    }
    public FeedbackNotFoundException(){
        super("feedback not found");
    }
    public FeedbackNotFoundException(String msg, Throwable throwable){
        super(msg,throwable);
    }
}
