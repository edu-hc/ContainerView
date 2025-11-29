package com.ftc.containerView.model.images;

public record ContainerImageResponseDTO(
        Long id,
        String imageUrl,
        String imageKey,
        ContainerImageCategory category,
        int expirationMinutes
) {}