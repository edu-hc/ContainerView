package com.ftc.containerView.service;

import com.ftc.containerView.model.Container;
import com.ftc.containerView.model.User;
import com.ftc.containerView.repository.ContainerRepository;
import com.ftc.containerView.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersById() {
        return userRepository.findById(id);
    }

    public List<User> getUsersByUsername() {
        return userRepository.findByUsername(username);
    }

}