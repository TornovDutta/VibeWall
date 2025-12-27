package org.example.vibewall.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GenericException {

    @ExceptionHandler(AdminNotFoundException.class)
    public ResponseEntity<?> handleAdminNotFound(AdminNotFoundException e) {
        return build(e.getMessage(), "admin not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException e) {
        return build(e.getMessage(), "user not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<?> handleReportNotFound(ReportNotFoundException e) {
        return build(e.getMessage(), "no report", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConfessionNotFoundException.class)
    public ResponseEntity<?> handleConfessionNotFound(ConfessionNotFoundException e) {
        return build(e.getMessage(), "confession not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FeedbackNotFoundException.class)
    public ResponseEntity<?> handleFeedbackNotFound(FeedbackNotFoundException e) {
        return build(e.getMessage(), "no feedback", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PrincipalNotFollowException.class)
    public ResponseEntity<?> missUse(PrincipalNotFollowException e) {
        return build(e.getMessage(), "sorry you cannot post it", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception e) {
        return build(e.getMessage(), "server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(UnSafeExecption.class)
    public ResponseEntity<?> handleUnsafe(UnSafeExecption e) {
        return build(e.getMessage(), "voilated the principal", HttpStatus.INTERNAL_SERVER_ERROR);
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
