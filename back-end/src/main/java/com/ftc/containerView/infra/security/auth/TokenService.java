package com.ftc.containerView.infra.security.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.ftc.containerView.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(User user) {
        logger.info("Gerando token JWT para usuário: {}", user.getCpf());
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("container-view")
                    .withSubject(user.getCpf())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
            logger.info("Token JWT gerado com sucesso para usuário: {}", user.getCpf());
            return token;
        } catch (JWTCreationException e) {
            logger.error("Erro ao gerar token JWT para usuário: {}. Erro: {}", user.getCpf(), e.getMessage(), e);
            throw new RuntimeException("Error while generating token", e);
        }
    }

    public String validateToken(String token) {
        logger.info("Validando token JWT recebido");
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String subject = JWT.require(algorithm)
                    .withIssuer("container-view")
                    .build()
                    .verify(token)
                    .getSubject();
            logger.info("Token JWT válido para subject: {}", subject);
            return subject;
        } catch (JWTVerificationException e) {
            logger.warn("Token JWT inválido: {}", e.getMessage());
            return "";
        }
    }

    private Instant genExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
