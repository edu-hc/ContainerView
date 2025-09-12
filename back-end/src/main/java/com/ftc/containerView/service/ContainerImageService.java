package com.ftc.containerView.service;

import com.ftc.containerView.infra.aws.S3Service;
import com.ftc.containerView.infra.errorhandling.exceptions.OperationNotFoundException;
import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.model.images.ContainerImage;
import com.ftc.containerView.model.images.ContainerImageCategory;
import com.ftc.containerView.model.operation.Operation;
import com.ftc.containerView.repositories.ContainerImageRepository;
import com.ftc.containerView.repositories.ContainerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service

public class ContainerImageService {

    private final ContainerImageRepository containerImageRepository;
    private final S3Service s3Service;
    private static final Logger logger = LoggerFactory.getLogger(ContainerImageService.class);
    private final ContainerRepository containerRepository;

    public ContainerImageService(ContainerImageRepository containerImageRepository, S3Service s3Service, ContainerRepository containerRepository) {
        this.containerImageRepository = containerImageRepository;
        this.s3Service = s3Service;
        this.containerRepository = containerRepository;
    }

    public List<String> findContainerImagesByCategory(ContainerImageCategory category, String containerId) {
        logger.debug("Buscando imagens [{}] do container de ID: {}", category, containerId);

        Container container = containerRepository.findByContainerId(containerId).orElse(null);

        if (container == null) {
            logger.debug("Operação de ID {} não foi encontrada", containerId);
            throw new IllegalArgumentException("Operação não pode ser nula");
        }

        List<ContainerImage> images = containerImageRepository.findByContainer(container);

        if (images == null || images.isEmpty()) {
            logger.debug("Nenhuma imagem de container foi encontrada");
            throw new IllegalArgumentException("Lista de imagens de containeres não pode ser nula ou vazia");
        }

        logger.debug("Lista com {} imagens de containeres foi fornecida", images.size());

        List<String> categoryImagesKeys = images.stream().filter(x -> x.getCategory() == category).map(ContainerImage::getImageKey).toList();
        logger.debug("{} imagens encontradas para a categoria {}", categoryImagesKeys.size(), category);

        List<String> imageLinks = new ArrayList<>();

        for (String imageKey : categoryImagesKeys) {

            imageLinks.add(s3Service.generatePresignedUrl(imageKey, 120));
            logger.debug("Link da imagem {} gerado", imageKey);
        }

        return imageLinks;

    }
}
