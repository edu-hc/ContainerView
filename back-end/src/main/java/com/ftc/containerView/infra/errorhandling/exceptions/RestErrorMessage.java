package com.ftc.containerView.infra.errorhandling.exceptions;

import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * Resposta padronizada para erros da API.
 *
 * Exemplo de resposta JSON:
 * {
 *   "code": "USER_NOT_FOUND",
 *   "message": "Usuário não encontrado com ID: 123",
 *   "timestamp": "2025-06-03T14:30:00",
 *   "errorId": "550e8400-e29b-41d4-a716-446655440000",
 *   "path": "/users/123"
 * }
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Só inclui campos não-nulos no JSON
public class RestErrorMessage {

    /**
     * Código padronizado do erro para facilitar tratamento no frontend.
     * Exemplos: "USER_NOT_FOUND", "VALIDATION_ERROR", "INTERNAL_ERROR"
     */
    private String code;

    /**
     * Mensagem amigável para o usuário final.
     */
    private String message;

    /**
     * Timestamp de quando o erro ocorreu.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * ID único para rastreamento do erro nos logs.
     * Útil para correlacionar erro no frontend com logs do backend.
     */
    private String errorId;

    /**
     * Path da requisição que causou o erro.
     */
    private String path;

    /**
     * Detalhes adicionais do erro (opcional).
     * Usado para validation errors, por exemplo.
     */
    private Object details;

    // Construtores de conveniência
    public static RestErrorMessage of(String code, String message) {
        return RestErrorMessage.builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static RestErrorMessage of(String code, String message, String path) {
        return RestErrorMessage.builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }
}