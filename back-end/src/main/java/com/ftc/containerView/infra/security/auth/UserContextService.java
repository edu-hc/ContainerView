package com.ftc.containerView.infra.security.auth;

import com.ftc.containerView.infra.errorhandling.exceptions.UserNotFoundException;
import com.ftc.containerView.model.user.User;
import com.ftc.containerView.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserContextService {

    private final UserRepository userRepository;

    /**
     * ✅ RESPONSABILIDADE ESPECÍFICA: Recuperar dados do usuário JÁ AUTENTICADO.
     * Usado durante o processamento de requisições para obter contexto.
     */
    public User getCurrentUser() {
        String cpf = getCurrentUserCpf();
        if (cpf == null) {
            return null;
        }

        try {
            return userRepository.findByCpf(cpf)
                    .orElseThrow(() -> new UserNotFoundException("Usuário autenticado não encontrado: " + cpf));
        } catch (Exception e) {
            log.error("Erro ao buscar usuário do contexto - CPF: {}", cpf, e);
            return null;
        }
    }

    /**
     * ✅ Recupera CPF do SecurityContext (dados da sessão autenticada)
     */
    private String getCurrentUserCpf() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() &&
                    !authentication.getName().equals("anonymousUser")) {
                return authentication.getName();
            }
            return null;
        } catch (Exception e) {
            log.warn("Erro ao recuperar CPF do contexto de segurança: {}", e.getMessage());
            return null;
        }
    }

    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public boolean isUserAuthenticated() {
        return getCurrentUserCpf() != null;
    }
}
