package com.ftc.containerView.repositories;

import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.model.operation.Operation;
import com.ftc.containerView.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Long> {

    Optional<Operation> findById(Long id);
    Optional<Operation> findByContainers(Container container);
    List<Operation> findByUser(User user);
    List<Operation> findByCreatedAt(LocalDateTime createdAt);
    List<Operation> findByCreatedAtBefore(LocalDateTime createdAt);
    List<Operation> findByCreatedAtAfter(LocalDateTime createdAt);
    List<Operation> findByCreatedAtBetween(LocalDateTime createdAt1, LocalDateTime createdAt2);

}
