package com.ftc.containerView.infra.errorhandling.exceptions;

public class ContainerNotFoundException extends RuntimeException {

    public ContainerNotFoundException() {
        super("Container not found");
    }
    public ContainerNotFoundException(String message) {
        super(message);
    }
}
