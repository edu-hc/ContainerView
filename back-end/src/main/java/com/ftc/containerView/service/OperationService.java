package com.ftc.containerView.service;

import com.ftc.containerView.infra.errorhandling.exceptions.OperationNotFoundException;
import com.ftc.containerView.infra.errorhandling.exceptions.UserNotFoundException;
import com.ftc.containerView.model.operation.OperationDTO;
import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.model.operation.Operation;
import com.ftc.containerView.model.operation.OperationStatus;
import com.ftc.containerView.model.user.User;
import com.ftc.containerView.repositories.ContainerRepository;
import com.ftc.containerView.repositories.OperationRepository;
import com.ftc.containerView.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OperationService {

    private static final Logger logger = LoggerFactory.getLogger(OperationService.class);
    private final OperationRepository operationRepository;
    private final UserRepository userRepository;
    private final ContainerRepository containerRepository;
    private final ContainerService containerService;

    @Autowired
    public OperationService(OperationRepository operationRepository, UserRepository userRepository, ContainerRepository containerRepository, ContainerService containerService) {
        this.operationRepository = operationRepository;
        this.userRepository = userRepository;
        this.containerRepository = containerRepository;
        this.containerService = containerService;
        logger.info("OperationService inicializado com sucesso");
    }

    public List<Operation> findOperations() {
        logger.debug("Buscando todas as operações");
        List<Operation> operations = operationRepository.findAll();
        logger.debug("Encontradas {} operações", operations.size());
        return operations;
    }

    public Operation findOperationById(Long id) {
        logger.debug("Buscando operação com ID: {}", id);

        Optional<Operation> operation = operationRepository.findById(id);

        if (operation.isPresent()) {
            logger.debug("Operação com ID {} encontrada", id);
            return operation.get();
        } else {
            logger.debug("Operação com ID {} não encontrada", id);
            throw new OperationNotFoundException("Operação com ID " + id + " nao encontrada");
        }

    }

    public Optional<Operation> findOperationByContainer(Container container) {return operationRepository.findByContainers(container);}

    public List<Operation> findOperationByUser(User user) {return operationRepository.findByUser(user);}

    public List<Operation> findOperationByCreatedAt(LocalDateTime createdAt) { return operationRepository.findByCreatedAt(createdAt); }

    public List<Operation> findOperationByCreatedAtBefore(LocalDateTime createdAt) { return operationRepository.findByCreatedAtBefore(createdAt); }

    public List<Operation> findOperationByCreatedAtAfter(LocalDateTime createdAt) { return operationRepository.findByCreatedAtAfter(createdAt); }

    public List<Operation> findOperationByCreatedAtBetween(LocalDateTime createdAt1, LocalDateTime createdAt2) { return operationRepository.findByCreatedAtBetween(createdAt1, createdAt2); }

    public void deleteOperation(Operation operation) {

        logger.debug("Excluindo operação com ID: {}", operation.getId());

        try {
            if(!operationRepository.existsById(operation.getId())) {
                logger.warn("Operação com ID {} nao encontrada", operation.getId());
                throw new OperationNotFoundException("Operação com ID " + operation.getId() + " nao encontrada");
            }
            operationRepository.delete(operation);
            logger.debug("Operação com ID {} excluída com sucesso", operation.getId());
        } catch (Exception e) {
            logger.error("Erro ao excluir operação com ID: {}. Erro: {}",
                    operation.getId(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public Operation createOperation(OperationDTO operationDTO, long userId) {
        logger.info("Criando nova operação. UserId: {}", userId);

        try {
            // Verificando e buscando o usuário
            logger.debug("Buscando usuário com ID: {}", userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.error("Usuário não encontrado com ID: {}", userId);
                        return new UserNotFoundException("Usuário não encontrado com ID: " + userId);
                    });
            logger.debug("Usuário encontrado: {}", user.getId());

            // Criando a operação
            logger.debug("Criando nova operação");
            Operation operation = new Operation(operationDTO, user);
            operation.setStatus(OperationStatus.OPEN);

            // Salvando a operação
            logger.debug("Salvando a operação no banco de dados");
            operationRepository.save(operation);

//            List<Container> containers = new ArrayList<>();
//            if (operationDTO.containers() != null && operationDTO.containers().size() > 0) {
//                // Criando o container
//                logger.debug("Criando {} containeres na operação", operationDTO.containers().size());
//                containers = containerService.createContainers(operationDTO.containers(), userId);
//                logger.debug("Containers criados com sucesso. ID dos containers: {}", containers.stream().map(Container::getId).toList());
//            }


            // Associando a operação ao usuário
            logger.debug("Associando operação ao usuário ID: {}", user.getId());
            user.addOperation(operation);
            userRepository.save(user);

            logger.info("Operação criada com sucesso. ID da operação: {}", operation.getId());
            return operation;
        } catch (EntityNotFoundException e) {
            // Já logado acima
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao criar operação. UserId: {}. Erro: {}",
                    userId, e.getMessage(), e);
            throw e;
        }
    }
}
