package com.ftc.containerView.model.auth;

public record VerifyCodeDTO(
        String tempToken,
        String code
) {}
