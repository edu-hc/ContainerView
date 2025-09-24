package com.ftc.containerView.repositories;

import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.model.container.ContainerStatus;
import com.ftc.containerView.model.operation.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContainerRepository extends JpaRepository<Container, String> {

    Optional<Container> findById(long id);

    Optional<Container> findByContainerId(String containerId);

    Page<Container> findByOperation(Operation operation, Pageable pageable);
    Page<Container> findByStatus(ContainerStatus status, Pageable pageable);

    long countByOperation(Operation operation);
    long countByStatus(ContainerStatus status);
}
