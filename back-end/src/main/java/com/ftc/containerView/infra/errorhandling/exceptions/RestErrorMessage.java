package com.ftc.containerView.infra.errorhandling.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Classe de resposta de erro atualizada com campos para monitoramento.
 * Mantém compatibilidade com a estrutura existente mas adiciona campos necessários.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestErrorMessage {

    /**
     * Status HTTP (mantido para compatibilidade)
     */
    private HttpStatus status;

    /**
     * Mensagem de erro (campo original)
     */
    private String message;

    /**
     * Código padronizado do erro para facilitar tratamento no frontend.
     * Exemplos: "USER_NOT_FOUND", "VALIDATION_ERROR", "INTERNAL_ERROR"
     */
    private String code;

    /**
     * Timestamp de quando o erro ocorreu.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * ID único para rastreamento do erro nos logs.
     */
    private String errorId;

    /**
     * Path da requisição que causou o erro.
     */
    private String path;

    /**
     * Detalhes adicionais do erro (opcional).
     */
    private Object details;

    // Construtores de compatibilidade com código existente
    public RestErrorMessage(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Método estático para facilitar criação
    public static RestErrorMessage of(String code, String message, String path) {
        return RestErrorMessage.builder()
                .code(code)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}