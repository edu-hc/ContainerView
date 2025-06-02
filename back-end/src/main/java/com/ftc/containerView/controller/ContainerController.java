package com.ftc.containerView.controller;

import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.service.ContainerService;
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

    private static final Logger logger = LoggerFactory.getLogger(ContainerController.class);

    private final ContainerService containerService;

    @Autowired
    public ContainerController(ContainerService containerService) {
        this.containerService = containerService;
    }

    @GetMapping
    public ResponseEntity<List<Container>> getAllContainers() {
        logger.info("GET /containers - Buscando todos os containers.");
        try {
            List<Container> containers = containerService.getContainers();
            logger.info("GET /containers concluído. Encontrados {} containers.", containers.size());
            return ResponseEntity.ok(containers);
        } catch (Exception e) {
            logger.error("Erro ao buscar containers. Erro: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Container> getContainerById(@PathVariable String id) {
        logger.info("GET /containers/{} - Buscando container por ID.", id);
        try {
            Optional<Container> container = containerService.getContainersById(id);
            if (container.isPresent()) {
                logger.info("Container com ID {} encontrado.", id);
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
    public ResponseEntity<Void> deleteContainer(@PathVariable String id) {
        logger.info("DELETE /containers/{} - Excluindo container.", id);
        try {
            containerService.deleteContainer(id);
            logger.info("Container com ID {} excluído com sucesso.", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Erro ao excluir container com ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
