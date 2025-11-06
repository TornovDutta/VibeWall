package org.example.vibewall.exception;

public class PrincipalNotFollowException extends Exception{
    public PrincipalNotFollowException(String msg){
        super(msg);
    }
    public PrincipalNotFollowException(String msg,Throwable throwable){
        super(msg,throwable);
    }
    public PrincipalNotFollowException(){
        super("don't miss use this platform");
    }
}
