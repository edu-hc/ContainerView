package com.ftc.containerView.controller;

import com.ftc.containerView.model.Container;
import com.ftc.containerView.repositories.UserRepository;
import com.ftc.containerView.service.ContainerService;
import com.ftc.containerView.service.LogService;
import com.ftc.containerView.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/containers")
public class ContainerController {

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";
    private final ContainerService containerService;
    private final LogService logService;
    private final UserService userService;
    //private final FirebaseStorageService firebaseStorageService;

    @Autowired
    public ContainerController(ContainerService containerService, LogService logService, UserService userService/*, FirebaseStorageService firebaseStorageService*/) {
        this.containerService = containerService;
        this.logService = logService;
        //this.firebaseStorageService = firebaseStorageService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Container>> getAllContainers() {
        return ResponseEntity.ok(containerService.getContainers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Container> getContainerById(@PathVariable Long id) {
        Optional<Container> container = containerService.getContainersById(id);
        return container.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Container> createContainer(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("description") String description) throws IOException {

        //String imageUrl = firebaseStorageService.uploadFile(file);
        Container container = new Container();
        container.setUser(userService.getUsersById(userId).orElseThrow(() -> new RuntimeException("Usuário não encontrado")));
        container.setDescription(description);

        // Cria o diretório de upload se não existir
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Gera um nome único para o arquivo
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // Salva o arquivo no diretório de upload
        Path filePath = Paths.get(UPLOAD_DIR + fileName);
        Files.copy(file.getInputStream(), filePath);

        // Gera a URL do arquivo
        container.setImageUrl("http://localhost:8080/uploads/" + fileName);

        logService.saveLog("Container created", LocalDateTime.now().toString());
        return ResponseEntity.ok(containerService.saveContainer(container));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContainer(@PathVariable Long id) {
        containerService.deleteContainer(id);
        return ResponseEntity.noContent().build();
    }
}
