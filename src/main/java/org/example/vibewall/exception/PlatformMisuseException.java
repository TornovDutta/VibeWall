package org.example.vibewall.exception;

public class PlatformMisuseException extends Exception{
    public PlatformMisuseException(String msg){
        super(msg);
    }
    public PlatformMisuseException(String msg,Throwable throwable){
        super(msg,throwable);
    }
    public PlatformMisuseException(){
        super("don't miss use this platform");
    }
}
