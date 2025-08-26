package com.ftc.containerView.model.container;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record CreateContainerDTO(
        String containerId,
        String description,
        List<String> imageKeys,
        Long userId,
        Long operationId,
        int sacksCount,
        float tareTons,
        float liquidWeight,
        float grossWeight,
        String agencySeal,
        List<String> otherSeals
) {}
