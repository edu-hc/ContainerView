package com.ftc.containerView.repositories;

import com.ftc.containerView.model.user.UserRole;
import com.ftc.containerView.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findAll(Pageable pageable);
    Optional<User> findById(Long id);
    Page<User> findByRole(UserRole role, Pageable pageable);
    Optional<User> findByCpf(String cpf);
    Optional<User> findByEmail(String email);
}
