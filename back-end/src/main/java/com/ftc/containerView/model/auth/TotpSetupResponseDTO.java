package com.ftc.containerView.model.auth;

public record TotpSetupResponseDTO(
        String secret,
        String qrCodeDataUri,
        String message
) {}