package com.ftc.containerView.service;

import com.ftc.containerView.model.Container;
import com.ftc.containerView.model.Operation;
import com.ftc.containerView.model.User;
import com.ftc.containerView.repositories.OperationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OperationService {

    private final OperationRepository operationRepository;

    @Autowired
    public OperationService(OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    public Optional<Operation> findById(Long id) {return operationRepository.findById(id);}

    public Optional<Operation> findByContainer(Container container) {return operationRepository.findByContainer(container);}

    public List<Operation> findByUser(User user) {return operationRepository.findByUser(user);}

    public List<Operation> findByCreatedAt(LocalDateTime createdAt) { return operationRepository.findByCreatedAt(createdAt); }

    public List<Operation> findByCreatedAtBefore(LocalDateTime createdAt) { return operationRepository.findByCreatedAtBefore(createdAt); }

    public List<Operation> findByCreatedAtAfter(LocalDateTime createdAt) { return operationRepository.findByCreatedAtAfter(createdAt); }

    public List<Operation> findByCreatedAtBetween(LocalDateTime createdAt1, LocalDateTime createdAt2) { return operationRepository.findByCreatedAtBetween(createdAt1, createdAt2); }

}
