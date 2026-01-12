package com.ftc.containerView.infra.errorhandling.exceptions;

public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException() { super("Unauthorized access"); }
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
