package com.ftc.containerView.infra.security.auth;

import com.ftc.containerView.model.user.User;
import com.ftc.containerView.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String cpf) throws UsernameNotFoundException {
        logger.info("Tentando carregar usuário pelo CPF: {}", cpf);
        try {
            User user = userRepository.findByCpf(cpf)
                    .orElseThrow(() -> {
                        logger.warn("Usuário não encontrado para CPF: {}", cpf);
                        return new UsernameNotFoundException("User not found: " + cpf);
                    });
            logger.info("Usuário carregado com sucesso para CPF: {}", cpf);
            return new org.springframework.security.core.userdetails.User(user.getCpf(), user.getPassword(), new ArrayList<>());
        } catch (Exception e) {
            logger.error("Erro ao carregar usuário para CPF: {}. Erro: {}", cpf, e.getMessage(), e);
            throw e;
        }
    }
}
