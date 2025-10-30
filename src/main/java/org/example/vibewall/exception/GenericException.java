package org.example.vibewall.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GenericException {
    @ExceptionHandler(AdminNotFoundException.class)
    public ResponseEntity<?> handleAdminNotFound(AdminNotFoundException adminNotFoundException){
        ErrorResponse errorResponse=new ErrorResponse(LocalDateTime.now(),adminNotFoundException.getMessage(),"admin " +
                "not found");
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException e){
        ErrorResponse errorResponse=new ErrorResponse(LocalDateTime.now(),e.getMessage(),"User not found");
        return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<?> handleReportNotFound(ReportNotFoundException e){
        ErrorResponse errorResponse=new ErrorResponse(LocalDateTime.now(),e.getMessage(),"no Report");
        return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception e){
        ErrorResponse errorResponse=new ErrorResponse(LocalDateTime.now(),e.getMessage(),"error by server");
        return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(ConfessionNotFoundException.class)
    public ResponseEntity<?> handleConfessionNotFound(ConfessionNotFoundException e){
        ErrorResponse errorResponse=new ErrorResponse(LocalDateTime.now(),e.getMessage(),"Confession not found");
        return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
    }
}
