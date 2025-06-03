package com.ftc.containerView.infra.errorhandling.exceptions;

public class ImageStorageException extends RuntimeException {

    public ImageStorageException() {
        super("Image storage error");
    }
    public ImageStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
