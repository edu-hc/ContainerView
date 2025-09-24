package com.ftc.containerView.controller;

import com.ftc.containerView.model.user.UserDTO;
import com.ftc.containerView.model.user.User;
import com.ftc.containerView.model.user.UserRole;
import com.ftc.containerView.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            HttpServletRequest request) {

        long startTime = System.currentTimeMillis();
        logger.info("GET /users - Página: {}, Tamanho: {}, Ordenação: {} {}. IP: {}",
                page, size, sortBy, sortDirection, request.getRemoteAddr());

        // Validar tamanho máximo da página
        if (size > 100) {
            size = 100;
            logger.warn("Tamanho da página limitado a 100 itens");
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> users = userService.getUsers(pageable);

        long execTime = System.currentTimeMillis() - startTime;
        logger.info("GET /users concluído. Página {} de {} ({} usuários de {} total). Tempo: {}ms",
                users.getNumber() + 1, users.getTotalPages(),
                users.getNumberOfElements(), users.getTotalElements(), execTime);

        return ResponseEntity.ok(users);
    }

    @GetMapping("/by-role/{role}")
    public ResponseEntity<Page<User>> getUsersByRole(
            @PathVariable UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            HttpServletRequest request) {

        long startTime = System.currentTimeMillis();
        logger.info("GET /users/by-role/{} - Página: {}, Tamanho: {}. IP: {}",
                role, page, size, request.getRemoteAddr());

        if (size > 100) {
            size = 100;
            logger.warn("Tamanho da página limitado a 100 itens");
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> users = userService.getUsersByRole(role, pageable);

        long execTime = System.currentTimeMillis() - startTime;
        logger.info("GET /users/by-role/{} concluído. Página {} de {} ({} usuários). Tempo: {}ms",
                role, users.getNumber() + 1, users.getTotalPages(),
                users.getNumberOfElements(), execTime);

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
