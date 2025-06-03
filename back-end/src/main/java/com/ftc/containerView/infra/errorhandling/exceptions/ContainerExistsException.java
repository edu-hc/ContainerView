package com.ftc.containerView.infra.errorhandling.exceptions;

public class ContainerExistsException extends RuntimeException {

    public ContainerExistsException() {
        super("Container already exists");
    }
    public ContainerExistsException(String message) {
        super(message);
    }
}
