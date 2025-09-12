package com.ftc.containerView.repositories;

import com.ftc.containerView.model.images.SackImage;
import com.ftc.containerView.model.operation.Operation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SackImageRepository extends JpaRepository<SackImage, Long> {

    List<SackImage> findByOperation(Operation operation);
    int countByOperation(Operation operation);
    void deleteAllByOperation(Operation operation);
}
