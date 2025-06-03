package com.ftc.containerView.service;

import com.ftc.containerView.infra.aws.S3Service;
import com.ftc.containerView.infra.errorhandling.exceptions.ImageStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class StoreImageService {

    private static final Logger logger = LoggerFactory.getLogger(StoreImageService.class);

    private final S3Service s3Service;

    @Autowired
    public StoreImageService(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    public List<String> storeImages(MultipartFile[] images, String containerId) {
        logger.info("Armazenando {} imagens para o container {}.", images != null ? images.length : 0, containerId);
        int imageCount = 0;
        List<String> imageKeys = new ArrayList<>();

        for (MultipartFile image : images) {
            String fileName = "image_" + imageCount++ + "_" + containerId + ".jpg";

            try {
                imageKeys.add(s3Service.uploadFile(image.getBytes(), fileName, "application/jpg"));
                logger.info("Imagem {} armazenada com sucesso como {}.", imageCount, fileName);
            }
            catch (Exception e) {
                logger.error("Erro ao armazenar imagem {} para container {}: {}", fileName, containerId, e.getMessage(), e);
                throw new ImageStorageException("Erro ao armazenar imagem para o container: " + containerId, e);
            }
        }
        logger.info("Todas as imagens processadas para o container {}.", containerId);
        return imageKeys;
    }
}
