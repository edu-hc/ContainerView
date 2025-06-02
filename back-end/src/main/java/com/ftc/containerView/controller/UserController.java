package com.ftc.containerView.controller;

import com.ftc.containerView.model.user.UserDTO;
import com.ftc.containerView.model.user.User;
import com.ftc.containerView.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("GET /users - Buscando todos os usuários.");
        try {
            List<User> users = userService.getUsers();
            logger.info("GET /users concluído. Encontrados {} usuários.", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Erro ao buscar usuários. Erro: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        logger.info("GET /users/{} - Buscando usuário por ID.", id);
        try {
            Optional<User> user = userService.getUsersById(id);
            if (user.isPresent()) {
                logger.info("Usuário com ID {} encontrado.", id);
                return ResponseEntity.ok(user.get());
            } else {
                logger.warn("Usuário com ID {} não encontrado.", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erro ao buscar usuário com ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        logger.info("PUT /users/{} - Atualizando usuário.", id);
        try {
            User user = userService.updateUser(id, updatedUser);
            logger.info("Usuário com ID {} atualizado com sucesso.", id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Erro ao atualizar usuário com ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("DELETE /users/{} - Excluindo usuário.", id);
        try {
            userService.deleteUser(id);
            logger.info("Usuário com ID {} excluído com sucesso.", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Erro ao excluir usuário com ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
