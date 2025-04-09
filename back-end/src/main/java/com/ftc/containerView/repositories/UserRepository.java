package com.ftc.containerView.repositories;

import com.ftc.containerView.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    List<User> findByRole(String role);
    List<User> findByUsernameContaining(String username);

}
