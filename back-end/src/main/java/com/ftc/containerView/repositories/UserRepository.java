package com.ftc.containerView.repositories;

import com.ftc.containerView.model.user.UserRole;
import com.ftc.containerView.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long id);
    List<User> findByRole(UserRole role);
    Optional<User> findByCpf(String cpf);
    Optional<User> findByEmail(String email);
}
