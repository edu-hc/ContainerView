package com.ftc.containerView.service;

import com.ftc.containerView.infra.errorhandling.exceptions.*;
import com.ftc.containerView.infra.security.InputSanitizer;
import com.ftc.containerView.model.container.*;
import com.ftc.containerView.model.images.AddImagesToContainerResultDTO;
import com.ftc.containerView.model.images.ContainerImage;
import com.ftc.containerView.model.images.ContainerImageCategory;
import com.ftc.containerView.model.operation.Operation;
import com.ftc.containerView.model.user.User;
import com.ftc.containerView.repositories.ContainerRepository;
import com.ftc.containerView.repositories.OperationRepository;
import com.ftc.containerView.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContainerService {

    private static final Logger logger = LoggerFactory.getLogger(ContainerService.class);

    private final ContainerRepository containerRepository;
    private final UserRepository userRepository;
    private final StoreImageService storeImageService;
    private final OperationRepository operationRepository;
    private final InputSanitizer inputSanitizer;

    @Autowired
    public ContainerService(ContainerRepository containerRepository, UserRepository userRepository,
                            StoreImageService storeImageService, OperationRepository operationRepository,
                            InputSanitizer inputSanitizer) {
        this.containerRepository = containerRepository;
        this.userRepository = userRepository;
        this.storeImageService = storeImageService;
        this.operationRepository = operationRepository;
        this.inputSanitizer = inputSanitizer;
    }

    /**
     * Cria um novo container (versão simplificada - usado pelo endpoint POST /containers)
     */
    public Container createContainer(CreateContainerDTO container) {
        logger.info("Criando novo container: {}", container.containerId());
        try {
            // Sanitização dos campos de texto livre
            String sanitizedContainerId = inputSanitizer.sanitizePlainText(container.containerId());
            String sanitizedDescription = inputSanitizer.sanitizeBasicHtml(container.description());
            String sanitizedAgencySeal = inputSanitizer.sanitizePlainText(container.agencySeal());

            // ✅ CORREÇÃO: Usar collect(Collectors.toList()) em vez de toList()
            List<String> sanitizedOtherSeals = null;
            if (container.otherSeals() != null) {
                sanitizedOtherSeals = container.otherSeals().stream()
                        .map(inputSanitizer::sanitizePlainText)
                        .collect(Collectors.toList());  // ✅ LISTA MUTÁVEL
            }

            Container newContainer = new Container(
                    sanitizedContainerId,
                    sanitizedDescription,
                    userRepository.findById(container.userId()).get(),
                    operationRepository.findById(container.operationId()).get(),
                    container.sacksCount(),
                    container.tareTons(),
                    container.liquidWeight(),
                    container.grossWeight(),
                    sanitizedAgencySeal,
                    sanitizedOtherSeals,
                    container.status()
            );

            Container saved = containerRepository.save(newContainer);
            logger.info("Container criado com sucesso: {}", saved.getId());
            return saved;

        } catch (IllegalArgumentException e) {
            logger.error("Erro ao criar container: {}", e.getMessage(), e);
            throw new ContainerNotFoundException("Container não encontrado com ID: " + container.containerId());
        }
    }

    /**
     * Cria um novo container com validações completas
     * (versão robusta - usado internamente ou pelo endpoint POST /containers/images)
     */
    @Transactional
    public Container createContainerWithValidations(CreateContainerDTO containerDTO) {
        logger.info("Criando novo container com validações: {}", containerDTO.containerId());

        try {
            // Buscar usuário
            User user = userRepository.findById(containerDTO.userId())
                    .orElseThrow(() -> new UserNotFoundException(
                            "Usuário não encontrado: " + containerDTO.userId()));

            // Verificar se já existe
            if (containerRepository.existsByContainerId(containerDTO.containerId())) {
                logger.error("Container já existe: {}", containerDTO.containerId());
                throw new IllegalArgumentException("Container já existe: " + containerDTO.containerId());
            }

            // Validar operação existe
            Operation operation = operationRepository.findById(containerDTO.operationId())
                    .orElseThrow(() -> new OperationNotFoundException(
                            "Operação não encontrada: " + containerDTO.operationId()));

            // Sanitização dos campos de texto livre
            String sanitizedContainerId = inputSanitizer.sanitizePlainText(containerDTO.containerId());
            String sanitizedDescription = inputSanitizer.sanitizeBasicHtml(containerDTO.description());
            String sanitizedAgencySeal = inputSanitizer.sanitizePlainText(containerDTO.agencySeal());

            // ✅ CORREÇÃO: Usar collect(Collectors.toList()) em vez de toList()
            List<String> sanitizedOtherSeals = null;
            if (containerDTO.otherSeals() != null) {
                sanitizedOtherSeals = containerDTO.otherSeals().stream()
                        .map(inputSanitizer::sanitizePlainText)
                        .collect(Collectors.toList());  // ✅ LISTA MUTÁVEL
            }

            // Criar container usando campos sanitizados
            Container newContainer = new Container(
                    sanitizedContainerId,
                    sanitizedDescription,
                    user,
                    operation,
                    containerDTO.sacksCount(),
                    containerDTO.tareTons(),
                    containerDTO.liquidWeight(),
                    containerDTO.grossWeight(),
                    sanitizedAgencySeal,
                    sanitizedOtherSeals,
                    containerDTO.status() != null ? containerDTO.status() : ContainerStatus.PENDING
            );

            Container saved = containerRepository.save(newContainer);
            logger.info("Container criado com sucesso: {}", saved.getId());
            return saved;

        } catch (UserNotFoundException | OperationNotFoundException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao criar container: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar container: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao criar container", e);
        }
    }

    /**
     * Atualiza um container existente
     */
    @Transactional
    public Container updateContainer(String containerId, UpdateContainerDTO updateDTO, Long userId) {
        logger.info("Atualizando container com ID: {} por usuário ID: {}", containerId, userId);

        try {
            Container existingContainer = containerRepository.findByContainerId(containerId)
                    .orElseThrow(() -> {
                        logger.warn("Container não encontrado com ID: {}", containerId);
                        return new ContainerNotFoundException("Container não encontrado com ID: " + containerId);
                    });

            // Verificar se o usuário tem permissão para atualizar (opcional)
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com ID: " + userId));

            boolean changed = false;

            // Atualizar apenas os campos enviados (não nulos)
            if (updateDTO.description() != null && !existingContainer.getDescription().equals(updateDTO.description())) {
                String sanitizedDescription = inputSanitizer.sanitizeBasicHtml(updateDTO.description());
                existingContainer.setDescription(sanitizedDescription);
                changed = true;
                logger.debug("Descrição atualizada para: {}", sanitizedDescription);
            }

            if (updateDTO.sacksCount() != null && existingContainer.getSacksCount() != updateDTO.sacksCount()) {
                existingContainer.setSacksCount(updateDTO.sacksCount());
                changed = true;
                logger.debug("Quantidade de sacas atualizada para: {}", updateDTO.sacksCount());
            }

            if (updateDTO.tareTons() != null && existingContainer.getTareTons() != updateDTO.tareTons()) {
                existingContainer.setTareTons(updateDTO.tareTons());
                changed = true;
                logger.debug("Tara em toneladas atualizada para: {}", updateDTO.tareTons());
            }

            if (updateDTO.liquidWeight() != null && existingContainer.getLiquidWeight() != updateDTO.liquidWeight()) {
                existingContainer.setLiquidWeight(updateDTO.liquidWeight());
                changed = true;
                logger.debug("Peso líquido atualizado para: {}", updateDTO.liquidWeight());
            }

            if (updateDTO.grossWeight() != null && existingContainer.getGrossWeight() != updateDTO.grossWeight()) {
                existingContainer.setGrossWeight(updateDTO.grossWeight());
                changed = true;
                logger.debug("Peso bruto atualizado para: {}", updateDTO.grossWeight());
            }

            if (updateDTO.agencySeal() != null && !existingContainer.getAgencySeal().equals(updateDTO.agencySeal())) {
                String sanitizedAgencySeal = inputSanitizer.sanitizePlainText(updateDTO.agencySeal());
                existingContainer.setAgencySeal(sanitizedAgencySeal);
                changed = true;
                logger.debug("Lacre da agência atualizado para: {}", sanitizedAgencySeal);
            }

            if (updateDTO.otherSeals() != null && !existingContainer.getOtherSeals().equals(updateDTO.otherSeals())) {
                // ✅ CORREÇÃO: Usar collect(Collectors.toList()) em vez de toList()
                List<String> sanitizedOtherSeals = updateDTO.otherSeals().stream()
                        .map(inputSanitizer::sanitizePlainText)
                        .collect(Collectors.toList());  // ✅ LISTA MUTÁVEL
                existingContainer.setOtherSeals(sanitizedOtherSeals);
                changed = true;
                logger.debug("Outros lacres atualizados");
            }

            if (updateDTO.status() != null && existingContainer.getStatus() != updateDTO.status()) {
                existingContainer.setStatus(updateDTO.status());
                changed = true;
                logger.debug("Status atualizado para: {}", updateDTO.status());
            }

            if (changed) {
                // JPA Auditing irá atualizar automaticamente updatedAt e updatedByCpf
                Container saved = containerRepository.save(existingContainer);
                logger.info("Container com ID {} atualizado com sucesso.", containerId);
                return saved;
            } else {
                logger.info("Nenhuma alteração detectada para o container com ID {}.", containerId);
                return existingContainer;
            }

        } catch (Exception e) {
            logger.error("Erro ao atualizar container com ID: {}. Erro: {}", containerId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Busca todos os containers
     */
    public List<Container> getContainers() {
        logger.info("Buscando todos os containers.");
        List<Container> containers = containerRepository.findAll();
        logger.info("Encontrados {} containers.", containers.size());
        return containers;
    }

    /**
     * Busca todos os containers com paginação
     */
    public Page<Container> getContainers(Pageable pageable) {
        logger.info("Buscando containers com paginação - Página: {}, Tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Container> containers = containerRepository.findAll(pageable);

        logger.info("Encontrados {} containers na página {} de {} (Total: {})",
                containers.getNumberOfElements(),
                containers.getNumber() + 1,
                containers.getTotalPages(),
                containers.getTotalElements());

        return containers;
    }

    /**
     * Busca container por ID
     */
    public Container getContainersById(String id) {
        logger.info("Buscando container por ID: {}", id);
        Optional<Container> container = containerRepository.findById(id);
        if (container.isPresent()) {
            logger.info("Container com ID {} encontrado.", id);
            return container.get();
        } else {
            logger.warn("Container com ID {} não encontrado.", id);
            throw new ContainerNotFoundException("Container não encontrado com ID: " + id);
        }
    }

    /**
     * Busca container por containerId (identificador do container, não ID do banco)
     */
    public Container getContainersByContainerId(String containerId) {
        logger.info("Buscando container por containerId: {}", containerId);
        Optional<Container> container = containerRepository.findByContainerId(containerId);
        if (container.isPresent()) {
            logger.info("Container com containerId {} encontrado.", containerId);
            return container.get();
        } else {
            logger.warn("Container com containerId {} não encontrado.", containerId);
            throw new ContainerNotFoundException("Container não encontrado com ID: " + containerId);
        }
    }

    /**
     * Busca containers por operação com paginação
     */
    public Page<Container> getContainersByOperation(Long operationId, Pageable pageable) {
        logger.info("Buscando containers da operação {} - Página: {}, Tamanho: {}",
                operationId, pageable.getPageNumber(), pageable.getPageSize());

        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> {
                    logger.warn("Operação com ID {} não encontrada", operationId);
                    return new OperationNotFoundException("Operação não encontrada com ID: " + operationId);
                });

        Page<Container> containers = containerRepository.findByOperation(operation, pageable);

        logger.info("Encontrados {} containers da operação {} na página {} de {} (Total: {})",
                containers.getNumberOfElements(), operationId,
                containers.getNumber() + 1, containers.getTotalPages(),
                containers.getTotalElements());

        return containers;
    }

    /**
     * Busca containers por status com paginação
     */
    public Page<Container> getContainersByStatus(ContainerStatus status, Pageable pageable) {
        logger.info("Buscando containers com status {} - Página: {}, Tamanho: {}",
                status, pageable.getPageNumber(), pageable.getPageSize());

        Page<Container> containers = containerRepository.findByStatus(status, pageable);

        logger.info("Encontrados {} containers com status {} na página {} de {} (Total: {})",
                containers.getNumberOfElements(), status,
                containers.getNumber() + 1, containers.getTotalPages(),
                containers.getTotalElements());

        return containers;
    }

    /**
     * Deleta um container por ID
     */
    public void deleteContainer(String id) {
        logger.info("Excluindo container com ID: {}", id);
        try {
            if (!containerRepository.existsById(id)) {
                logger.warn("Container com ID {} não encontrado.", id);
                throw new ContainerNotFoundException("Container não encontrado com ID: " + id);
            }
            containerRepository.deleteById(id);
            logger.info("Container com ID {} excluído com sucesso.", id);
        } catch (Exception e) {
            logger.error("Erro ao excluir container com ID: {}. Erro: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Completa o status de um container (muda para COMPLETED)
     */
    public Container completeContainerStatus(String containerId) {
        logger.info("Mudando status para FINALIZADO do container com ID: {}", containerId);

        Container container = containerRepository.findByContainerId(containerId)
                .orElseThrow(() -> {
                    logger.warn("Container com ID {} não encontrado.", containerId);
                    return new ContainerNotFoundException("Container não encontrado com ID: " + containerId);
                });

        logger.debug("Container com ID {} encontrado.", containerId);

        validateContainerStatusTransition(container.getStatus(), ContainerStatus.COMPLETED);

        validateMandatoryCategories(container.getContainerImages());

        logger.info("Transição de status {} para {} autorizada", container.getStatus(), ContainerStatus.COMPLETED);

        container.setStatus(ContainerStatus.COMPLETED);

        logger.info("Status do container com ID {} atualizado para FINALIZADO.", containerId);

        return containerRepository.save(container);
    }

    /**
     * Valida se todas as categorias obrigatórias de imagens estão presentes
     */
    public void validateMandatoryCategories(List<ContainerImage> images) {
        Set<ContainerImageCategory> mandatoryCategories = Set.of(
                ContainerImageCategory.VAZIO_FORRADO,
                ContainerImageCategory.FIADA,
                ContainerImageCategory.CHEIO_ABERTO,
                ContainerImageCategory.MEIA_PORTA,
                ContainerImageCategory.LACRADO_FECHADO,
                ContainerImageCategory.LACRES_PRINCIPAIS
        );

        Set<ContainerImageCategory> presentCategories = images.stream()
                .map(ContainerImage::getCategory)
                .collect(Collectors.toSet());

        if (!presentCategories.containsAll(mandatoryCategories)) {
            throw new IllegalArgumentException("Todas as 6 categorias obrigatórias devem ter pelo menos 1 imagem");
        } else {
            logger.info("Todas as 6 categorias obrigatórias contêm pelo menos 1 imagem");
        }
    }

    /**
     * Valida transições de status do container
     */
    private void validateContainerStatusTransition(ContainerStatus currentStatus, ContainerStatus newStatus) {
        logger.debug("Validando transição de status de {} para {}", currentStatus, newStatus);

        boolean validTransition = switch (currentStatus) {
            case OPEN -> newStatus == ContainerStatus.PENDING;
            case PENDING -> newStatus == ContainerStatus.COMPLETED;
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

    /**
     * Cria múltiplos containers em lote
     */
    @Transactional
    public List<Container> createMultipleContainers(List<CreateContainerDTO> containerDTOs, Long userId) {
        logger.info("Criando {} containers em lote para usuário {}", containerDTOs.size(), userId);

        // Validar usuário uma vez só
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com ID: " + userId));

        List<Container> createdContainers = new ArrayList<>();
        Set<String> containerIds = new HashSet<>();

        // Validar IDs duplicados na própria lista
        for (CreateContainerDTO dto : containerDTOs) {
            if (!containerIds.add(dto.containerId())) {
                logger.error("Container duplicado na requisição: {}", dto.containerId());
                throw new IllegalArgumentException("Container duplicado na requisição: " + dto.containerId());
            }
        }

        // Criar cada container
        for (CreateContainerDTO containerDTO : containerDTOs) {
            try {
                // Verificar se já existe
                if (containerRepository.existsByContainerId(containerDTO.containerId())) {
                    logger.error("Container já existe: {}", containerDTO.containerId());
                    throw new IllegalArgumentException("Container já existe: " + containerDTO.containerId());
                }

                // Validar operação existe
                Operation operation = operationRepository.findById(containerDTO.operationId())
                        .orElseThrow(() -> new OperationNotFoundException(
                                "Operação não encontrada: " + containerDTO.operationId()));

                // Sanitização dos campos de texto livre
                String sanitizedContainerId = inputSanitizer.sanitizePlainText(containerDTO.containerId());
                String sanitizedDescription = inputSanitizer.sanitizeBasicHtml(containerDTO.description());
                String sanitizedAgencySeal = inputSanitizer.sanitizePlainText(containerDTO.agencySeal());

                // ✅ CORREÇÃO: Usar collect(Collectors.toList()) em vez de toList()
                List<String> sanitizedOtherSeals = null;
                if (containerDTO.otherSeals() != null) {
                    sanitizedOtherSeals = containerDTO.otherSeals().stream()
                            .map(inputSanitizer::sanitizePlainText)
                            .collect(Collectors.toList());  // ✅ LISTA MUTÁVEL
                }

                Container newContainer = new Container(
                        sanitizedContainerId,
                        sanitizedDescription,
                        user,
                        operation,
                        containerDTO.sacksCount(),
                        containerDTO.tareTons(),
                        containerDTO.liquidWeight(),
                        containerDTO.grossWeight(),
                        sanitizedAgencySeal,
                        sanitizedOtherSeals,
                        containerDTO.status() != null ? containerDTO.status() : ContainerStatus.OPEN
                );

                Container saved = containerRepository.save(newContainer);
                createdContainers.add(saved);

                logger.debug("Container {} criado com sucesso", sanitizedContainerId);

            } catch (Exception e) {
                // Em caso de erro, fazer rollback de toda a transação
                logger.error("Erro ao criar container {}: {}", containerDTO.containerId(), e.getMessage());
                throw new RuntimeException("Erro ao criar container " + containerDTO.containerId() + ": " + e.getMessage(), e);
            }
        }

        logger.info("{} containers criados com sucesso", createdContainers.size());
        return createdContainers;
    }

    /**
     * Adiciona imagens a um container existente
     */
    @Transactional
    public AddImagesToContainerResultDTO addImagesToContainer(
            Container container,
            MultipartFile[] vazioForradoImages,
            MultipartFile[] fiadaImages,
            MultipartFile[] cheioAbertoImages,
            MultipartFile[] meiaPortaImages,
            MultipartFile[] lacradoFechadoImages,
            MultipartFile[] lacresPrincipalImages,
            MultipartFile[] lacresOutrosImages,
            Long userId) {

        logger.info("Adicionando imagens ao container ID: {} por usuário ID: {}",
                container.getContainerId(), userId);

        // Verificar permissões (opcional)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com ID: " + userId));

        List<ContainerImage> newImages = new ArrayList<>();
        Map<String, Integer> imagesByCategory = new HashMap<>();

        // Processar cada categoria de imagem
        if (vazioForradoImages != null && vazioForradoImages.length > 0) {
            List<ContainerImage> images = storeImageService.storeImagesToContainer(
                    vazioForradoImages, container.getId(), ContainerImageCategory.VAZIO_FORRADO);
            newImages.addAll(images);
            imagesByCategory.put("VAZIO_FORRADO", images.size());
        }

        if (fiadaImages != null && fiadaImages.length > 0) {
            List<ContainerImage> images = storeImageService.storeImagesToContainer(
                    fiadaImages, container.getId(), ContainerImageCategory.FIADA);
            newImages.addAll(images);
            imagesByCategory.put("FIADA", images.size());
        }

        if (cheioAbertoImages != null && cheioAbertoImages.length > 0) {
            List<ContainerImage> images = storeImageService.storeImagesToContainer(
                    cheioAbertoImages, container.getId(), ContainerImageCategory.CHEIO_ABERTO);
            newImages.addAll(images);
            imagesByCategory.put("CHEIO_ABERTO", images.size());
        }

        if (meiaPortaImages != null && meiaPortaImages.length > 0) {
            List<ContainerImage> images = storeImageService.storeImagesToContainer(
                    meiaPortaImages, container.getId(), ContainerImageCategory.MEIA_PORTA);
            newImages.addAll(images);
            imagesByCategory.put("MEIA_PORTA", images.size());
        }

        if (lacradoFechadoImages != null && lacradoFechadoImages.length > 0) {
            List<ContainerImage> images = storeImageService.storeImagesToContainer(
                    lacradoFechadoImages, container.getId(), ContainerImageCategory.LACRADO_FECHADO);
            newImages.addAll(images);
            imagesByCategory.put("LACRADO_FECHADO", images.size());
        }

        if (lacresPrincipalImages != null && lacresPrincipalImages.length > 0) {
            List<ContainerImage> images = storeImageService.storeImagesToContainer(
                    lacresPrincipalImages, container.getId(), ContainerImageCategory.LACRES_PRINCIPAIS);
            newImages.addAll(images);
            imagesByCategory.put("LACRES_PRINCIPAIS", images.size());
        }

        if (lacresOutrosImages != null && lacresOutrosImages.length > 0) {
            List<ContainerImage> images = storeImageService.storeImagesToContainer(
                    lacresOutrosImages, container.getId(), ContainerImageCategory.LACRES_OUTROS);
            newImages.addAll(images);
            imagesByCategory.put("LACRES_OUTROS", images.size());
        }

        // Adicionar novas imagens ao container
        container.getContainerImages().addAll(newImages);
        container.setStatus(ContainerStatus.PENDING);
        Container updatedContainer = containerRepository.save(container);

        logger.info("Total de {} imagens adicionadas ao container ID: {}",
                newImages.size(), container.getContainerId());

        return new AddImagesToContainerResultDTO(
                updatedContainer,
                newImages.size(),
                imagesByCategory
        );
    }
}