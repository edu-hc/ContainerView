package com.ftc.containerView.model.images;

import com.ftc.containerView.model.operation.Operation;

import java.util.List;

public record AddSackImagesResultDTO(
        Operation updatedOperation,
        int totalImagesAdded,
        List<Long> addedImageIds,
        int totalSackImages
) {}