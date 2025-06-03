package com.ftc.containerView.service;

import com.ftc.containerView.infra.errorhandling.exceptions.FileNotSupportedException;
import com.ftc.containerView.infra.errorhandling.exceptions.ImageExceedsMaxSizeException;
import io.micrometer.core.instrument.config.validate.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
public class ImageValidationService {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");
    private static final long MAX_SIZE = 1_200_000; // 1.2MB como no RF003

    public void validateImage(MultipartFile file) {
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new FileNotSupportedException("Tipo de arquivo não permitido");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ImageExceedsMaxSizeException("Arquivo muito grande");
        }
    }
}