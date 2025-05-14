package com.ftc.containerView.repositories;

import com.ftc.containerView.model.auth.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findFirstByUserCpfOrderByExpiryDateDesc(String userCpf);
    void deleteByUserCpf(String userCpf);
}