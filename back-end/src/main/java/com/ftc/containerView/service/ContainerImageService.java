package com.ftc.containerView.service;

import com.ftc.containerView.infra.aws.S3Service;
import com.ftc.containerView.infra.errorhandling.exceptions.ImageNotFoundException;
import com.ftc.containerView.infra.errorhandling.exceptions.OperationNotFoundException;
import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.model.container.ContainerStatus;
import com.ftc.containerView.model.images.ContainerImage;
import com.ftc.containerView.model.images.ContainerImageCategory;
import com.ftc.containerView.model.images.ContainerImageResponseDTO;
import com.ftc.containerView.model.operation.Operation;
import com.ftc.containerView.repositories.ContainerImageRepository;
import com.ftc.containerView.repositories.ContainerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service

public class ContainerImageService {

    private final ContainerImageRepository containerImageRepository;
    private final S3Service s3Service;
    private static final Logger logger = LoggerFactory.getLogger(ContainerImageService.class);
    private final ContainerRepository containerRepository;
    private final ContainerService containerService;

    public ContainerImageService(ContainerImageRepository containerImageRepository, S3Service s3Service, ContainerRepository containerRepository, ContainerService containerService) {
        this.containerImageRepository = containerImageRepository;
        this.s3Service = s3Service;
        this.containerRepository = containerRepository;
        this.containerService = containerService;
    }

    public List<ContainerImageResponseDTO> findContainerImagesByCategory(ContainerImageCategory category, String containerId, int expirationMinutes) {
        logger.debug("Buscando imagens [{}] do container de ID: {}", category, containerId);

        Container container = containerRepository.findByContainerId(containerId).orElse(null);

        if (container == null) {
            logger.debug("Container de ID {} não foi encontrada", containerId);
            throw new IllegalArgumentException("Container não pode ser nulo");
        }

        List<ContainerImage> images = containerImageRepository.findByContainer(container);

        if (images == null || images.isEmpty()) {
            logger.debug("Nenhuma imagem de container foi encontrada");
            return new ArrayList<>();
        }

        logger.debug("Lista com {} imagens de containeres foi fornecida", images.size());

        List<ContainerImage> categoryImages = images.stream()
                .filter(x -> x.getCategory() == category)
                .toList();

        logger.debug("{} imagens encontradas para a categoria {}", categoryImages.size(), category);

        if (categoryImages.isEmpty()) {
            logger.debug("Nenhuma imagem encontrada para a categoria {} no container {}", category, containerId);
            return new ArrayList<>();
        }

        return categoryImages.stream()
                .map(image -> new ContainerImageResponseDTO(
                        image.getId(),
                        s3Service.generatePresignedUrl(image.getImageKey(), expirationMinutes),
                        image.getImageKey(),
                        image.getCategory(),
                        expirationMinutes
                ))
                .toList();

    }

    @Transactional
    public void deleteContainerImage(String containerId, Long imageId, Long userId) {
        logger.info("Removendo imagem {} do container {} por usuário {}",
                imageId, containerId, userId);

        // Buscar o container
        Container container = containerRepository.findByContainerId(containerId)
                .orElseThrow(() -> {
                    logger.error("Container não encontrado: {}", containerId);
                    return new IllegalArgumentException("Container não encontrado com ID: " + containerId);
                });

        containerService.validateContainerCanBeEdited(container);

        // Verificar status do container (opcional - dependendo da regra de negócio)
        if (container.getStatus() == ContainerStatus.COMPLETED) {
            logger.error("Não é possível remover imagens de um container fechado: {}", containerId);
            throw new IllegalStateException("Não é possível remover imagens de um container fechado");
        }

        // Buscar a imagem
        ContainerImage imageToDelete = containerImageRepository.findById(imageId)
                .orElseThrow(() -> {
                    logger.error("Imagem não encontrada: {}", imageId);
                    return new ImageNotFoundException("Imagem não encontrada com ID: " + imageId);
                });

        // Verificar se a imagem pertence ao container
        if (!imageToDelete.getContainer().getContainerId().equals(containerId)) {
            logger.error("Imagem {} não pertence ao container {}", imageId, containerId);
            throw new IllegalArgumentException("Imagem não pertence ao container especificado");
        }

        // Deletar do S3
        try {
            s3Service.deleteFile(imageToDelete.getImageKey());
            logger.debug("Imagem deletada do S3: {}", imageToDelete.getImageKey());
        } catch (Exception e) {
            logger.error("Erro ao deletar imagem do S3, continuando com remoção do banco: {}", e.getMessage());
            // Continua mesmo se falhar no S3 (segue o padrão do OperationService)
        }

        // Deletar do banco
        containerImageRepository.delete(imageToDelete);

        logger.info("Imagem {} removida com sucesso do container {}", imageId, containerId);
    }
}
