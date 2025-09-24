package com.ftc.containerView;

import com.ftc.containerView.model.user.User;
import com.ftc.containerView.model.user.UserRole;
import com.ftc.containerView.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitComponent implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        boolean admins = userRepository.existsByRole(UserRole.ADMIN);

        if (!admins) {
            log.info("Nenhum usuário encontrado no banco. Criando usuário admin padrão...");

            User admin = new User(
                    "Admin",
                    "Sistema",
                    "000.000.000-00",
                    "admin@containerview.com",
                    passwordEncoder.encode("admin123"),
                    UserRole.ADMIN,
                    false
            );

            userRepository.save(admin);

            log.info("Usuário admin criado com sucesso!");
            log.info("Email: admin@containerview.com");
            log.info("Senha: admin123");
            log.warn("ALTERE A SENHA APÓS O PRIMEIRO LOGIN!");
        }
    }
}