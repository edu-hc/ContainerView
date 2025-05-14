package com.ftc.containerView.model.auth;

public record LoginResponseDTO(
        String cpf,
        boolean requiresTwoFactor,
        String temporaryToken
) {}