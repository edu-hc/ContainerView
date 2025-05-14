package com.ftc.containerView.service;

import com.ftc.containerView.infra.aws.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class StoreImageService {

    private final S3Service s3Service;

    @Autowired
    public StoreImageService(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    public List<String> storeImages(MultipartFile[] images, String containerId) throws IOException {

        int imageCount = 0;
        List<String> imageKeys = new ArrayList<>();

        for (MultipartFile image : images) {
            String fileName = "image_" + imageCount++ + "_" + containerId + ".jpg";

            try {
                imageKeys.add(s3Service.uploadFile(image.getBytes(), fileName, "application/jpg"));
            }
            catch (IOException e) {
                e.printStackTrace();
            }}

        return imageKeys;
    }
}
