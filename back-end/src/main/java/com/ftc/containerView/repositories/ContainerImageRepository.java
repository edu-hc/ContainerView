package com.ftc.containerView.repositories;

import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.model.images.ContainerImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContainerImageRepository extends JpaRepository<ContainerImage, Long> {

    List<ContainerImage> findByContainer(Container container);
}
