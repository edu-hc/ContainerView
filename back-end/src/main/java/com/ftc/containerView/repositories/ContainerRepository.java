package com.ftc.containerView.repositories;

import com.ftc.containerView.model.Container;
import com.ftc.containerView.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContainerRepository extends JpaRepository<Container, String> {

    Optional<Container> findById(String id);
}
