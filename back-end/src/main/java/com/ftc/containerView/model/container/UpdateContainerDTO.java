package com.ftc.containerView.model.container;

import java.util.List;

public record UpdateContainerDTO(
        String description,
        Integer sacksCount,
        Float tareTons,
        Float liquidWeight,
        Float grossWeight,
        String agencySeal,
        List<String> otherSeals,
        ContainerStatus status
) {}