package com.ftc.containerView.service;

import com.ftc.containerView.model.LogEntry;
import com.ftc.containerView.repositories.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogService {

    private final LogRepository logRepository;

    @Autowired
    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public LogEntry saveLog(String level, String message) {
        LogEntry logEntry = new LogEntry(level, message);
        return logRepository.save(logEntry);
    }

    // Buscar logs entre duas datas
    public List<LogEntry> getLogsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return logRepository.findByCreatedAtBetween(startDate, endDate);
    }

    // Buscar logs por nível
    public List<LogEntry> getLogsByLevel(String level) {
        return logRepository.findByLevel(level);
    }

    // Buscar logs por nível e entre duas datas
    public List<LogEntry> getLogsByLevelAndDateRange(String level, LocalDateTime startDate, LocalDateTime endDate) {
        return logRepository.findByLevelAndCreatedAtBetween(level, startDate, endDate);
    }
}
