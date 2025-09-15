package com.ftc.containerView.controller;

import com.ftc.containerView.infra.aws.S3Service;
import com.ftc.containerView.infra.errorhandling.exceptions.ContainerNotFoundException;
import com.ftc.containerView.infra.security.auth.UserContextService;
import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.model.container.ContainerStatus;
import com.ftc.containerView.model.container.CreateContainerDTO;
import com.ftc.containerView.model.container.UpdateContainerDTO;
import com.ftc.containerView.model.images.AddImagesToContainerResultDTO;
import com.ftc.containerView.model.images.ContainerImage;
import com.ftc.containerView.model.images.ContainerImageCategory;
import com.ftc.containerView.repositories.ContainerRepository;
import com.ftc.containerView.repositories.OperationRepository;
import com.ftc.containerView.repositories.UserRepository;
import com.ftc.containerView.service.ContainerImageService;
import com.ftc.containerView.service.ContainerService;
import com.ftc.containerView.service.StoreImageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/containers")
public class ContainerController {

    private final ContainerService containerService;
    private final S3Service s3Service;
    private final UserContextService userContextService;
    private final StoreImageService storeImageService;
    private final ContainerRepository containerRepository;
    private final ContainerImageService containerImageService;
    private static final Logger logger = LoggerFactory.getLogger(ContainerController.class);

