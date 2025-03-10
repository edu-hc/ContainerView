package com.ftc.containerView.repositories;

import com.ftc.containerView.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<LogEntry, Long> {

    // Encontre logs entre duas datas
    List<LogEntry> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Encontre logs por nível (info, error, etc.)
    List<LogEntry> findByLevel(String level);

    // Encontre logs por nível e data
    List<LogEntry> findByLevelAndCreatedAtBetween(String level, LocalDateTime startDate, LocalDateTime endDate);
}
