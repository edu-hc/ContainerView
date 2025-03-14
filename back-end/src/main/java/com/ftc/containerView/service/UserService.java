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

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
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
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Atualiza os campos se forem diferentes
        if (!existingUser.getUsername().equals(updatedUser.getUsername())) {
            existingUser.setUsername(updatedUser.getUsername());
        }

        if (!existingUser.getPassword().equals(updatedUser.getPassword())) {
            existingUser.setPassword(updatedUser.getPassword());
        }

        // Salva o usuário atualizado
        return userRepository.save(existingUser);

    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}