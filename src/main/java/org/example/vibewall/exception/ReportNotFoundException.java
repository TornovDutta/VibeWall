package org.example.vibewall.exception;

public class ReportNotFoundException extends Exception{
    public ReportNotFoundException(){
        super("report is not exist");
    }
    public ReportNotFoundException(String msg){
        super(msg);
    }
    public ReportNotFoundException(String msg,Throwable throwable){
        super(msg,throwable);
    }
}
