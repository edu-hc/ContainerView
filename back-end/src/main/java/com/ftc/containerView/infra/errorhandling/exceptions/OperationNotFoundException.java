package com.ftc.containerView.infra.errorhandling.exceptions;

public class OperationNotFoundException extends RuntimeException {

    public OperationNotFoundException() {
        super("Operation not found");
    }
    public OperationNotFoundException(String message) {
        super(message);
    }
}
