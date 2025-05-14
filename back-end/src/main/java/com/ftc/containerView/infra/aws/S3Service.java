package com.ftc.containerView.infra.aws;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Service
public class S3Service {

    private final AmazonS3 amazonS3;

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

