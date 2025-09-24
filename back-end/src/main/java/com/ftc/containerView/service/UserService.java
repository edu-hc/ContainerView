package com.ftc.containerView.service;

import com.ftc.containerView.infra.errorhandling.exceptions.UserNotFoundException;
import com.ftc.containerView.infra.security.InputSanitizer;
import com.ftc.containerView.model.user.User;
import com.ftc.containerView.model.user.UserRole;
import com.ftc.containerView.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final InputSanitizer inputSanitizer;

    @Autowired
    public UserService(UserRepository userRepository, InputSanitizer inputSanitizer) {
        this.userRepository = userRepository;
        this.inputSanitizer = inputSanitizer;
    }

    public User saveUser(User user) {
        logger.info("Salvando novo usuário: {}", user.getCpf());
        try {
            // Sanitização dos campos de texto livre
            user.setFirstName(inputSanitizer.sanitizePlainText(user.getFirstName()));
            user.setLastName(inputSanitizer.sanitizePlainText(user.getLastName()));
            user.setEmail(inputSanitizer.sanitizePlainText(user.getEmail()));
            user.setCpf(inputSanitizer.sanitizeDocument(user.getCpf()));
            user.setPassword(inputSanitizer.sanitizePlainText(user.getPassword()));

            User saved = userRepository.save(user);
            logger.info("Usuário salvo com sucesso: {}", saved.getCpf());
            return saved;
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao salvar usuário: {}. Erro: {}", user.getCpf(), e.getMessage(), e);
            throw new UserNotFoundException("Usuário nao encontrado com ID: " + user.getId());
        }
    }

    public User registerUser(User user) {
        logger.info("Registrando novo usuário: {}", user.getCpf());
        try {
            // Sanitização dos campos de texto livre
            user.setFirstName(inputSanitizer.sanitizePlainText(user.getFirstName()));
            user.setLastName(inputSanitizer.sanitizePlainText(user.getLastName()));

            User saved = userRepository.save(user);
            logger.info("Usuário registrado com sucesso: {}", saved.getCpf());
            return saved;
        } catch (IllegalArgumentException e) {
            logger.error("Erro ao registrar usuário: {}. Erro: {}", user.getCpf(), e.getMessage(), e);
            throw new UserNotFoundException("Usuário nao encontrado com ID: " + user.getId());
        }
    }



    public Page<User> getUsers(Pageable pageable) {
        logger.info("Buscando usuários com paginação - Página: {}, Tamanho: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<User> users = userRepository.findAll(pageable);

        logger.info("Encontrados {} usuários na página {} de {}",
                users.getNumberOfElements(),
                users.getNumber() + 1,
                users.getTotalPages());

        return users;
    }

    public Page<User> getUsersByRole(UserRole role, Pageable pageable) {
        logger.info("Buscando usuários com role {} - Página: {}, Tamanho: {}",
                role, pageable.getPageNumber(), pageable.getPageSize());

        Page<User> users = userRepository.findByRole(role, pageable);

        logger.info("Encontrados {} usuários com role {} na página {} de {} (Total: {})",
                users.getNumberOfElements(), role,
                users.getNumber() + 1, users.getTotalPages(),
                users.getTotalElements());

        return users;
    }

    public User getUsersById(Long id) {
        logger.info("Buscando usuário por ID: {}", id);
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                logger.info("Usuário com ID {} encontrado.", id);
                return user.get();
            } else {
                logger.warn("Usuário com ID {} não encontrado.", id);
                throw new UserNotFoundException("Usuário nao encontrado com ID: " + id);
            }

    }

    public User getUsersByEmail(String email) {
        logger.info("Buscando usuário por email: {}", email);
            Optional<User> user = userRepository.findByEmail(email);
            if (user.isPresent()) {
                logger.info("Usuário com email {} encontrado.", email);
                return user.get();
            } else {
                logger.warn("Usuário com email {} não encontrado.", email);
                throw new UserNotFoundException("Usuário nao encontrado com email: " + email);
            }

    }

    @Transactional
    public User updateUser(Long userId, User updatedUser) {
        logger.info("Atualizando usuário com ID: {}", userId);
        try {
            User existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("Usuario nao encontrado com ID: " + userId));
            boolean changed = false;
            if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
                existingUser.setEmail(updatedUser.getEmail());
                changed = true;
            }
            if (!existingUser.getPassword().equals(updatedUser.getPassword())) {
                existingUser.setPassword(updatedUser.getPassword());
                changed = true;
            }
            if (!existingUser.getRole().equals(updatedUser.getRole())) {
                existingUser.setRole(updatedUser.getRole());
                changed = true;
            }
            if (changed) {
                User saved = userRepository.save(existingUser);
                logger.info("Usuário com ID {} atualizado com sucesso.", userId);
                return saved;
            } else {
                logger.info("Nenhuma alteração detectada para o usuário com ID {}.", userId);
                return existingUser;
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao atualizar usuário com ID: {}. Erro: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    public void deleteUser(Long id) {
        logger.info("Excluindo usuário com ID: {}", id);
        try {
            if (!userRepository.existsById(id)) {
                logger.warn("Usuário com ID {} nao encontrado.", id);
                throw new UserNotFoundException("Usuário nao encontrado com ID: " + id);
            }
            userRepository.deleteById(id);
            logger.info("Usuário com ID {} excluído com sucesso.", id);
        } catch (Exception e) {
            logger.error("Erro ao excluir usuário com ID: {}. Erro: {}", id, e.getMessage(), e);
            throw e;
        }
    }


}