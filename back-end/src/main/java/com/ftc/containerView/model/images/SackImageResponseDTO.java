package com.ftc.containerView.model.images;

import java.time.LocalDateTime;

public record SackImageResponseDTO(
        Long id,
        String imageUrl,
        String imageKey,
        LocalDateTime uploadedAt,
        int expirationMinutes
) {}