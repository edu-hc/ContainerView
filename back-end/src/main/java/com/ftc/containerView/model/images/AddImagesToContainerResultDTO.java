package com.ftc.containerView.model.images;

import com.ftc.containerView.model.container.Container;

import java.util.Map;

public record AddImagesToContainerResultDTO(
        Container updatedContainer,
        int totalImagesAdded,
        Map<String, Integer> imagesByCategory
) {}