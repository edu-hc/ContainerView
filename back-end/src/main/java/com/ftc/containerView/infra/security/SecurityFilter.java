package com.ftc.containerView.infra.security;

import com.ftc.containerView.infra.security.auth.TokenService;
import com.ftc.containerView.model.user.User;
import com.ftc.containerView.model.user.UserRole;
import com.ftc.containerView.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.info("Interceptando requisição: {} {}", request.getMethod(), request.getRequestURI());
        var token = this.recoverToken(request);

        if (token != null && !token.isEmpty()) {
            try {
                logger.debug("Token recebido: {}", token);
                // Obter CPF do token
                var cpf = tokenService.validateToken(token);

                // Se o CPF for vazio, apenas continue o filtro
                if (cpf == null || cpf.isEmpty()) {
                    logger.warn("Token com CPF vazio ou inválido");
                    filterChain.doFilter(request, response);
                    return;
                }

                logger.debug("CPF extraído do token: {}", cpf);

                // Consultar o usuário pelo CPF
                Optional<User> userOptional = userRepository.findByCpf(cpf);

                // Se o usuário existir, autenticar
                if (userOptional.isPresent()) {
                    logger.info("Usuário autenticado via token: {}", cpf);
                    var user = userOptional.get();

                    var authorities = ROLE_PERMISSIONS.getOrDefault(user.getRole(), Collections.emptyList())
                            .stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    logger.debug(String.format("Usuário: %s, Role: %s, Authorities: %s",
                            user.getCpf(),
                            user.getRole(),
                            String.join(", ", authorities.stream().map(a -> a.getAuthority()).toList())));

                    var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    logger.debug("Usuário autenticado com sucesso: " + cpf);
                } else {
                    logger.warn("Usuário não encontrado para CPF extraído do token: {}", cpf);
                }
            } catch (Exception e) {
                logger.error("Erro ao processar token de autenticação: {}", e.getMessage(), e);
            }
        }

        filterChain.doFilter(request, response);
    }

    private static final Map<UserRole, List<String>> ROLE_PERMISSIONS = Map.of(
            UserRole.ADMIN, List.of("ROLE_ADMIN", "ROLE_GERENTE", "ROLE_INSPETOR"),
            UserRole.GERENTE, List.of("ROLE_GERENTE", "ROLE_INSPETOR"),
            UserRole.INSPETOR, List.of("ROLE_INSPETOR")
    );

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7); // "Bearer " tem 7 caracteres
    }
}
