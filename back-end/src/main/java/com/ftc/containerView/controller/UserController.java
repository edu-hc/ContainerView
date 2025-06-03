package com.ftc.containerView.controller;

import com.ftc.containerView.model.user.UserDTO;
import com.ftc.containerView.model.user.User;
import com.ftc.containerView.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("GET /users - Buscando todos os usuários. IP: {}", request.getRemoteAddr());
        List<User> users = userService.getUsers();
        long execTime = System.currentTimeMillis() - startTime;
        logger.info("GET /users concluído. Encontrados {} usuários. Tempo de resposta: {}ms", users.size(), execTime);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("GET /users/{} - Buscando usuário por ID. IP: {}", id, request.getRemoteAddr());
        User user = userService.getUsersById(id);
        long execTime = System.currentTimeMillis() - startTime;
        logger.info("Usuário com ID {} encontrado. Tempo de resposta: {}ms", id, execTime);
        return ResponseEntity.ok(user);
    }


    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("PUT /users/{} - Atualizando usuário. IP: {}", id, request.getRemoteAddr());
        User user = userService.updateUser(id, updatedUser);
        long execTime = System.currentTimeMillis() - startTime;
        logger.info("Usuário com ID {} atualizado com sucesso. Tempo de resposta: {}ms", id, execTime);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("DELETE /users/{} - Excluindo usuário. IP: {}", id, request.getRemoteAddr());
        userService.deleteUser(id);
        long execTime = System.currentTimeMillis() - startTime;
        logger.info("Usuário com ID {} excluído com sucesso. Tempo de resposta: {}ms", id, execTime);
        return ResponseEntity.noContent().build();
    }
}
