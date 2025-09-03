package com.ftc.containerView.repositories;

import com.ftc.containerView.model.images.SackImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SackImageRepository extends JpaRepository<SackImage, Long> {
}
