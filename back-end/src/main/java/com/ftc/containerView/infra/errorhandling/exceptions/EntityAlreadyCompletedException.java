package com.ftc.containerView.infra.errorhandling.exceptions;

public class EntityAlreadyCompletedException extends RuntimeException {
    public EntityAlreadyCompletedException(String message) {
        super(message);
    }
}