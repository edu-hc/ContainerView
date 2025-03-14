package com.ftc.containerView.repositories;

import com.ftc.containerView.model.Container;
import com.ftc.containerView.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContainerRepository extends JpaRepository<Container, Long> {

    // Encontre contêineres por data de criação
    List<Container> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Container> findByUser(User user);
}
