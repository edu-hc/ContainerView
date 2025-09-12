package com.ftc.containerView.infra.aws;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class S3Service {

    private final AmazonS3 amazonS3;
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.images.folder}")
    private String imagesFolder;

    @Autowired
    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public String uploadFile(byte[] content, String fileName, String contentType) {
        String fileKey = imagesFolder + "/" + fileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(content.length);
        metadata.setContentType(contentType);

        amazonS3.putObject(
                new PutObjectRequest(bucketName, fileKey,
                        new ByteArrayInputStream(content),
                        metadata));

        return fileKey;
    }

    public void deleteFile(String fileKey) {
        logger.info("Deletando arquivo do S3: {}", fileKey);

        try {
            // Verificar se o arquivo existe antes de tentar deletar
            if (!fileExists(fileKey)) {
                logger.warn("Arquivo não encontrado no S3 para deletar: {}", fileKey);
                return;
            }

            // Criar requisição de delete
            DeleteObjectRequest deleteRequest = new DeleteObjectRequest(bucketName, fileKey);

            // Executar delete
            amazonS3.deleteObject(deleteRequest);

            logger.info("Arquivo {} deletado com sucesso do bucket {}", fileKey, bucketName);
            
        } catch (Exception e) {
            logger.error("Erro inesperado ao deletar arquivo {} do S3: {}", fileKey, e.getMessage(), e);
            throw new RuntimeException("Erro ao deletar arquivo do S3", e);
        }
    }

    /**
     * Deleta múltiplos arquivos do bucket S3 em lote
     * @param fileKeys - lista de chaves dos arquivos
     * @return número de arquivos deletados com sucesso
     */
    public int deleteFiles(List<String> fileKeys) {
        logger.info("Deletando {} arquivos do S3", fileKeys.size());

        int deletedCount = 0;
        List<String> errors = new ArrayList<>();

        for (String fileKey : fileKeys) {
            try {
                deleteFile(fileKey);
                deletedCount++;
            } catch (Exception e) {
                errors.add(fileKey);
                logger.error("Falha ao deletar arquivo {}: {}", fileKey, e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            logger.warn("Falha ao deletar {} arquivos: {}", errors.size(), errors);
        }

        logger.info("Deletados {} de {} arquivos do S3", deletedCount, fileKeys.size());
        return deletedCount;
    }

    public byte[] getFile(String fileKey) throws IOException {
        S3Object s3Object = amazonS3.getObject(bucketName, fileKey);
        try (InputStream inputStream = s3Object.getObjectContent()) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    public String generatePresignedUrl(String fileKey, int expirationMinutes) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime() + (expirationMinutes * 60 * 1000);
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, fileKey)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    public boolean fileExists(String fileKey) {
        try {
            amazonS3.getObjectMetadata(bucketName, fileKey);
            return true;
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                return false;
            }
            throw e;
        }
    }
}

