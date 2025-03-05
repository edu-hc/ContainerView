package com.ftc.containerView.service;

import com.ftc.containerView.model.Container;
import com.ftc.containerView.model.User;
import com.ftc.containerView.repository.ContainerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContainerService {

    private final ContainerRepository containerRepository;

    public ContainerService(ContainerRepository containerRepository) {
        this.containerRepository = containerRepository;
    }

    public Container saveContainer(Container container) {
        return containerRepository.save(container);
    }

    public List<Container> getContainers() {
        return containerRepository.findAll();
    }

    public List<Container> getContainersByUser(User user) {
        return containerRepository.findByUser(user);
    }
}