    @Autowired
    public ContainerController(ContainerService containerService, S3Service s3Service, UserContextService userContextService, UserRepository userRepository, OperationRepository operationRepository, StoreImageService storeImageService, ContainerRepository containerRepository, ContainerImageService containerImageService) {
        this.containerService = containerService;
        this.s3Service = s3Service;
        this.userContextService = userContextService;
        this.storeImageService = storeImageService;
        this.containerRepository = containerRepository;
        this.containerImageService = containerImageService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Container> createContainer(@RequestParam("containerId") String containerId,
                                                     @RequestParam("description") String description,
                                                     @RequestParam("operationId") Long operationId,
                                                     @RequestParam("sacksCount") int sacksCount,
                                                     @RequestParam("tareTons") float tareTons,
                                                     @RequestParam("liquidWeight") float liquidWeight,
                                                     @RequestParam("grossWeight") float grossWeight,
                                                     @RequestParam("agencySeal") String agencySeal,
                                                     @RequestParam("otherSeals") List<String> otherSeals,
                                                     HttpServletRequest request) {

        Long userId = userContextService.getCurrentUserId();

        logger.info("POST /containers - Criando novo container. ContainerId: {}, UserId: {}, IP: {}",
                containerId, userId, request.getRemoteAddr());


        long startTime = System.currentTimeMillis();

        CreateContainerDTO containerDTO = new CreateContainerDTO(containerId, description, new ArrayList<>(), userId, operationId,
                sacksCount, tareTons, liquidWeight, grossWeight, agencySeal, otherSeals, ContainerStatus.OPEN);

        Container newContainer = containerService.createContainer(containerDTO);

        containerRepository.save(newContainer);

        long executionTime = System.currentTimeMillis() - startTime;
        logger.info("POST /containers concluído. Container criado com ID: {}. Tempo de resposta: {}ms",
                newContainer.getId(), executionTime);

        return ResponseEntity.status(201).body(newContainer);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Container>> createMultipleContainers(
            @RequestBody @Valid List<CreateContainerDTO> containers,
            HttpServletRequest request) {

        long startTime = System.currentTimeMillis();
        Long userId = userContextService.getCurrentUserId();

        logger.info("POST /containers/batch - Criando {} containers. UserId: {}, IP: {}",
                containers.size(), userId, request.getRemoteAddr());

        try {
            // Validar quantidade
            if (containers.isEmpty()) {
                logger.warn("Lista de containers vazia");
                return ResponseEntity.badRequest().build();
            }

            if (containers.size() > 50) {
                logger.error("Tentativa de criar {} containers. Máximo permitido: 50", containers.size());
                return ResponseEntity.badRequest().build();
            }

            // Criar containers usando o service
            List<Container> createdContainers = containerService.createMultipleContainers(containers, userId);

            long execTime = System.currentTimeMillis() - startTime;
            logger.info("POST /containers/batch concluído. {} containers criados. Tempo: {}ms",
                    createdContainers.size(), execTime);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdContainers);

        } catch (Exception e) {
            logger.error("Erro ao criar múltiplos containers: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(path = "/images", consumes = "multipart/form-data")
    public ResponseEntity<Container> createContainerWithImages(@RequestParam("containerId") String containerId,
                                                     @RequestParam("description") String description,
                                                     @RequestParam("operationId") Long operationId,
                                                     @RequestParam("sacksCount") int sacksCount,
                                                     @RequestParam("tareTons") float tareTons,
                                                     @RequestParam("liquidWeight") float liquidWeight,
                                                     @RequestParam("grossWeight") float grossWeight,
                                                     @RequestParam("agencySeal") String agencySeal,
                                                     @RequestParam("otherSeals") List<String> otherSeals,

                                                     @RequestParam(value = "vazioForrado", required = false) MultipartFile[] vazioForradoImages,
                                                     @RequestParam(value = "fiada", required = false) MultipartFile[] fiadaImages,
                                                     @RequestParam(value = "cheioAberto", required = false) MultipartFile[] cheioAbertoImages,
                                                     @RequestParam(value = "meiaPorta", required = false) MultipartFile[] meiaPortaImages,
                                                     @RequestParam(value = "lacradoFechado", required = false) MultipartFile[] lacradoFechadoImages,
                                                     @RequestParam(value = "lacresPrincipal", required = false) MultipartFile[] lacresPrincipalImages,
                                                     @RequestParam(value = "lacresOutros", required = false) MultipartFile[] lacresOutrosImages,


                                                     HttpServletRequest request) {

        Long userId = userContextService.getCurrentUserId();

        logger.info("POST /containers/images - Criando novo container. ContainerId: {}, UserId: {}, IP: {}",
                containerId, userId, request.getRemoteAddr());


        long startTime = System.currentTimeMillis();

        CreateContainerDTO containerDTO = new CreateContainerDTO(containerId, description, new ArrayList<>(), userId, operationId,
                sacksCount, tareTons, liquidWeight, grossWeight, agencySeal, otherSeals, ContainerStatus.PENDING);

        Container newContainer = containerService.createContainer(containerDTO);

        List<ContainerImage> containerImages = new ArrayList<>();

        containerImages.addAll(storeImageService.storeImagesToContainer(vazioForradoImages, newContainer.getId(), ContainerImageCategory.VAZIO_FORRADO));
        containerImages.addAll(storeImageService.storeImagesToContainer(fiadaImages, newContainer.getId(), ContainerImageCategory.FIADA));
        containerImages.addAll(storeImageService.storeImagesToContainer(cheioAbertoImages, newContainer.getId(), ContainerImageCategory.CHEIO_ABERTO));
        containerImages.addAll(storeImageService.storeImagesToContainer(meiaPortaImages, newContainer.getId(), ContainerImageCategory.MEIA_PORTA));
        containerImages.addAll(storeImageService.storeImagesToContainer(lacradoFechadoImages, newContainer.getId(), ContainerImageCategory.LACRADO_FECHADO));
        containerImages.addAll(storeImageService.storeImagesToContainer(lacresPrincipalImages, newContainer.getId(), ContainerImageCategory.LACRES_PRINCIPAIS));
        containerImages.addAll(storeImageService.storeImagesToContainer(lacresOutrosImages, newContainer.getId(), ContainerImageCategory.LACRES_OUTROS));

        containerService.validateMandatoryCategories(containerImages);

        newContainer.getContainerImages().addAll(containerImages); // Adiciona as imagens ao containerContainerImages(containerImages);
        containerRepository.save(newContainer);

        long executionTime = System.currentTimeMillis() - startTime;
        logger.info("POST /containers/images concluído. Container criado com ID: {}. Tempo de resposta: {}ms",
                newContainer.getId(), executionTime);

        return ResponseEntity.status(201).body(newContainer);
    }

    @PostMapping(path = "/{containerId}/images", consumes = "multipart/form-data")
    public ResponseEntity<Container> addImagesToContainer(
            @PathVariable String containerId,
            @RequestParam(value = "vazioForrado", required = false) MultipartFile[] vazioForradoImages,
            @RequestParam(value = "fiada", required = false) MultipartFile[] fiadaImages,
            @RequestParam(value = "cheioAberto", required = false) MultipartFile[] cheioAbertoImages,
            @RequestParam(value = "meiaPorta", required = false) MultipartFile[] meiaPortaImages,
            @RequestParam(value = "lacradoFechado", required = false) MultipartFile[] lacradoFechadoImages,
            @RequestParam(value = "lacresPrincipal", required = false) MultipartFile[] lacresPrincipalImages,
            @RequestParam(value = "lacresOutros", required = false) MultipartFile[] lacresOutrosImages,
            @RequestParam(value = "validateMandatory", defaultValue = "false") boolean validateMandatory,
            HttpServletRequest request) {

        long startTime = System.currentTimeMillis();
        Long userId = userContextService.getCurrentUserId();

        logger.info("POST /containers/{}/images - Adicionando imagens ao container. UserId: {}, IP: {}",
                containerId, userId, request.getRemoteAddr());

        try {
            // Buscar o container existente
            Container container = containerService.getContainersByContainerId(containerId);

            // Adicionar imagens através do service
            AddImagesToContainerResultDTO result = containerService.addImagesToContainer(
                    container,
                    vazioForradoImages,
                    fiadaImages,
                    cheioAbertoImages,
                    meiaPortaImages,
                    lacradoFechadoImages,
                    lacresPrincipalImages,
                    lacresOutrosImages,
                    userId
            );

            long execTime = System.currentTimeMillis() - startTime;
            logger.info("POST /containers/{}/images concluído. {} imagens adicionadas. Tempo de resposta: {}ms",
                    containerId, result.totalImagesAdded(), execTime);

            return ResponseEntity.ok(result.updatedContainer());

        } catch (ContainerNotFoundException e) {
            logger.error("Container não encontrado: {}", containerId);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao adicionar imagens: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao adicionar imagens ao container {}: {}", containerId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{containerId}")
    public ResponseEntity<Container> updateContainer(@PathVariable String containerId,
                                                     @RequestBody UpdateContainerDTO updateContainerDTO,
                                                     HttpServletRequest request) {
        long startTime = System.currentTimeMillis();

        Long userId = userContextService.getCurrentUserId();

        logger.info("PUT /containers/{} - Atualizando container. UserId: {}, IP: {}",
                containerId, userId, request.getRemoteAddr());

        Container updatedContainer = containerService.updateContainer(containerId, updateContainerDTO, userId);

        long execTime = System.currentTimeMillis() - startTime;
        logger.info("Container com ID {} atualizado com sucesso. Tempo de resposta: {}ms",
                containerId, execTime);

        return ResponseEntity.ok(updatedContainer);
    }

    @PatchMapping("/{containerId}/status")
    public ResponseEntity<Container> completeContainerStatus(@PathVariable String containerId, HttpServletRequest request) {

        long startTime = System.currentTimeMillis();

        logger.info("PATCH /containers/{} - Completando status do container. IP: {}",
                containerId, request.getRemoteAddr());

        Container updatedContainer = containerService.completeContainerStatus(containerId);

        long execTime = System.currentTimeMillis() - startTime;
        logger.info("Status do container com ID {} atualizado (FINALIZADO) com sucesso. Tempo de resposta: {}ms",
                containerId, execTime);

        return ResponseEntity.ok(updatedContainer);
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
    public ResponseEntity<Container> getContainerByContainerId(@PathVariable String id, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("GET /containers/{} - Buscando container por ID. IP: {}", id, request.getRemoteAddr());
        Container container = containerService.getContainersByContainerId(id);
        long execTime = System.currentTimeMillis() - startTime;
        logger.info("Container com ID {} encontrado. Tempo de resposta: {}ms", id, execTime);
        return ResponseEntity.ok(container);
    }

    @GetMapping("/{id}/images/VAZIO_FORRADO")
    public ResponseEntity<List<String>> getContainerImagesVazioForrado(@PathVariable String id, HttpServletRequest request) {
        logger.info("GET /containers/{}/images/VAZIO_FORRADO - Buscando imagens categorizadas do container. IP: {}", id, request.getRemoteAddr());

        long startTime = System.currentTimeMillis();

        List<String> containerImages = containerImageService.findContainerImagesByCategory(ContainerImageCategory.VAZIO_FORRADO, id);

        logger.info("{} imagens de categoria VAZIO_FORRADO encontradas para o container de ID {}", containerImages.size(), id);

        long executionTime = System.currentTimeMillis() - startTime;
        logger.info("GET /containers/{}/images/VAZIO_FORRADO concluído. Encontradas {} imagens. Tempo de resposta: {}ms",
                id, containerImages.size(), executionTime);

        return ResponseEntity.ok(containerImages);
    }

    @GetMapping("/{id}/images/FIADA")
    public ResponseEntity<List<String>> getContainerImagesFiada(@PathVariable String id, HttpServletRequest request) {
        logger.info("GET /containers/{}/images/FIADA - Buscando imagens categorizadas do container. IP: {}", id, request.getRemoteAddr());

        long startTime = System.currentTimeMillis();

        List<String> containerImages = containerImageService.findContainerImagesByCategory(ContainerImageCategory.FIADA, id);

        logger.info("{} imagens de categoria FIADA encontradas para o container de ID {}", containerImages.size(), id);

        long executionTime = System.currentTimeMillis() - startTime;
        logger.info("GET /containers/{}/images/FIADA concluído. Encontradas {} imagens. Tempo de resposta: {}ms",
                id, containerImages.size(), executionTime);

        return ResponseEntity.ok(containerImages);
    }

    @GetMapping("/{id}/images/CHEIO_ABERTO")
    public ResponseEntity<List<String>> getContainerImagesCheioAberto(@PathVariable String id, HttpServletRequest request) {
        logger.info("GET /containers/{}/images/CHEIO_ABERTO - Buscando imagens categorizadas do container. IP: {}", id, request.getRemoteAddr());

        long startTime = System.currentTimeMillis();

        List<String> containerImages = containerImageService.findContainerImagesByCategory(ContainerImageCategory.CHEIO_ABERTO, id);

        logger.info("{} imagens de categoria CHEIO_ABERTO encontradas para o container de ID {}", containerImages.size(), id);

        long executionTime = System.currentTimeMillis() - startTime;
        logger.info("GET /containers/{}/images/CHEIO_ABERTO concluído. Encontradas {} imagens. Tempo de resposta: {}ms",
                id, containerImages.size(), executionTime);

        return ResponseEntity.ok(containerImages);
    }

    @GetMapping("/{id}/images/MEIA_PORTA")
    public ResponseEntity<List<String>> getContainerImagesMeiaPorta(@PathVariable String id, HttpServletRequest request) {
        logger.info("GET /containers/{}/images/MEIA_PORTA - Buscando imagens categorizadas do container. IP: {}", id, request.getRemoteAddr());

        long startTime = System.currentTimeMillis();

        List<String> containerImages = containerImageService.findContainerImagesByCategory(ContainerImageCategory.MEIA_PORTA, id);

        logger.info("{} imagens de categoria MEIA_PORTA encontradas para o container de ID {}", containerImages.size(), id);

        long executionTime = System.currentTimeMillis() - startTime;
        logger.info("GET /containers/{}/images/MEIA_PORTA concluído. Encontradas {} imagens. Tempo de resposta: {}ms",
                id, containerImages.size(), executionTime);

        return ResponseEntity.ok(containerImages);
    }

    @GetMapping("/{id}/images/LACRADO_FECHADO")
    public ResponseEntity<List<String>> getContainerImagesLacradoFechado(@PathVariable String id, HttpServletRequest request) {
        logger.info("GET /containers/{}/images/LACRADO_FECHADO - Buscando imagens categorizadas do container. IP: {}", id, request.getRemoteAddr());

        long startTime = System.currentTimeMillis();

        List<String> containerImages = containerImageService.findContainerImagesByCategory(ContainerImageCategory.LACRADO_FECHADO, id);

        logger.info("{} imagens de categoria LACRADO_FECHADO encontradas para o container de ID {}", containerImages.size(), id);

        long executionTime = System.currentTimeMillis() - startTime;
        logger.info("GET /containers/{}/images/LACRADO_FECHADO concluído. Encontradas {} imagens. Tempo de resposta: {}ms",
                id, containerImages.size(), executionTime);

        return ResponseEntity.ok(containerImages);
    }

    @GetMapping("/{id}/images/LACRES_PRINCIPAIS")
    public ResponseEntity<List<String>> getContainerImagesLacresPrincipais(@PathVariable String id, HttpServletRequest request) {
        logger.info("GET /containers/{}/images/LACRES_PRINCIPAIS - Buscando imagens categorizadas do container. IP: {}", id, request.getRemoteAddr());

        long startTime = System.currentTimeMillis();

        List<String> containerImages = containerImageService.findContainerImagesByCategory(ContainerImageCategory.LACRES_PRINCIPAIS, id);

        logger.info("{} imagens de categoria LACRES_PRINCIPAIS encontradas para o container de ID {}", containerImages.size(), id);

        long executionTime = System.currentTimeMillis() - startTime;
        logger.info("GET /containers/{}/images/LACRES_PRINCIPAIS concluído. Encontradas {} imagens. Tempo de resposta: {}ms",
                id, containerImages.size(), executionTime);

        return ResponseEntity.ok(containerImages);
    }

    @GetMapping("/{id}/images/LACRES_OUTROS")
    public ResponseEntity<List<String>> getContainerImagesLacresOutros(@PathVariable String id, HttpServletRequest request) {
        logger.info("GET /containers/{}/images/LACRES_OUTROS - Buscando imagens categorizadas do container. IP: {}", id, request.getRemoteAddr());

        long startTime = System.currentTimeMillis();

        List<String> containerImages = containerImageService.findContainerImagesByCategory(ContainerImageCategory.LACRES_OUTROS, id);

        logger.info("{} imagens de categoria LACRES_OUTROS encontradas para o container de ID {}", containerImages.size(), id);

        long executionTime = System.currentTimeMillis() - startTime;
        logger.info("GET /containers/{}/images/LACRES_OUTROS concluído. Encontradas {} imagens. Tempo de resposta: {}ms",
                id, containerImages.size(), executionTime);

        return ResponseEntity.ok(containerImages);
    }

    @DeleteMapping("/{containerId}")
    public ResponseEntity<Void> deleteContainer(@PathVariable String containerId, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("DELETE /containers/{} - Excluindo container. IP: {}", containerId, request.getRemoteAddr());
        containerService.deleteContainer(containerId);
        long execTime = System.currentTimeMillis() - startTime;
        logger.info("Container com ID {} excluído com sucesso. Tempo de resposta: {}ms", containerId, execTime);
        return ResponseEntity.noContent().build();
    }


}

