package com.ftc.containerView.service;

import com.ftc.containerView.model.Container;
import com.ftc.containerView.model.User;
import com.ftc.containerView.repositories.ContainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContainerService {

    private final ContainerRepository containerRepository;

    @Autowired
    public ContainerService(ContainerRepository containerRepository) {
        this.containerRepository = containerRepository;
    }

    public Container saveContainer(Container container) {
        return containerRepository.save(container);
    }

    public List<Container> getContainers() {
        return containerRepository.findAll();
    }

    public Optional<Container> getContainersById(Long id) {
        return containerRepository.findById(id);
    }

    public List<Container> getContainersByUser(User user) {
        return containerRepository.findByUser(user);
    }

    public Container updateContainer(Long containerId, Container updatedContainer) {

        Container existingContainer = containerRepository.findById(containerId)
                .orElseThrow(() -> new RuntimeException("Container não encontrado"));

        // Atualiza os campos se forem diferentes
        if (!existingContainer.getImageUrl().equals(updatedContainer.getImageUrl())) {
            existingContainer.setImageUrl(updatedContainer.getImageUrl());
        }

        if (!existingContainer.getDescription().equals(updatedContainer.getDescription())) {
            existingContainer.setDescription(updatedContainer.getDescription());
        }

        // Salva o usuário atualizado
        return containerRepository.save(existingContainer);

    }

    public void deleteContainer(Long id) {
        containerRepository.deleteById(id);
    }
}

