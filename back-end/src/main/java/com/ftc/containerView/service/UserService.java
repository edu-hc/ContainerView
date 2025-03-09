package com.ftc.containerView.service;

import com.ftc.containerView.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}