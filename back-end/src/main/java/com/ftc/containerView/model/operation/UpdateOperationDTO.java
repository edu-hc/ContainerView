package com.ftc.containerView.model.operation;

import java.util.Date;

public record UpdateOperationDTO(
        String ctv,
        String exporter,
        String ship,
        String terminal,
        Date deadlineDraft,
        String destination,
        Date arrivalDate,
        String reservation,
        String refClient,
        String loadDeadline,
        OperationStatus status
) {}