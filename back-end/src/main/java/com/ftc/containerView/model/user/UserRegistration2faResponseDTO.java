package com.ftc.containerView.model.user;

public record UserRegistration2faResponseDTO(
        String message,
        String cpf,
        String totpSecret,
        String qrCodeDataUri
) {}