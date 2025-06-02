package com.ftc.containerView.service;

import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.repositories.ContainerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContainerService {

    private static final Logger logger = LoggerFactory.getLogger(ContainerService.class);

    private final ContainerRepository containerRepository;

    @Autowired
    public ContainerService(ContainerRepository containerRepository) {
        this.containerRepository = containerRepository;
    }

    public Container saveContainer(Container container) {
        logger.info("Salvando novo container: {}", container.getId());
        try {
            Container saved = containerRepository.save(container);
            logger.info("Container salvo com sucesso: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            logger.error("Erro ao salvar container: {}. Erro: {}", container.getId(), e.getMessage(), e);
            throw e;
        }
    }

    public List<Container> getContainers() {
        logger.info("Buscando todos os containers.");
        try {
            List<Container> containers = containerRepository.findAll();
            logger.info("Encontrados {} containers.", containers.size());
            return containers;
        } catch (Exception e) {
            logger.error("Erro ao buscar containers. Erro: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Optional<Container> getContainersById(String id) {
        logger.info("Buscando container por ID: {}", id);
        try {
            Optional<Container> container = containerRepository.findById(id);
            if (container.isPresent()) {
                logger.info("Container com ID {} encontrado.", id);
            } else {
                logger.warn("Container com ID {} não encontrado.", id);
            }
            return container;
        } catch (Exception e) {
            logger.error("Erro ao buscar container com ID: {}. Erro: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    public Container updateContainer(String containerId, Container updatedContainer) {
        logger.info("Atualizando container com ID: {}", containerId);
        try {
            Container existingContainer = containerRepository.findById(containerId)
                    .orElseThrow(() -> new RuntimeException("Container não encontrado"));
            boolean changed = false;
            if (!existingContainer.getImages().equals(updatedContainer.getImages())) {
                existingContainer.setImages(updatedContainer.getImages());
                changed = true;
            }
            if (!existingContainer.getDescription().equals(updatedContainer.getDescription())) {
                existingContainer.setDescription(updatedContainer.getDescription());
                changed = true;
            }
            if (changed) {
                Container saved = containerRepository.save(existingContainer);
                logger.info("Container com ID {} atualizado com sucesso.", containerId);
                return saved;
            } else {
                logger.info("Nenhuma alteração detectada para o container com ID {}.", containerId);
                return existingContainer;
            }
        } catch (Exception e) {
            logger.error("Erro ao atualizar container com ID: {}. Erro: {}", containerId, e.getMessage(), e);
            throw e;
        }
    }

    public void deleteContainer(String id) {
        logger.info("Excluindo container com ID: {}", id);
        try {
            containerRepository.deleteById(id);
            logger.info("Container com ID {} excluído com sucesso.", id);
        } catch (Exception e) {
            logger.error("Erro ao excluir container com ID: {}. Erro: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
