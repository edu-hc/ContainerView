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
public class TempTokenService {

    private static final Logger logger = LoggerFactory.getLogger(TempTokenService.class);

    @Value("${jwt.temp.secret}")
    private String tempSecret;

    public String generateTempToken(User user) {
        logger.info("Gerando token temporário para usuário: {}", user.getCpf());
        try {
            String token = generateTempTokenInternal(user);
            logger.info("Token temporário gerado com sucesso para usuário: {}", user.getCpf());
            return token;
        } catch (Exception e) {
            logger.error("Erro ao gerar token temporário para usuário: {}. Erro: {}", user.getCpf(), e.getMessage(), e);
            throw e;
        }
    }

    private String generateTempTokenInternal(User user) {
        Algorithm algorithm = Algorithm.HMAC256(tempSecret);
        return JWT.create()
                .withIssuer("container-view-2fa")
                .withSubject(user.getCpf())
                .withExpiresAt(genExpirationDate())
                .sign(algorithm);
    }

    public String validateTempToken(String tempToken) {
        logger.info("Validando token temporário recebido");
        try {
            String cpf = validateTempTokenInternal(tempToken);
            logger.info("Token temporário válido para CPF: {}", cpf);
            return cpf;
        } catch (Exception e) {
            logger.warn("Token temporário inválido: {}", e.getMessage());
            return "";
        }
    }

    private String validateTempTokenInternal(String tempToken) {
        Algorithm algorithm = Algorithm.HMAC256(tempSecret);
        return JWT.require(algorithm)
                .withIssuer("container-view-2fa")
                .build()
                .verify(tempToken)
                .getSubject();
    }

    private Instant genExpirationDate() {
        // Token temporário expira em 10 minutos
        return LocalDateTime.now().plusMinutes(10).toInstant(ZoneOffset.of("-03:00"));
    }
}