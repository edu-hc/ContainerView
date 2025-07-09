package com.ftc.containerView.model.auth;

import jakarta.validation.constraints.NotNull;

public record VerifyCodeDTO(
        @NotNull(message = "Código 2FA é obrigatório")
        String code
) {}
