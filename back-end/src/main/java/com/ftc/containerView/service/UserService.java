package com.ftc.containerView.service;

import com.ftc.containerView.model.User;
import com.ftc.containerView.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
        log.info("Criando usuário: {}", user.getUsername());
        return userRepository.save(user);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUsersById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUsersByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User updateUser(Long userId, User updatedUser) {
        // Busca o usuário existente
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Usuário ID: {} não encontrado.", userId);
                    return new RuntimeException("Usuário não encontrado");
                });

        // Log dos dados antigos
        log.info("Atualizando usuário ID: {}", userId);
        log.debug("Dados antigos - Username: {}, Password: {}", existingUser.getUsername(), existingUser.getPassword());

        // Atualiza os campos se forem diferentes
        if (!existingUser.getUsername().equals(updatedUser.getUsername())) {
            log.debug("Username alterado: {} -> {}", existingUser.getUsername(), updatedUser.getUsername());
            existingUser.setUsername(updatedUser.getUsername());
        }

        if (!existingUser.getPassword().equals(updatedUser.getPassword())) {
            log.debug("Password alterado: {} -> {}", existingUser.getPassword(), updatedUser.getPassword());
            existingUser.setPassword(updatedUser.getPassword());
        }

        // Salva o usuário atualizado
        User savedUser = userRepository.save(existingUser);

        // Log dos novos dados
        log.debug("Dados atualizados - Username: {}, Password: {}", savedUser.getUsername(), savedUser.getPassword());
        log.info("Usuário ID: {} atualizado com sucesso.", userId);

        return savedUser;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}