package com.ftc.containerView.model.auth;

import jakarta.validation.constraints.NotNull;

public record VerifyCodeDTO(
        @NotNull(message = "Token temporário é obrigatório")
        String tempToken,
        @NotNull(message = "Código 2FA é obrigatório")
        String code
) {}
