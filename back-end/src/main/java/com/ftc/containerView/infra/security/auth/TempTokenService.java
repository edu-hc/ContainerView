package com.ftc.containerView.infra.security.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.ftc.containerView.model.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TempTokenService {

    @Value("${jwt.temp.secret}")
    private String tempSecret;

    public String generateTempToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(tempSecret);
            String token = JWT.create()
                    .withIssuer("container-view-2fa")
                    .withSubject(user.getCpf())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException e) {
            throw new RuntimeException("Erro ao gerar token temporário", e);
        }
    }

    public String validateTempToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(tempSecret);
            return JWT.require(algorithm)
                    .withIssuer("container-view-2fa")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            return "";
        }
    }

    private Instant genExpirationDate() {
        // Token temporário expira em 10 minutos
        return LocalDateTime.now().plusMinutes(10).toInstant(ZoneOffset.of("-03:00"));
    }
}