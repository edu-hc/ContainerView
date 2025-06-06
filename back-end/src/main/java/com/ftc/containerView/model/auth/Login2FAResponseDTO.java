package com.ftc.containerView.model.auth;

public record Login2FAResponseDTO(
        String cpf,
        boolean requiresTwoFactor,
        String temporaryToken
) {}