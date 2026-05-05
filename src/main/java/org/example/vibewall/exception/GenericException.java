package org.example.vibewall.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GenericException {

    @ExceptionHandler({
            AdminNotFoundException.class,
            UserNotFoundException.class,
            ReportNotFoundException.class,
            ConfessionNotFoundException.class,
            FeedbackNotFoundException.class
    })
    public ResponseEntity<?> handleNotFound(Exception e) {
        return build(e.getMessage(), "resource not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
            PrincipalNotFollowException.class,
            UnSafeExecption.class
    })
    public ResponseEntity<?> handlePolicyViolation(RuntimeException e) {
        return build(e.getMessage(), "content policy violation",
                HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException e) {
        return build(e.getMessage(), "access denied", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e) {
        return build(
                "validation failed",
                e.getBindingResult().getFieldError().getDefaultMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception e) {
        return build(e.getMessage(), "server error",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> build(
            String message,
            String details,
            HttpStatus status
    ) {
        return new ResponseEntity<>(
                new ErrorResponse(LocalDateTime.now(), message, details),
                status
        );
    }
}
