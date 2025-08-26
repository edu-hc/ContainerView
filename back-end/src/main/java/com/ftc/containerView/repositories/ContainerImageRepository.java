package com.ftc.containerView.repositories;

import com.ftc.containerView.model.images.ContainerImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContainerImageRepository extends JpaRepository<ContainerImage, Long> {
}
