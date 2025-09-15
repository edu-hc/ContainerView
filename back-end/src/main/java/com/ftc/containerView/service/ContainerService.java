package com.ftc.containerView.service;

import com.ftc.containerView.infra.errorhandling.exceptions.ContainerNotFoundException;
import com.ftc.containerView.infra.errorhandling.exceptions.OperationNotFoundException;
import com.ftc.containerView.infra.errorhandling.exceptions.UserNotFoundException;
import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.model.container.ContainerStatus;
import com.ftc.containerView.model.container.CreateContainerDTO;
import com.ftc.containerView.model.container.UpdateContainerDTO;
import com.ftc.containerView.model.images.AddImagesToContainerResultDTO;
import com.ftc.containerView.model.images.ContainerImage;
import com.ftc.containerView.model.images.ContainerImageCategory;
import com.ftc.containerView.model.operation.Operation;
import com.ftc.containerView.model.operation.OperationStatus;
import com.ftc.containerView.model.user.User;
import com.ftc.containerView.repositories.ContainerRepository;
import com.ftc.containerView.repositories.OperationRepository;
import com.ftc.containerView.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    public ContainerService(ContainerRepository containerRepository, UserRepository userRepository, StoreImageService storeImageService, OperationRepository operationRepository) {
        this.containerRepository = containerRepository;
        this.userRepository = userRepository;
        this.storeImageService = storeImageService;
        this.operationRepository = operationRepository;
    }

    public Container createContainer(CreateContainerDTO container) {
        logger.info("Criando novo container: {}", container.containerId());
        try {
            Container newContainer = new Container(container.containerId(), container.description(),
                    userRepository.findById(container.userId()).get(), operationRepository.findById(container.operationId()).get(),
                    container.sacksCount(), container.tareTons(), container.liquidWeight(), container.grossWeight(),
                    container.agencySeal(), container.otherSeals(), container.status());
            Container saved = containerRepository.save(newContainer);
            logger.info("Container criado com sucesso: {}", saved.getId());
            return saved;
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao criar container: {}. Erro: {}", container.containerId(), e.getMessage(), e);
            throw new ContainerNotFoundException("Container nao encontrado com ID: " + container.containerId());
        }
    }

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
                if (containerRepository.existsById(containerDTO.containerId())) {
                    logger.error("Container já existe: {}", containerDTO.containerId());
                    throw new IllegalArgumentException("Container já existe: " + containerDTO.containerId());
                }

                // Validar operação existe
                Operation operation = operationRepository.findById(containerDTO.operationId())
                        .orElseThrow(() -> new OperationNotFoundException(
                                "Operação não encontrada: " + containerDTO.operationId()));

                // Criar container usando metodo existente
                Container newContainer = new Container(
                        containerDTO.containerId(),
                        containerDTO.description(),
                        user,
                        operation,
                        containerDTO.sacksCount(),
                        containerDTO.tareTons(),
                        containerDTO.liquidWeight(),
                        containerDTO.grossWeight(),
                        containerDTO.agencySeal(),
                        containerDTO.otherSeals(),
                        containerDTO.status() != null ? containerDTO.status() : ContainerStatus.PENDING
                );

                Container saved = containerRepository.save(newContainer);
                createdContainers.add(saved);

                logger.debug("Container {} criado com sucesso", containerDTO.containerId());

            } catch (Exception e) {
                // Em caso de erro, fazer rollback de toda a transação
                logger.error("Erro ao criar container {}: {}", containerDTO.containerId(), e.getMessage());
                throw new RuntimeException("Erro ao criar container " + containerDTO.containerId() + ": " + e.getMessage(), e);
            }
        }

        logger.info("{} containers criados com sucesso", createdContainers.size());
        return createdContainers;
    }

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

        // Verificar permissões (opcional - depende das regras de negócio)
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
            logger.debug("Adicionadas {} imagens VAZIO_FORRADO", images.size());
        }

        if (fiadaImages != null && fiadaImages.length > 0) {
            List<ContainerImage> images = storeImageService.storeImagesToContainer(
                    fiadaImages, container.getId(), ContainerImageCategory.FIADA);
            newImages.addAll(images);
            imagesByCategory.put("FIADA", images.size());
            logger.debug("Adicionadas {} imagens FIADA", images.size());
        }

        if (cheioAbertoImages != null && cheioAbertoImages.length > 0) {
            List<ContainerImage> images = storeImageService.storeImagesToContainer(
                    cheioAbertoImages, container.getId(), ContainerImageCategory.CHEIO_ABERTO);
            newImages.addAll(images);
            imagesByCategory.put("CHEIO_ABERTO", images.size());
            logger.debug("Adicionadas {} imagens CHEIO_ABERTO", images.size());
        }

        if (meiaPortaImages != null && meiaPortaImages.length > 0) {
            List<ContainerImage> images = storeImageService.storeImagesToContainer(
                    meiaPortaImages, container.getId(), ContainerImageCategory.MEIA_PORTA);
            newImages.addAll(images);
            imagesByCategory.put("MEIA_PORTA", images.size());
            logger.debug("Adicionadas {} imagens MEIA_PORTA", images.size());
        }

        if (lacradoFechadoImages != null && lacradoFechadoImages.length > 0) {
            List<ContainerImage> images = storeImageService.storeImagesToContainer(
                    lacradoFechadoImages, container.getId(), ContainerImageCategory.LACRADO_FECHADO);
            newImages.addAll(images);
            imagesByCategory.put("LACRADO_FECHADO", images.size());
            logger.debug("Adicionadas {} imagens LACRADO_FECHADO", images.size());
        }

        if (lacresPrincipalImages != null && lacresPrincipalImages.length > 0) {
            List<ContainerImage> images = storeImageService.storeImagesToContainer(
                    lacresPrincipalImages, container.getId(), ContainerImageCategory.LACRES_PRINCIPAIS);
            newImages.addAll(images);
            imagesByCategory.put("LACRES_PRINCIPAIS", images.size());
            logger.debug("Adicionadas {} imagens LACRES_PRINCIPAIS", images.size());
        }

        if (lacresOutrosImages != null && lacresOutrosImages.length > 0) {
            List<ContainerImage> images = storeImageService.storeImagesToContainer(
                    lacresOutrosImages, container.getId(), ContainerImageCategory.LACRES_OUTROS);
            newImages.addAll(images);
            imagesByCategory.put("LACRES_OUTROS", images.size());
            logger.debug("Adicionadas {} imagens LACRES_OUTROS", images.size());
        }

        // Adicionar as novas imagens à lista existente do container
        container.getContainerImages().addAll(newImages);

        // Salvar o container atualizado
        Container savedContainer = containerRepository.save(container);

        logger.info("Total de {} imagens adicionadas ao container {}",
                newImages.size(), container.getContainerId());

        return new AddImagesToContainerResultDTO(
                savedContainer,
                newImages.size(),
                imagesByCategory
        );
    }

    public Container saveContainer(Container container) {
        logger.info("Salvando novo container: {}", container.getId());
        try {
            Container saved = containerRepository.save(container);
            logger.info("Container salvo com sucesso: {}", saved.getId());
            return saved;
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao salvar container: {}. Erro: {}", container.getId(), e.getMessage(), e);
            throw new ContainerNotFoundException("Container nao encontrado com ID: " + container.getId());
        }
    }

    @Transactional
    public Container updateContainer(String containerId, UpdateContainerDTO updateDTO, Long userId) {
        logger.info("Atualizando container com ID: {} por usuário ID: {}", containerId, userId);

        try {
            Container existingContainer = containerRepository.findById(containerId)
                    .orElseThrow(() -> {
                        logger.warn("Container com ID {} não encontrado.", containerId);
                        return new ContainerNotFoundException("Container não encontrado com ID: " + containerId);
                    });

            // Verificar se o usuário tem permissão para atualizar (opcional - depende das regras de negócio)
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com ID: " + userId));

            boolean changed = false;

            // Atualizar apenas os campos enviados (não nulos)
            if (updateDTO.description() != null && !existingContainer.getDescription().equals(updateDTO.description())) {
                existingContainer.setDescription(updateDTO.description());
                changed = true;
                logger.debug("Descrição atualizada para: {}", updateDTO.description());
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
                existingContainer.setAgencySeal(updateDTO.agencySeal());
                changed = true;
                logger.debug("Lacre da agência atualizado para: {}", updateDTO.agencySeal());
            }

            if (updateDTO.otherSeals() != null && !existingContainer.getOtherSeals().equals(updateDTO.otherSeals())) {
                existingContainer.setOtherSeals(updateDTO.otherSeals());
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

    public List<Container> getContainers() {
        logger.info("Buscando todos os containers.");
        List<Container> containers = containerRepository.findAll();
        logger.info("Encontrados {} containers.", containers.size());
        return containers;
    }

    public Container getContainersById(String id) {
        logger.info("Buscando container por ID: {}", id);
        Optional<Container> container = containerRepository.findById(id);
        if (container.isPresent()) {
            logger.info("Container com ID {} encontrado.", id);
            return container.get();
        } else {
            logger.warn("Container com ID {} n 3o encontrado.", id);
            throw new ContainerNotFoundException("Container nao encontrado com ID: " + id);
        }
    }


    public void deleteContainer(String id) {
        logger.info("Excluindo container com ID: {}", id);
        try {
            if (!containerRepository.existsById(id)) {
                logger.warn("Usuário com ID {} nao encontrado.", id);
                throw new ContainerNotFoundException("Container nao encontrado com ID: " + id);
            }
            containerRepository.deleteById(id);
            logger.info("Container com ID {} excluído com sucesso.", id);
        } catch (Exception e) {
            logger.error("Erro ao excluir container com ID: {}. Erro: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    public Container getContainersByContainerId(String id) {
        logger.info("Buscando container por ID: {}", id);
        Optional<Container> container = containerRepository.findById(id);
        if (container.isPresent()) {
            logger.info("Container com ID {} encontrado.", id);
            return container.get();
        } else {
            logger.warn("Container com ID {} nulo encontrado.", id);
            throw new ContainerNotFoundException("Container nao encontrado com ID: " + id);
        }
    }

    public Container completeContainerStatus (String containerId) {
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
            logger.info("Todas as 6 categorias obrigatórias contem pelo menos 1 imagem");
        }
    }

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
}
