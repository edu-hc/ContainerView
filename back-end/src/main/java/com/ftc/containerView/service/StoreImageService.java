package com.ftc.containerView.service;

import com.ftc.containerView.infra.aws.S3Service;
import com.ftc.containerView.infra.errorhandling.exceptions.ImageStorageException;
import com.ftc.containerView.model.images.ContainerImage;
import com.ftc.containerView.model.images.ContainerImageCategory;
import com.ftc.containerView.model.images.SackImage;
import com.ftc.containerView.repositories.ContainerImageRepository;
import com.ftc.containerView.repositories.ContainerRepository;
import com.ftc.containerView.repositories.OperationRepository;
import com.ftc.containerView.repositories.SackImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class StoreImageService {
    private static final Logger logger = LoggerFactory.getLogger(StoreImageService.class);
    private final ImageValidationService imageValidationService;
    private final ContainerRepository containerRepository;
    private final ContainerImageRepository containerImageRepository;

    private final SackImageRepository sackImageRepository;

    private final OperationRepository operationRepository;

    private final S3Service s3Service;

    @Autowired
    public StoreImageService(ImageValidationService imageValidationService, ContainerRepository containerRepository, ContainerImageRepository containerImageRepository, SackImageRepository sackImageRepository, OperationRepository operationRepository, S3Service s3Service) {
        this.imageValidationService = imageValidationService;
        this.containerRepository = containerRepository;
        this.containerImageRepository = containerImageRepository;
        this.sackImageRepository = sackImageRepository;
        this.operationRepository = operationRepository;
        this.s3Service = s3Service;
    }

    public List<ContainerImage> storeImagesToContainer(MultipartFile[] images, long containerIdDef, ContainerImageCategory category) {
        logger.info("Armazenando {} imagens para o container {}.", images != null ? images.length : 0, containerIdDef);
        int imageCount = 0;
        List<ContainerImage> containerImages = new ArrayList<>();

        if (images == null || images.length == 0) {
            return containerImages;
        }

        for (MultipartFile image : images) {
            String uniqueId = UUID.randomUUID().toString();
            String fileName = "containerImage_" + uniqueId + "_" + category + "_" + containerIdDef + ".jpg";

            imageValidationService.validateImage(image);

            try {
                ContainerImage containerImage = new ContainerImage();
                containerImage.setImageKey(s3Service.uploadFile(image.getBytes(), fileName, "application/jpg"));
                containerImage.setContainer(containerRepository.findById(containerIdDef).get());
                containerImage.setCategory(category);
                ContainerImage savedImage = containerImageRepository.save(containerImage);
                containerImages.add(savedImage);
                logger.info("Imagem {} {} armazenada com sucesso como {}.", category,imageCount, fileName);
            }
            catch (Exception e) {
                logger.error("Erro ao armazenar imagem {} para container {}: {}", fileName, containerIdDef, e.getMessage(), e);
                throw new ImageStorageException("Erro ao armazenar imagem para o container: " + containerIdDef, e);
            }
        }
        logger.info("Todas as imagens processadas para o container {}.", containerIdDef);
        return containerImages;
    }

    public List<SackImage> storeSackImages(MultipartFile[] images, long operationId) {
        logger.info("Armazenando {} imagens de sacaria para a operação {}.", images != null ? images.length : 0, operationId);
        int imageCount = 0;
        List<SackImage> sackImages = new ArrayList<>();

        if (images == null || images.length == 0) {
            return sackImages;
        }

        for (MultipartFile image : images) {
            String fileName = "sackImage_" + imageCount++ + "_" + operationId + ".jpg";

            imageValidationService.validateImage(image);

            try {
                SackImage sackImage = new SackImage();
                sackImage.setImageKey(s3Service.uploadFile(image.getBytes(), fileName, "application/jpg"));
                sackImage.setOperation(operationRepository.findById(operationId).get());
                SackImage savedImage = sackImageRepository.save(sackImage);
                sackImages.add(savedImage);
                logger.info("Imagem de sacaria {} armazenada com sucesso como {} na operação {}.", imageCount, fileName, operationId);
            }
            catch (Exception e) {
                logger.error("Erro ao armazenar imagem de sacaria {} para operação {}: {}", fileName, operationId, e.getMessage(), e);
                throw new ImageStorageException("Erro ao armazenar imagem de sacaria para a operação: " + operationId, e);
            }
        }
        logger.info("Todas as imagens de sacaria processadas para a operação {}.", operationId);
        return sackImages;
    }


}