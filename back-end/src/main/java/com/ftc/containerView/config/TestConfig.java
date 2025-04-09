package com.ftc.containerView.config;

import com.ftc.containerView.model.Container;
import com.ftc.containerView.model.Operation;
import com.ftc.containerView.model.User;
import com.ftc.containerView.repositories.ContainerRepository;
import com.ftc.containerView.repositories.OperationRepository;
import com.ftc.containerView.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Profile("test")
public class TestConfig implements CommandLineRunner {

    @Autowired
    private ContainerRepository containerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OperationRepository operationRepository;

    @Override
    public void run(String... args) throws Exception {
        // Cria usuários (agora com senhas em texto plano)
        User admin = new User("admin", "admin123", "ADMIN");
        User user1 = new User("user1", "user123", "USER");
        User user2 = new User("user2", "user123", "USER");

        userRepository.saveAll(Arrays.asList(admin, user1, user2));

        // Cria containers
        Container container1 = new Container("CONT001", "Container de produtos eletrônicos", "https://storage.com/container1.jpg");
        Container container2 = new Container("CONT002", "Container de roupas", "https://storage.com/container2.jpg");
        Container container3 = new Container("CONT003", "Container de alimentos", "https://storage.com/container3.jpg");

        containerRepository.saveAll(Arrays.asList(container1, container2, container3));

        // Cria operações
        Operation op1 = new Operation(null, container1, admin);
        Operation op2 = new Operation(null, container2, user1);
        Operation op3 = new Operation(null, container3, user2);

        operationRepository.saveAll(Arrays.asList(op1, op2, op3));
    }
}

