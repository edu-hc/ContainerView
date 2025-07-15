package com.ftc.containerView.controller;

import com.ftc.containerView.infra.aws.S3Service;
import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.service.ContainerService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/containers")
public class ContainerController {

    private final ContainerService containerService;
    private final S3Service s3Service;
    private static final Logger logger = LoggerFactory.getLogger(ContainerController.class);

    @Autowired
    public ContainerController(ContainerService containerService, S3Service s3Service) {
        this.containerService = containerService;
        this.s3Service = s3Service;
    }

    @GetMapping
    public ResponseEntity<List<Container>> getAllContainers(HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("GET /containers - Buscando todos os containers. IP: {}", request.getRemoteAddr());
        List<Container> containers = containerService.getContainers();
        long execTime = System.currentTimeMillis() - startTime;
        logger.info("GET /containers concluído. Encontrados {} containers. Tempo de resposta: {}ms", containers.size(), execTime);
        return ResponseEntity.ok(containers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Container> getContainerById(@PathVariable String id, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("GET /containers/{} - Buscando container por ID. IP: {}", id, request.getRemoteAddr());
        Container container = containerService.getContainersById(id);
        long execTime = System.currentTimeMillis() - startTime;
        logger.info("Container com ID {} encontrado. Tempo de resposta: {}ms", id, execTime);
        return ResponseEntity.ok(container);
    }

    @GetMapping("/{id}/imagens")
    public ResponseEntity<List<String>> getContainerImages(@PathVariable String id) {
        Container container = containerService.getContainersById(id);
        List<String> imageKeys = container.getImages();
        List<String> imageUrls = new ArrayList<>();
        for (String key : imageKeys) {
            // 60 minutos de validade
            imageUrls.add(s3Service.generatePresignedUrl(key, 60));
        }
        return ResponseEntity.ok(imageUrls);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContainer(@PathVariable String id, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("DELETE /containers/{} - Excluindo container. IP: {}", id, request.getRemoteAddr());
        containerService.deleteContainer(id);
        long execTime = System.currentTimeMillis() - startTime;
        logger.info("Container com ID {} excluído com sucesso. Tempo de resposta: {}ms", id, execTime);
        return ResponseEntity.noContent().build();
    }
}
