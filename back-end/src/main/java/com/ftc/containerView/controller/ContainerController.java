package com.ftc.containerView.controller;

import com.ftc.containerView.infra.aws.S3Service;
import com.ftc.containerView.infra.security.auth.UserContextService;
import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.model.container.ContainerStatus;
import com.ftc.containerView.model.container.CreateContainerDTO;
import com.ftc.containerView.model.images.ContainerImage;
import com.ftc.containerView.model.images.ContainerImageCategory;
import com.ftc.containerView.repositories.ContainerRepository;
import com.ftc.containerView.repositories.OperationRepository;
import com.ftc.containerView.repositories.UserRepository;
import com.ftc.containerView.service.ContainerService;
import com.ftc.containerView.service.StoreImageService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final Logger logger = LoggerFactory.getLogger(ContainerController.class);

    @Autowired
    public ContainerController(ContainerService containerService, S3Service s3Service, UserContextService userContextService, UserRepository userRepository, OperationRepository operationRepository, StoreImageService storeImageService, ContainerRepository containerRepository) {
        this.containerService = containerService;
        this.s3Service = s3Service;
        this.userContextService = userContextService;
        this.storeImageService = storeImageService;
        this.containerRepository = containerRepository;
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
    public ResponseEntity<List<String>> getContainerImagesVazioForrado(@PathVariable String id) {
        Container container = containerService.getContainersByContainerId(id);
        List<String> imageKeys = container.getContainerImages().stream()
                .filter(image -> image.getCategory() == ContainerImageCategory.VAZIO_FORRADO)
                .map(ContainerImage::getImageKey).toList(); // pega o imageKey;
        List<String> imageUrls = new ArrayList<>();
        for (String key : imageKeys) {
            // 60 minutos de validade
            imageUrls.add(s3Service.generatePresignedUrl(key, 60));
        }
        return ResponseEntity.ok(imageUrls);
    }

    @GetMapping("/{id}/images/FIADA")
    public ResponseEntity<List<String>> getContainerImagesFiada(@PathVariable String id) {
        Container container = containerService.getContainersByContainerId(id);
        List<String> imageKeys = container.getContainerImages().stream()
                .filter(image -> image.getCategory() == ContainerImageCategory.FIADA)
                .map(ContainerImage::getImageKey).toList(); // pega o imageKey;
        List<String> imageUrls = new ArrayList<>();
        for (String key : imageKeys) {
            // 60 minutos de validade
            imageUrls.add(s3Service.generatePresignedUrl(key, 60));
        }
        return ResponseEntity.ok(imageUrls);
    }

    @GetMapping("/{id}/images/CHEIO_ABERTO")
    public ResponseEntity<List<String>> getContainerImagesCheioAberto(@PathVariable String id) {
        Container container = containerService.getContainersByContainerId(id);
        List<String> imageKeys = container.getContainerImages().stream()
                .filter(image -> image.getCategory() == ContainerImageCategory.CHEIO_ABERTO)
                .map(ContainerImage::getImageKey).toList(); // pega o imageKey;
        List<String> imageUrls = new ArrayList<>();
        for (String key : imageKeys) {
            // 60 minutos de validade
            imageUrls.add(s3Service.generatePresignedUrl(key, 60));
        }
        return ResponseEntity.ok(imageUrls);
    }

    @GetMapping("/{id}/images/MEIA_PORTA")
    public ResponseEntity<List<String>> getContainerImagesMeiaPorta(@PathVariable String id) {
        Container container = containerService.getContainersByContainerId(id);
        List<String> imageKeys = container.getContainerImages().stream()
                .filter(image -> image.getCategory() == ContainerImageCategory.MEIA_PORTA)
                .map(ContainerImage::getImageKey).toList(); // pega o imageKey;
        List<String> imageUrls = new ArrayList<>();
        for (String key : imageKeys) {
            // 60 minutos de validade
            imageUrls.add(s3Service.generatePresignedUrl(key, 60));
        }
        return ResponseEntity.ok(imageUrls);
    }

    @GetMapping("/{id}/images/LACRADO_FECHADO")
    public ResponseEntity<List<String>> getContainerImagesLacradoFechado(@PathVariable String id) {
        Container container = containerService.getContainersByContainerId(id);
        List<String> imageKeys = container.getContainerImages().stream()
                .filter(image -> image.getCategory() == ContainerImageCategory.LACRADO_FECHADO)
                .map(ContainerImage::getImageKey).toList(); // pega o imageKey;
        List<String> imageUrls = new ArrayList<>();
        for (String key : imageKeys) {
            // 60 minutos de validade
            imageUrls.add(s3Service.generatePresignedUrl(key, 60));
        }
        return ResponseEntity.ok(imageUrls);
    }

    @GetMapping("/{id}/images/LACRES_PRINCIPAIS")
    public ResponseEntity<List<String>> getContainerImagesLacresPrincipais(@PathVariable String id) {
        Container container = containerService.getContainersByContainerId(id);
        List<String> imageKeys = container.getContainerImages().stream()
                .filter(image -> image.getCategory() == ContainerImageCategory.LACRES_PRINCIPAIS)
                .map(ContainerImage::getImageKey).toList(); // pega o imageKey;
        List<String> imageUrls = new ArrayList<>();
        for (String key : imageKeys) {
            // 60 minutos de validade
            imageUrls.add(s3Service.generatePresignedUrl(key, 60));
        }
        return ResponseEntity.ok(imageUrls);
    }

    @GetMapping("/{id}/images/LACRES_OUTROS")
    public ResponseEntity<List<String>> getContainerImagesLacresOutros(@PathVariable String id) {
        Container container = containerService.getContainersByContainerId(id);
        List<String> imageKeys = container.getContainerImages().stream()
                .filter(image -> image.getCategory() == ContainerImageCategory.LACRES_OUTROS)
                .map(ContainerImage::getImageKey).toList(); // pega o imageKey;
        List<String> imageUrls = new ArrayList<>();
        for (String key : imageKeys) {
            // 60 minutos de validade
            imageUrls.add(s3Service.generatePresignedUrl(key, 60));
        }
        return ResponseEntity.ok(imageUrls);
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

