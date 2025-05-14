package com.ftc.containerView.controller;

import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.service.ContainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/containers")
public class ContainerController {

    private final ContainerService containerService;

    @Autowired
    public ContainerController(ContainerService containerService) {
        this.containerService = containerService;
    }

    @GetMapping
    public ResponseEntity<List<Container>> getAllContainers() {
        return ResponseEntity.ok(containerService.getContainers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Container> getContainerById(@PathVariable String id) {
        Optional<Container> container = containerService.getContainersById(id);
        return container.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContainer(@PathVariable String id) {
        containerService.deleteContainer(id);
        return ResponseEntity.noContent().build();
    }
}
