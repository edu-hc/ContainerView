package com.ftc.containerView.repositories;

import com.ftc.containerView.model.container.Container;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContainerRepository extends JpaRepository<Container, String> {

    Optional<Container> findById(long id);
}
