package org.example.vibewall.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.vibewall.security.CustomUserDetails;
import org.example.vibewall.service.SystemLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@RequiredArgsConstructor
public class GenericException {

    private final SystemLogService systemLogService;

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<?> handleBadRequest(RuntimeException e, HttpServletRequest req) {
        logError("WARN", "API", req, HttpStatus.BAD_REQUEST.value(), e.getMessage(), "bad request");
        return build(e.getMessage(), "bad request", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            AdminNotFoundException.class,
            UserNotFoundException.class,
            ReportNotFoundException.class,
            ConfessionNotFoundException.class,
            FeedbackNotFoundException.class,
            PollNotFoundException.class
    })
    public ResponseEntity<?> handleNotFound(Exception e, HttpServletRequest req) {
        logError("WARN", "API", req, HttpStatus.NOT_FOUND.value(), e.getMessage(), "resource not found");
        return build(e.getMessage(), "resource not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
            PlatformMisuseException.class,
            UnsafeContentException.class
    })
    public ResponseEntity<?> handlePolicyViolation(RuntimeException e, HttpServletRequest req) {
        logError("WARN", "MODERATION", req, HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getMessage(), "content policy violation");
        return build(e.getMessage(), "content policy violation", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(AiUnavailableException.class)
    public ResponseEntity<?> handleAiUnavailable(AiUnavailableException e, HttpServletRequest req) {
        logError("CRITICAL", "AI_SERVICE", req, HttpStatus.SERVICE_UNAVAILABLE.value(), e.getMessage(), "ai system down");
        return build(e.getMessage(), "ai system down", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?> handleInvalidCredentials(InvalidCredentialsException e, HttpServletRequest req) {
        logError("WARN", "AUTH", req, HttpStatus.UNAUTHORIZED.value(), e.getMessage(), "authentication failed");
        return build(e.getMessage(), "authentication failed", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException e, HttpServletRequest req) {
        logError("WARN", "AUTH", req, HttpStatus.FORBIDDEN.value(), e.getMessage(), "access denied");
        return build(e.getMessage(), "access denied", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e, HttpServletRequest req) {
        String detailMsg = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "Validation failed";
        logError("WARN", "VALIDATION", req, HttpStatus.BAD_REQUEST.value(), "Validation failed: " + detailMsg, detailMsg);
        return build("validation failed", detailMsg, HttpStatus.BAD_REQUEST);
    }

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GenericException.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception e, HttpServletRequest req) {
        log.error("Unhandled exception: {}", e.getMessage(), e);
        String details = e.getClass().getSimpleName() + ": " + (e.getMessage() != null ? e.getMessage() : "Unexpected error");
        logError("ERROR", "SERVER", req, HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred: " + e.getMessage(), details);
        return build("An unexpected error occurred", "server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void logError(String level, String source, HttpServletRequest req, int statusCode, String message, String details) {
        try {
            String path = (req != null) ? req.getMethod() + " " + req.getRequestURI() : "UNKNOWN";
            String userId = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
                userId = ((CustomUserDetails) auth.getPrincipal()).getId();
            }
            if (systemLogService != null) {
                systemLogService.recordLog(level, source, path, statusCode, userId, message, details);
            }
        } catch (Exception ex) {
            log.error("Failed to record exception to system log: {}", ex.getMessage());
        }
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
