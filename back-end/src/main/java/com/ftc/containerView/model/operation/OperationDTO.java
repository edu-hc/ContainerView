package com.ftc.containerView.model.operation;

import java.util.List;

public record OperationDTO(String containerId,
                           String containerDescription,
                           List<String> containerImages,
                           Long userId) {
}
