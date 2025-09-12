package com.ftc.containerView.service;

import com.ftc.containerView.infra.aws.S3Service;
import com.ftc.containerView.model.images.ContainerImage;
import com.ftc.containerView.model.images.ContainerImageCategory;
import com.ftc.containerView.model.images.SackImage;
import com.ftc.containerView.model.operation.Operation;
import com.ftc.containerView.repositories.OperationRepository;
import com.ftc.containerView.repositories.SackImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SackImageService {

    private final SackImageRepository sackImageRepository;
    private final OperationRepository operationRepository;
    private final S3Service s3Service;
    private static final Logger logger = LoggerFactory.getLogger(SackImageService.class);


    public SackImageService(SackImageRepository sackImageRepository, OperationRepository operationRepository, S3Service s3Service) {
        this.sackImageRepository = sackImageRepository;
        this.operationRepository = operationRepository;
        this.s3Service = s3Service;
    }

    public List<String> findSackImages(Long operationId) {
        logger.debug("Buscando imagens de sacaria da operação de ID: {}", operationId);

        Operation operation = operationRepository.findById(operationId).orElse(null);

        if (operation == null) {
            logger.debug("Operação de ID {} não foi encontrada", operationId);
            throw new IllegalArgumentException("Operação não pode ser nula");
        }

        List<SackImage> images = sackImageRepository.findByOperation(operation);

        if (images == null || images.isEmpty()) {
            logger.debug("Nenhuma imagem de sacaria da operação foi encontrada");
            throw new IllegalArgumentException("Lista de imagens de sacaria não pode ser nula ou vazia");
        }

        logger.debug("Lista com {} imagens de sacaria foi fornecida", images.size());

        List<String> sackImagesKeys = images.stream().map(SackImage::getImageKey).toList();
        logger.debug("{} imagens encontradas para a sacaria da operação {}", sackImagesKeys.size(), operationId);

        List<String> imageLinks = new ArrayList<>();

        for (String imageKey : sackImagesKeys) {

            imageLinks.add(s3Service.generatePresignedUrl(imageKey, 120));
            logger.debug("Link da imagem {} gerado", imageKey);
        }

        logger.debug("Lista com {} links de imagens de sacaria foi gerada", imageLinks.size());

        return imageLinks;

    }
}
