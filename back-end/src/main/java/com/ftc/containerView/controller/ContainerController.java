package com.ftc.containerView.controller;

import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.service.ContainerService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/containers")
public class ContainerController {

    private final ContainerService containerService;
    private static final Logger logger = LoggerFactory.getLogger(ContainerController.class);

    @Autowired
    public ContainerController(ContainerService containerService) {
        this.containerService = containerService;
    }

    @GetMapping
    public ResponseEntity<List<Container>> getAllContainers(HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("GET /containers - Buscando todos os containers. IP: {}", request.getRemoteAddr());
        try {
            List<Container> containers = containerService.getContainers();
            long execTime = System.currentTimeMillis() - startTime;
            logger.info("GET /containers concluído. Encontrados {} containers. Tempo de resposta: {}ms", containers.size(), execTime);
            return ResponseEntity.ok(containers);
        } catch (Exception e) {
            logger.error("Erro ao buscar containers. Erro: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Container> getContainerById(@PathVariable String id, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("GET /containers/{} - Buscando container por ID. IP: {}", id, request.getRemoteAddr());
        try {
            Optional<Container> container = containerService.getContainersById(id);
            if (container.isPresent()) {
                long execTime = System.currentTimeMillis() - startTime;
                logger.info("Container com ID {} encontrado. Tempo de resposta: {}ms", id, execTime);
                return ResponseEntity.ok(container.get());
            } else {
                logger.warn("Container com ID {} não encontrado.", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erro ao buscar container com ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContainer(@PathVariable String id, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("DELETE /containers/{} - Excluindo container. IP: {}", id, request.getRemoteAddr());
        try {
            containerService.deleteContainer(id);
            long execTime = System.currentTimeMillis() - startTime;
            logger.info("Container com ID {} excluído com sucesso. Tempo de resposta: {}ms", id, execTime);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Erro ao excluir container com ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
