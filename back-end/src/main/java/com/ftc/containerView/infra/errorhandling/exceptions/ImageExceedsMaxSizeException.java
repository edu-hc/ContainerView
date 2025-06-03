package com.ftc.containerView.infra.errorhandling.exceptions;

public class ImageExceedsMaxSizeException extends RuntimeException {

    public ImageExceedsMaxSizeException() {
        super("Image exceeds maximum size");
    }
    public ImageExceedsMaxSizeException(String message) {
        super(message);
    }
}
