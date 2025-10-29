package org.example.vibewall.exception;

public class AdminNotFoundException extends Exception{
    public AdminNotFoundException(String msg){
        super(msg);
    }
    public AdminNotFoundException(){
        super("admin not found");
    }
    public AdminNotFoundException(String msg,Throwable throwable){
        super(msg,throwable);
    }


}
