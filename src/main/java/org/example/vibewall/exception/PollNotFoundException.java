package org.example.vibewall.exception;

public class PollNotFoundException extends RuntimeException {
    public PollNotFoundException() {
        super("Poll not found");
    }
    public PollNotFoundException(String message) {
        super(message);
    }
}
