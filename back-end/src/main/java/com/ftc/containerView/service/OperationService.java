package com.ftc.containerView.service;

import com.ftc.containerView.infra.aws.S3Service;
import com.ftc.containerView.infra.errorhandling.exceptions.ContainerNotFoundException;
import com.ftc.containerView.infra.errorhandling.exceptions.ImageNotFoundException;
import com.ftc.containerView.infra.errorhandling.exceptions.OperationNotFoundException;
import com.ftc.containerView.infra.errorhandling.exceptions.UserNotFoundException;
import com.ftc.containerView.infra.security.InputSanitizer;
import com.ftc.containerView.model.container.ContainerStatus;
import com.ftc.containerView.model.images.AddSackImagesResultDTO;
import com.ftc.containerView.model.images.SackImage;
import com.ftc.containerView.model.images.SackImageResponseDTO;
import com.ftc.containerView.model.operation.OperationDTO;
import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.model.operation.Operation;
import com.ftc.containerView.model.operation.OperationStatus;
import com.ftc.containerView.model.operation.UpdateOperationDTO;
import com.ftc.containerView.model.user.User;
import com.ftc.containerView.repositories.ContainerRepository;
import com.ftc.containerView.repositories.OperationRepository;
import com.ftc.containerView.repositories.SackImageRepository;
import com.ftc.containerView.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OperationService {

    private static final Logger logger = LoggerFactory.getLogger(OperationService.class);
    private final OperationRepository operationRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final SackImageRepository sackImageRepository;
    private final StoreImageService storeImageService;
    private final InputSanitizer inputSanitizer;

    @Autowired
    public OperationService(OperationRepository operationRepository, UserRepository userRepository, S3Service s3Service, ContainerRepository containerRepository, ContainerService containerService, SackImageRepository sackImageRepository, StoreImageService storeImageService, InputSanitizer inputSanitizer) {
        this.operationRepository = operationRepository;
        this.userRepository = userRepository;
        this.s3Service = s3Service;
        this.sackImageRepository = sackImageRepository;
        this.storeImageService = storeImageService;
        this.inputSanitizer = inputSanitizer;
        logger.info("OperationService inicializado com sucesso");
    }

    public Page<Operation> findOperations(Pageable pageable) {
        logger.info("Buscando operações com paginação - Página: {}, Tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Operation> operations = operationRepository.findAll(pageable);

        logger.info("Encontradas {} operações na página {} de {} (Total: {})",
                operations.getNumberOfElements(),
                operations.getNumber() + 1,
                operations.getTotalPages(),
                operations.getTotalElements());

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

    public Page<Operation> getOperationsByStatus(OperationStatus status, Pageable pageable) {
        logger.info("Buscando operações com status {} - Página: {}, Tamanho: {}",
                status, pageable.getPageNumber(), pageable.getPageSize());

        Page<Operation> operations = operationRepository.findByStatus(status, pageable);

        logger.info("Encontradas {} operações com status {} na página {} de {} (Total: {})",
                operations.getNumberOfElements(), status,
                operations.getNumber() + 1, operations.getTotalPages(),
                operations.getTotalElements());

        return operations;
    }


    public Page<Operation> getOperationsByUser(Long userId, Pageable pageable) {
        logger.info("Buscando operações do usuário {} - Página: {}, Tamanho: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Usuário com ID {} não encontrado", userId);
                    return new UserNotFoundException("Usuário não encontrado com ID: " + userId);
                });

        Page<Operation> operations = operationRepository.findByUser(user, pageable);

        logger.info("Encontradas {} operações do usuário {} na página {} de {} (Total: {})",
                operations.getNumberOfElements(), userId,
                operations.getNumber() + 1, operations.getTotalPages(),
                operations.getTotalElements());

        return operations;
    }

    @Transactional
    public Operation updateOperation(Long operationId, UpdateOperationDTO updateDTO, Long userId) {
        logger.info("Atualizando operação com ID: {} por usuário ID: {}", operationId, userId);

        try {
            Operation existingOperation = operationRepository.findById(operationId)
                    .orElseThrow(() -> {
                        logger.warn("Operação com ID {} não encontrada.", operationId);
                        return new OperationNotFoundException("Operação não encontrada com ID: " + operationId);
                    });

            // Verificar se o usuário tem permissão para atualizar
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com ID: " + userId));

            boolean changed = false;

            // Atualizar apenas os campos enviados (não nulos)
            if (updateDTO.ctv() != null && !existingOperation.getCtv().equals(updateDTO.ctv())) {
                String sanitizedCtv = inputSanitizer.sanitizePlainText(updateDTO.ctv());
                existingOperation.setCtv(sanitizedCtv);
                changed = true;
                logger.debug("CTV atualizado para: {}", sanitizedCtv);
            }

            if (updateDTO.exporter() != null && !existingOperation.getExporter().equals(updateDTO.exporter())) {
                String sanitizedExporter = inputSanitizer.sanitizePlainText(updateDTO.exporter());
                existingOperation.setExporter(sanitizedExporter);
                changed = true;
                logger.debug("Exportador atualizado para: {}", sanitizedExporter);
            }

            if (updateDTO.ship() != null && !existingOperation.getShip().equals(updateDTO.ship())) {
                String sanitizedShip = inputSanitizer.sanitizePlainText(updateDTO.ship());
                existingOperation.setShip(sanitizedShip);
                changed = true;
                logger.debug("Navio atualizado para: {}", sanitizedShip);
            }

            if (updateDTO.terminal() != null && !existingOperation.getTerminal().equals(updateDTO.terminal())) {
                String sanitizedTerminal = inputSanitizer.sanitizePlainText(updateDTO.terminal());
                existingOperation.setTerminal(sanitizedTerminal);
                changed = true;
                logger.debug("Terminal atualizado para: {}", sanitizedTerminal);
            }

            if (updateDTO.deadlineDraft() != null && !existingOperation.getDeadlineDraft().equals(updateDTO.deadlineDraft())) {
                existingOperation.setDeadlineDraft(updateDTO.deadlineDraft());
                changed = true;
                logger.debug("Prazo do draft atualizado para: {}", updateDTO.deadlineDraft());
            }

            if (updateDTO.destination() != null && !existingOperation.getDestination().equals(updateDTO.destination())) {
                String sanitizedDestination = inputSanitizer.sanitizePlainText(updateDTO.destination());
                existingOperation.setDestination(sanitizedDestination);
                changed = true;
                logger.debug("Destino atualizado para: {}", sanitizedDestination);
            }

            if (updateDTO.arrivalDate() != null && !existingOperation.getArrivalDate().equals(updateDTO.arrivalDate())) {
                existingOperation.setArrivalDate(updateDTO.arrivalDate());
                changed = true;
                logger.debug("Data de chegada atualizada para: {}", updateDTO.arrivalDate());
            }

            if (updateDTO.reservation() != null && !existingOperation.getReservation().equals(updateDTO.reservation())) {
                String sanitizedReservation = inputSanitizer.sanitizePlainText(updateDTO.reservation());
                existingOperation.setReservation(sanitizedReservation);
                changed = true;
                logger.debug("Reserva atualizada para: {}", sanitizedReservation);
            }

            if (updateDTO.refClient() != null && !existingOperation.getRefClient().equals(updateDTO.refClient())) {
                String sanitizedRefClient = inputSanitizer.sanitizePlainText(updateDTO.refClient());
                existingOperation.setRefClient(sanitizedRefClient);
                changed = true;
                logger.debug("Referência do cliente atualizada para: {}", sanitizedRefClient);
            }

            if (updateDTO.loadDeadline() != null && !existingOperation.getLoadDeadline().equals(updateDTO.loadDeadline())) {
                String sanitizedLoadDeadline = inputSanitizer.sanitizePlainText(updateDTO.loadDeadline());
                existingOperation.setLoadDeadline(sanitizedLoadDeadline);
                changed = true;
                logger.debug("Prazo de carregamento atualizado para: {}", sanitizedLoadDeadline);
            }

            if (updateDTO.status() != null && existingOperation.getStatus() != updateDTO.status()) {
                // Validar transição de status
                validateOperationStatusTransition(existingOperation.getStatus(), updateDTO.status());
                existingOperation.setStatus(updateDTO.status());
                changed = true;
                logger.debug("Status atualizado de {} para: {}", existingOperation.getStatus(), updateDTO.status());
            }

            if (changed) {
                // JPA Auditing irá atualizar automaticamente updatedAt e updatedByCpf
                Operation saved = operationRepository.save(existingOperation);
                logger.info("Operação com ID {} atualizada com sucesso.", operationId);
                return saved;
            } else {
                logger.info("Nenhuma alteração detectada para a operação com ID {}.", operationId);
                return existingOperation;
            }

        } catch (Exception e) {
            logger.error("Erro ao atualizar operação com ID: {}. Erro: {}", operationId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public AddSackImagesResultDTO addSackImagesToOperation(
            Operation operation,
            MultipartFile[] sackImages,
            Long userId) {

        logger.info("Adicionando {} imagens de sacaria à operação ID: {} por usuário ID: {}",
                sackImages.length, operation.getId(), userId);

        // Verificar permissões (opcional - depende das regras de negócio)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com ID: " + userId));

        // Verificar status da operação
        if (operation.getStatus() == OperationStatus.COMPLETED) {
            logger.error("Não é possível adicionar imagens a uma operação fechada: {}", operation.getId());
            throw new IllegalStateException("Não é possível adicionar imagens a uma operação fechada");
        }

        // Processar e armazenar as imagens
        List<SackImage> newSackImages = storeImageService.storeSackImages(sackImages, operation.getId());

        // Coletar IDs das novas imagens
        List<Long> addedImageIds = newSackImages.stream()
                .map(SackImage::getId)
                .collect(Collectors.toList());

        // Adicionar as novas imagens à lista existente da operação
        operation.getSacksImages().addAll(newSackImages);

        // Atualizar a operação
        Operation savedOperation = operationRepository.save(operation);

        logger.info("Total de {} imagens de sacaria adicionadas à operação {}. Total atual: {}",
                newSackImages.size(), operation.getId(), savedOperation.getSacksImages().size());

        return new AddSackImagesResultDTO(
                savedOperation,
                newSackImages.size(),
                addedImageIds,
                savedOperation.getSacksImages().size()
        );
    }

    public List<SackImageResponseDTO> getSackImagesWithUrls(Operation operation, int expirationMinutes) {
        logger.debug("Gerando URLs para {} imagens de sacaria da operação {}",
                operation.getSacksImages().size(), operation.getId());

        return operation.getSacksImages().stream()
                .map(sackImage -> {
                    String imageUrl = s3Service.generatePresignedUrl(sackImage.getImageKey(), expirationMinutes);
                    return new SackImageResponseDTO(
                            sackImage.getId(),
                            imageUrl,
                            sackImage.getImageKey(),
                            operation.getCreatedAt(), // ou adicionar campo específico se houver
                            expirationMinutes
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSackImage(Long operationId, Long imageId, Long userId) {
        logger.info("Removendo imagem de sacaria {} da operação {} por usuário {}",
                imageId, operationId, userId);

        // Buscar a operação
        Operation operation = findOperationById(operationId);

        // Verificar status da operação
        if (operation.getStatus() == OperationStatus.COMPLETED) {
            logger.error("Não é possível remover imagens de uma operação fechada: {}", operationId);
            throw new IllegalStateException("Não é possível remover imagens de uma operação fechada");
        }

        // Buscar a imagem
        SackImage imageToDelete = sackImageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Imagem de sacaria não encontrada com ID: " + imageId));

        // Verificar se a imagem pertence à operação
        if (!imageToDelete.getOperation().getId().equals(operationId)) {
            throw new IllegalArgumentException("Imagem não pertence à operação especificada");
        }

        // Deletar do S3
        try {
            s3Service.deleteFile(imageToDelete.getImageKey());
            logger.debug("Imagem de sacaria deletada do S3: {}", imageToDelete.getImageKey());
        } catch (Exception e) {
            logger.error("Erro ao deletar imagem do S3, continuando com remoção do banco: {}", e.getMessage());
        }

        // Remover da lista da operação
        operation.getSacksImages().removeIf(img -> img.getId().equals(imageId));

        // Deletar do banco
        sackImageRepository.delete(imageToDelete);

        logger.info("Imagem de sacaria {} removida com sucesso da operação {}", imageId, operationId);
    }



    public Operation completeOperationStatus (Long operationId) {
        logger.info("Mudando status para FINALIZADO do container com ID: {}", operationId);

        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> {
                    logger.warn("Operação com ID {} não encontrada.", operationId);
                    return new OperationNotFoundException("Operação não encontrada com ID: " + operationId);
                });

        logger.debug("Operação com ID {} encontrada.", operationId);

        validateOperationStatusTransition(operation.getStatus(), OperationStatus.COMPLETED);

        logger.info("Transição de status {} para {} autorizada", operation.getStatus(), OperationStatus.COMPLETED);

        operation.setStatus(OperationStatus.COMPLETED);

        logger.info("Status da operação com ID {} atualizado para FINALIZADO.", operationId);

        return operationRepository.save(operation);
    }

    private void validateOperationStatusTransition(OperationStatus currentStatus, OperationStatus newStatus) {
        logger.debug("Validando transição de status de {} para {}", currentStatus, newStatus);

        boolean validTransition = switch (currentStatus) {
            case OPEN -> newStatus == OperationStatus.COMPLETED;
            case COMPLETED -> false;
            default -> false;
        };

        if (!validTransition) {
            logger.error("Transição de status inválida: {} -> {}", currentStatus, newStatus);
            throw new IllegalArgumentException(
                    String.format("Transição de status inválida: %s -> %s", currentStatus, newStatus)
            );
        }

        logger.debug("Transição de status válida");
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

            // Sanitização dos campos de texto livre
            String sanitizedCtv = inputSanitizer.sanitizePlainText(operationDTO.ctv());
            String sanitizedExporter = inputSanitizer.sanitizePlainText(operationDTO.exporter());
            String sanitizedShip = inputSanitizer.sanitizePlainText(operationDTO.ship());
            String sanitizedTerminal = inputSanitizer.sanitizePlainText(operationDTO.terminal());
            String sanitizedDestination = inputSanitizer.sanitizePlainText(operationDTO.destination());
            String sanitizedReservation = inputSanitizer.sanitizePlainText(operationDTO.reservation());
            String sanitizedRefClient = inputSanitizer.sanitizePlainText(operationDTO.refClient());
            String sanitizedLoadDeadline = inputSanitizer.sanitizePlainText(operationDTO.loadDeadline());

            // Atribuindo os valores sanitizados à operação
            operation.setCtv(sanitizedCtv);
            operation.setExporter(sanitizedExporter);
            operation.setShip(sanitizedShip);
            operation.setTerminal(sanitizedTerminal);
            operation.setDestination(sanitizedDestination);
            operation.setReservation(sanitizedReservation);
            operation.setRefClient(sanitizedRefClient);
            operation.setLoadDeadline(sanitizedLoadDeadline);

            // Salvando a operação
            logger.debug("Salvando a operação no banco de dados");
            operationRepository.save(operation);

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
