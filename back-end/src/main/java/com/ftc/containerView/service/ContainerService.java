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

    public void deleteContainer(Long id) {
        containerRepository.deleteById(id);
    }
}

