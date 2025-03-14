package com.ftc.containerView.repositories;

import com.ftc.containerView.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Encontre usuário pelo nome de usuário
    Optional<User> findByUsername(String username);
}
