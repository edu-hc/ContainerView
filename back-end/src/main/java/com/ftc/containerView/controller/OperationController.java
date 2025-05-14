package com.ftc.containerView.controller;

import com.ftc.containerView.model.operation.OperationDTO;
import com.ftc.containerView.model.operation.Operation;
import com.ftc.containerView.service.OperationService;
import com.ftc.containerView.service.StoreImageService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/operations")
public class OperationController {

    private final OperationService operationService;
    private final StoreImageService storeImageService;
    private static final Logger logger = LoggerFactory.getLogger(OperationController.class);


    @Autowired
    public OperationController(OperationService operationService, StoreImageService storeImageService) {
        this.operationService = operationService;
        this.storeImageService = storeImageService;
        logger.info("OperationController inicializado com sucesso");
    }

    @GetMapping
    public ResponseEntity <List<Operation>> getAllOperations(HttpServletRequest request) {
        logger.info("GET /operations - Buscando todas as operações. IP: {}", request.getRemoteAddr());

        long startTime = System.currentTimeMillis();

        try {
            List<Operation> operations = operationService.findOperations();

            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("GET /operations concluído. Encontradas {} operações. Tempo de resposta: {}ms",
                    operations.size(), executionTime);

            return ResponseEntity.ok(operations);
        } catch (Exception e) {
            logger.error("Erro ao buscar todas as operações. Erro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Operation> getOperationsById(@PathVariable Long id, HttpServletRequest request) {

        logger.info("GET /operations/{} - Buscando operação por ID. IP: {}", id, request.getRemoteAddr());

        long startTime = System.currentTimeMillis();

        try {
            Optional<Operation> operation = operationService.findOperationById(id);

            if (operation.isPresent()) {
                long executionTime = System.currentTimeMillis() - startTime;
                logger.info("GET /operations/{} concluído com sucesso. Tempo de resposta: {}ms", id, executionTime);
                return ResponseEntity.ok(operation.get());
            } else {
                logger.warn("Operação com ID {} não encontrada", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erro ao buscar operação com ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Operation> createOperation(@RequestParam("containerId") String containerId,
                                                     @RequestParam("containerDescription") String containerDescription,
                                                     @RequestParam("userId") Long userId,
                                                     @RequestParam(value = "images", required = false) MultipartFile[] imageFiles,
                                                     HttpServletRequest request) throws IOException {

        logger.info("POST /operations - Criando nova operação. ContainerId: {}, UserId: {}, IP: {}",
                containerId, userId, request.getRemoteAddr());

        int imageCount = imageFiles != null ? imageFiles.length : 0;
        logger.debug("Recebidos {} arquivos de imagem para o container {}", imageCount, containerId);

        long startTime = System.currentTimeMillis();

        try {
            OperationDTO operation = new OperationDTO(
                    containerId,
                    containerDescription,
                    new ArrayList<>(),
                    userId);

            if (imageFiles != null) {
                logger.debug("Processando {} imagens para o container {}", imageFiles.length, containerId);
                operation.containerImages().addAll(storeImageService.storeImages(imageFiles, operation.containerId()));
                logger.debug("Imagens processadas com sucesso para o container {}", containerId);
            }

            Operation newOperation = operationService.createOperation(operation);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("POST /operations concluído. Operação criada com ID: {}. Tempo de resposta: {}ms",
                    newOperation.getId(), executionTime);

            return new ResponseEntity<>(newOperation, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Erro ao criar operação. ContainerId: {}, UserId: {}. Erro: {}",
                    containerId, userId, e.getMessage(), e);
            throw e; // Repassando a exceção para que seja tratada pelo controlador de exceções global
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperation(@PathVariable Long id, HttpServletRequest request) {

        Optional<Operation> operation = null;
        try {
            operation = operationService.findOperationById(id);

            if (operation.isPresent()) {
                logger.info("DELETE /operations/{} - Excluindo operação. IP: {}", operation.get().getId(), request.getRemoteAddr());
                operationService.deleteOperation(operation.get());
                logger.info("Operação com ID {} excluída com sucesso", operation.get().getId());

                return ResponseEntity.noContent().build();

            } else {
                logger.warn("Operação com ID {} não encontrada", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erro ao excluir operação com ID: {}. Erro: {}", operation.get().getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
