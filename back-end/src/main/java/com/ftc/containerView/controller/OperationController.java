package com.ftc.containerView.controller;

import com.ftc.containerView.infra.errorhandling.exceptions.ImageNotFoundException;
import com.ftc.containerView.infra.errorhandling.exceptions.ImageStorageException;
import com.ftc.containerView.infra.errorhandling.exceptions.OperationNotFoundException;
import com.ftc.containerView.infra.security.auth.UserContextService;
import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.model.images.AddSackImagesResultDTO;
import com.ftc.containerView.model.images.SackImageResponseDTO;
import com.ftc.containerView.model.operation.OperationDTO;
import com.ftc.containerView.model.operation.Operation;
import com.ftc.containerView.model.operation.UpdateOperationDTO;
import com.ftc.containerView.service.OperationService;
import com.ftc.containerView.service.SackImageService;
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
    private final UserContextService userContextService;
    private static final Logger logger = LoggerFactory.getLogger(OperationController.class);
    private final SackImageService sackImageService;


    @Autowired
    public OperationController(OperationService operationService, StoreImageService storeImageService, UserContextService userContextService, SackImageService sackImageService) {
        this.operationService = operationService;
        this.storeImageService = storeImageService;
        this.userContextService = userContextService;
        logger.info("OperationController inicializado com sucesso");
        this.sackImageService = sackImageService;
    }

    @GetMapping
    public ResponseEntity <List<Operation>> getAllOperations(HttpServletRequest request) {
        logger.info("GET /operations - Buscando todas as operações. IP: {}", request.getRemoteAddr());

        long startTime = System.currentTimeMillis();

        List<Operation> operations = operationService.findOperations();

        long executionTime = System.currentTimeMillis() - startTime;
        logger.info("GET /operations concluído. Encontradas {} operações. Tempo de resposta: {}ms",
                operations.size(), executionTime);

        return ResponseEntity.ok(operations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Operation> getOperationsById(@PathVariable Long id, HttpServletRequest request) {

        logger.info("GET /operations/{} - Buscando operação por ID. IP: {}", id, request.getRemoteAddr());

        long startTime = System.currentTimeMillis();

        Operation operation = operationService.findOperationById(id);
        long executionTime = System.currentTimeMillis() - startTime;
        logger.info("GET /operations/{} concluído com sucesso. Tempo de resposta: {}ms", id, executionTime);
        return ResponseEntity.ok(operation);

    }

    @GetMapping("/{id}/sack_images")
    public ResponseEntity<List<String>> getSackImages(@PathVariable Long id, HttpServletRequest request) {

        logger.info("GET /operations/{}/sack_images - Buscando imagens de sacaria da operação. IP: {}", id, request.getRemoteAddr());

        long startTime = System.currentTimeMillis();

        List<String> sackImages = sackImageService.findSackImages(id);

        logger.info("{} imagens de sacaria encontradas para a operação de ID {}", sackImages.size(), id);

        long executionTime = System.currentTimeMillis() - startTime;
        logger.info("GET /operations/{}/sack_images concluído. Encontradas {} imagens. Tempo de resposta: {}ms",
                id, sackImages.size(), executionTime);

        return ResponseEntity.ok(sackImages);
    }

    @PostMapping
    public ResponseEntity<Operation> createOperation(@RequestBody OperationDTO operationDTO,
                                                     HttpServletRequest request) throws IOException {

        Long userId = userContextService.getCurrentUserId();

        logger.info("POST /operations - Criando nova operação. UserId: {}, IP: {}",
                userId, request.getRemoteAddr());


        long startTime = System.currentTimeMillis();


        Operation newOperation = operationService.createOperation(operationDTO, userId);

        long executionTime = System.currentTimeMillis() - startTime;
        logger.info("POST /operations concluído. Operação criada com ID: {}. Tempo de resposta: {}ms",
                 newOperation.getId(), executionTime);

        return new ResponseEntity<>(newOperation, HttpStatus.CREATED);
    }

    @PostMapping(path = "/{id}/sack-images", consumes = "multipart/form-data")
    public ResponseEntity<Operation> addSackImagesToOperation(
            @PathVariable Long id,
            @RequestParam("sackImages") MultipartFile[] sackImages,
            HttpServletRequest request) {

        long startTime = System.currentTimeMillis();
        Long userId = userContextService.getCurrentUserId();

        logger.info("POST /operations/{}/sack-images - Adicionando imagens de sacaria. UserId: {}, IP: {}, Quantidade: {}",
                id, userId, request.getRemoteAddr(), sackImages != null ? sackImages.length : 0);

        try {
            // Validar se há imagens
            if (sackImages == null || sackImages.length == 0) {
                logger.warn("Nenhuma imagem de sacaria fornecida para operação {}", id);
                return ResponseEntity.badRequest().build();
            }

            // Buscar a operação existente
            Operation operation = operationService.findOperationById(id);

            // Adicionar imagens através do service
            AddSackImagesResultDTO result = operationService.addSackImagesToOperation(
                    operation,
                    sackImages,
                    userId
            );

            long execTime = System.currentTimeMillis() - startTime;
            logger.info("POST /operations/{}/sack-images concluído. {} imagens adicionadas. Tempo de resposta: {}ms",
                    id, result.totalImagesAdded(), execTime);

            return ResponseEntity.ok(result.updatedOperation());

        } catch (OperationNotFoundException e) {
            logger.error("Operação não encontrada: {}", id);
            return ResponseEntity.notFound().build();
        } catch (ImageStorageException e) {
            logger.error("Erro ao armazenar imagens: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("Erro ao adicionar imagens de sacaria à operação {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/sack-images")
    public ResponseEntity<List<SackImageResponseDTO>> getSackImages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "60") int expirationMinutes,
            HttpServletRequest request) {

        long startTime = System.currentTimeMillis();

        logger.info("GET /operations/{}/sack-images - Buscando imagens de sacaria. IP: {}",
                id, request.getRemoteAddr());

        try {
            Operation operation = operationService.findOperationById(id);

            List<SackImageResponseDTO> imageUrls = operationService.getSackImagesWithUrls(
                    operation,
                    expirationMinutes
            );

            long execTime = System.currentTimeMillis() - startTime;
            logger.info("GET /operations/{}/sack-images concluído. {} imagens encontradas. Tempo de resposta: {}ms",
                    id, imageUrls.size(), execTime);

            return ResponseEntity.ok(imageUrls);

        } catch (OperationNotFoundException e) {
            logger.error("Operação não encontrada: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{operationId}/sack-images/{imageId}")
    public ResponseEntity<Void> deleteSackImage(
            @PathVariable Long operationId,
            @PathVariable Long imageId,
            HttpServletRequest request) {

        long startTime = System.currentTimeMillis();
        Long userId = userContextService.getCurrentUserId();

        logger.info("DELETE /operations/{}/sack-images/{} - Removendo imagem de sacaria. UserId: {}, IP: {}",
                operationId, imageId, userId, request.getRemoteAddr());

        try {
            operationService.deleteSackImage(operationId, imageId, userId);

            long execTime = System.currentTimeMillis() - startTime;
            logger.info("Imagem de sacaria {} removida da operação {} com sucesso. Tempo de resposta: {}ms",
                    imageId, operationId, execTime);

            return ResponseEntity.noContent().build();

        } catch (OperationNotFoundException | ImageNotFoundException e) {
            logger.error("Operação ou imagem não encontradas: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erro ao deletar imagem de sacaria: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperation(@PathVariable Long id, HttpServletRequest request) {

        Operation operation = operationService.findOperationById(id);
        logger.info("DELETE /operations/{} - Excluindo operação. IP: {}", operation.getId(), request.getRemoteAddr());
        operationService.deleteOperation(operation);
        logger.info("Operação com ID {} excluída com sucesso", operation.getId());
        return ResponseEntity.noContent().build();

    }

    @PutMapping("/{id}")
    public ResponseEntity<Operation> updateOperation(@PathVariable Long id,
                                                     @RequestBody UpdateOperationDTO updateOperationDTO,
                                                     HttpServletRequest request) {
        long startTime = System.currentTimeMillis();

        Long userId = userContextService.getCurrentUserId();

        logger.info("PUT /operations/{} - Atualizando operação. UserId: {}, IP: {}",
                id, userId, request.getRemoteAddr());

        Operation updatedOperation = operationService.updateOperation(id, updateOperationDTO, userId);

        long execTime = System.currentTimeMillis() - startTime;
        logger.info("Operação com ID {} atualizada com sucesso. Tempo de resposta: {}ms",
                id, execTime);

        return ResponseEntity.ok(updatedOperation);
    }

    @PatchMapping("/{operationId}/status")
    public ResponseEntity<Operation> completeOperationStatus(@PathVariable Long operationId, HttpServletRequest request) {

        long startTime = System.currentTimeMillis();

        logger.info("PATCH /operations/{} - Finalizando operação. IP: {}",
                operationId, request.getRemoteAddr());

        Operation updatedOperation = operationService.completeOperationStatus(operationId);

        long execTime = System.currentTimeMillis() - startTime;
        logger.info("Status da operação com ID {} atualizada (FINALIZADO) com sucesso. Tempo de resposta: {}ms",
                operationId, execTime);

        return ResponseEntity.ok(updatedOperation);
    }
}
