package com.ftc.containerView.infra.errorhandling.exceptions;

public class FileNotSupportedException extends RuntimeException {

    public FileNotSupportedException() {
        super("File not supported");
    }
    public FileNotSupportedException(String message) {
        super(message);
    }
}
