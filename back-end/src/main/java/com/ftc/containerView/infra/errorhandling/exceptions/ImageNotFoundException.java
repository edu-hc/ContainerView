package com.ftc.containerView.infra.errorhandling.exceptions;

public class ImageNotFoundException extends RuntimeException {
  public ImageNotFoundException(String message) {
    super(message);
  }

  public ImageNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public ImageNotFoundException(Long imageId) {
    super("Imagem não encontrada com ID: " + imageId);
  }
}
