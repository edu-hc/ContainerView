package com.ftc.containerView.model.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyTotpDTO(
        @NotBlank(message = "Código TOTP é obrigatório")
        @Pattern(regexp = "^\\d{6}$", message = "Código deve ter exatamente 6 dígitos")
        String code
) {}