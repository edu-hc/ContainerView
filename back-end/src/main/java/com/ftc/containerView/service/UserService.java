package com.ftc.containerView.service;

import com.ftc.containerView.model.user.User;
import com.ftc.containerView.repositories.UserRepository;
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

    public Optional<User> getUsersByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User updateUser(Long userId, User updatedUser) {
        // Busca o usuário existente
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Atualiza os campos se forem diferentes
        if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
            existingUser.setEmail(updatedUser.getEmail());
        }

        if (!existingUser.getPassword().equals(updatedUser.getPassword())) {
            existingUser.setPassword(updatedUser.getPassword());
        }
        if (!existingUser.getRole().equals(updatedUser.getRole())) {
            existingUser.setRole(updatedUser.getRole());
        }

        // Salva o usuário atualizado
        return userRepository.save(existingUser);

    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}