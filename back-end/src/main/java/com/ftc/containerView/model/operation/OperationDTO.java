package com.ftc.containerView.model.operation;

import com.ftc.containerView.model.container.Container;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public record OperationDTO(
        String ctv,
        String exporter,
        String ship,
        String terminal,
        Date deadlineDraft,
        String destination,
        Date arrivalDate,
        String reservation,
        String refClient,
        String loadDeadline) {
}
