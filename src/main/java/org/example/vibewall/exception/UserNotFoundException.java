package org.example.vibewall.exception;

public class UserNotFoundException extends Exception{
    public UserNotFoundException(String msg){
        super(msg);

    }
    public UserNotFoundException(String msg,Throwable throwable){
        super(msg,throwable);
    }
    public UserNotFoundException(){
        super("user not found");
    }
}
