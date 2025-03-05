package com.ftc.containerView.repository;

import com.ftc.containerView.model.Container;
import com.ftc.containerView.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContainerRepository extends JpaRepository<Container, Long> {

    List<Container> findByUser(User user);
}

