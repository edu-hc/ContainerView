package com.ftc.containerView.model.container;

import java.util.List;

public record ContainerRequestDTO(String containerId,
                                  String description,
                                  Long operationId,
                                  int sacksCount,
                                  float tareTons,
                                  float liquidWeight,
                                  float grossWeight,
                                  String agencySeal,
                                  List<String> otherSeals) {
}
