package com.ftc.containerView.infra.errorhandling;

import com.ftc.containerView.infra.errorhandling.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Exception Handler LIVRE DE CONFLITOS.
 *
 * NÃO estende ResponseEntityExceptionHandler para evitar qualquer conflito.
 * Trata todas as exceções com @ExceptionHandler.
 *
 * ✅ RESOLVE TODOS OS PROBLEMAS:
 * - Zero conflitos de handlers
 * - Não vaza informações sensíveis
 * - Logs estruturados
 * - IDs únicos para rastreamento
 * - Respostas padronizadas
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ================================================================================================
    // EXCEÇÕES CUSTOMIZADAS DE NEGÓCIO
    // ================================================================================================

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<RestErrorMessage> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        String errorId = generateErrorId();

        log.warn("Usuário não encontrado - ID: {} - Path: {} - IP: {} - Detalhes: {}",
                errorId, request.getRequestURI(), getClientIP(request), ex.getMessage());

        RestErrorMessage error = RestErrorMessage.builder()
                .code("USER_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errorId(errorId)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(OperationNotFoundException.class)
    public ResponseEntity<RestErrorMessage> handleOperationNotFound(OperationNotFoundException ex, HttpServletRequest request) {
        String errorId = generateErrorId();

        log.warn("Operação não encontrada - ID: {} - Path: {} - IP: {} - Detalhes: {}",
                errorId, request.getRequestURI(), getClientIP(request), ex.getMessage());

        RestErrorMessage error = RestErrorMessage.builder()
                .code("OPERATION_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errorId(errorId)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ContainerNotFoundException.class)
    public ResponseEntity<RestErrorMessage> handleContainerNotFound(ContainerNotFoundException ex, HttpServletRequest request) {
        String errorId = generateErrorId();

        log.warn("Container não encontrado - ID: {} - Path: {} - IP: {} - Detalhes: {}",
                errorId, request.getRequestURI(), getClientIP(request), ex.getMessage());

        RestErrorMessage error = RestErrorMessage.builder()
                .code("CONTAINER_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errorId(errorId)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ContainerExistsException.class)
    public ResponseEntity<RestErrorMessage> handleContainerExists(ContainerExistsException ex, HttpServletRequest request) {
        String errorId = generateErrorId();

        log.warn("Container já existe - ID: {} - Path: {} - IP: {} - Detalhes: {}",
                errorId, request.getRequestURI(), getClientIP(request), ex.getMessage());

        RestErrorMessage error = RestErrorMessage.builder()
                .code("CONTAINER_ALREADY_EXISTS")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errorId(errorId)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ImageStorageException.class)
    public ResponseEntity<RestErrorMessage> handleImageStorage(ImageStorageException ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        String currentUser = getCurrentUsername();

        log.error("Erro no armazenamento de imagem - ID: {} - Path: {} - IP: {} - User: {} - Erro: {}",
                errorId, request.getRequestURI(), getClientIP(request), currentUser, ex.getMessage(), ex);

        RestErrorMessage error = RestErrorMessage.builder()
                .code("IMAGE_STORAGE_ERROR")
                .message("Erro ao processar imagem. Tente novamente.")
                .timestamp(LocalDateTime.now())
                .errorId(errorId)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // ================================================================================================
    // EXCEÇÕES DE SEGURANÇA
    // ================================================================================================

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<RestErrorMessage> handleUsernameNotFound(UsernameNotFoundException ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        String clientIP = getClientIP(request);

        log.warn("Tentativa de login falhada - ID: {} - IP: {} - Path: {}",
                errorId, clientIP, request.getRequestURI());

        RestErrorMessage error = RestErrorMessage.builder()
                .code("AUTHENTICATION_FAILED")
                .message("Credenciais inválidas") // Mensagem genérica por segurança
                .timestamp(LocalDateTime.now())
                .errorId(errorId)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<RestErrorMessage> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        String errorId = generateErrorId();

        log.warn("Falha na autenticação - ID: {} - Path: {} - IP: {} - Tipo: {}",
                errorId, request.getRequestURI(), getClientIP(request), ex.getClass().getSimpleName());

        RestErrorMessage error = RestErrorMessage.builder()
                .code("AUTHENTICATION_FAILED")
                .message("Credenciais inválidas")
                .timestamp(LocalDateTime.now())
                .errorId(errorId)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RestErrorMessage> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String errorId = generateErrorId();

        log.warn("Acesso negado - ID: {} - Path: {} - IP: {} - User: {}",
                errorId, request.getRequestURI(), getClientIP(request), getCurrentUsername());

        RestErrorMessage error = RestErrorMessage.builder()
                .code("ACCESS_DENIED")
                .message("Acesso negado - permissões insuficientes")
                .timestamp(LocalDateTime.now())
                .errorId(errorId)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // ================================================================================================
    // EXCEÇÕES DE VALIDAÇÃO E PARÂMETROS
    // ================================================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestErrorMessage> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorId = generateErrorId();

        String violations = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);

        log.warn("Erro de validação - ID: {} - Path: {} - IP: {} - Violações: {}",
                errorId, request.getRequestURI(), getClientIP(request), violations);

        RestErrorMessage error = RestErrorMessage.builder()
                .code("VALIDATION_ERROR")
                .message("Dados inválidos: " + violations)
                .timestamp(LocalDateTime.now())
                .errorId(errorId)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<RestErrorMessage> handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String errorId = generateErrorId();

        log.warn("Parâmetro obrigatório ausente - ID: {} - Path: {} - IP: {} - Parâmetro: {}",
                errorId, request.getRequestURI(), getClientIP(request), ex.getParameterName());

        RestErrorMessage error = RestErrorMessage.builder()
                .code("MISSING_PARAMETER")
                .message("Parâmetro obrigatório ausente: " + ex.getParameterName())
                .timestamp(LocalDateTime.now())
                .errorId(errorId)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RestErrorMessage> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String errorId = generateErrorId();

        log.warn("Tipo de parâmetro inválido - ID: {} - Path: {} - IP: {} - Parâmetro: {} - Valor: {}",
                errorId, request.getRequestURI(), getClientIP(request), ex.getName(), ex.getValue());

        RestErrorMessage error = RestErrorMessage.builder()
                .code("INVALID_PARAMETER_TYPE")
                .message("Parâmetro inválido: " + ex.getName())
                .timestamp(LocalDateTime.now())
                .errorId(errorId)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<RestErrorMessage> handleMaxUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        String errorId = generateErrorId();

        log.warn("Upload muito grande - ID: {} - Path: {} - IP: {} - Tamanho máximo: {}",
                errorId, request.getRequestURI(), getClientIP(request), ex.getMaxUploadSize());

        RestErrorMessage error = RestErrorMessage.builder()
                .code("FILE_TOO_LARGE")
                .message("Arquivo muito grande. Tamanho máximo permitido: " + formatBytes(ex.getMaxUploadSize()))
                .timestamp(LocalDateTime.now())
                .errorId(errorId)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ================================================================================================
    // HANDLER GENÉRICO - ÚLTIMO RECURSO (CRÍTICO PARA SEGURANÇA)
    // ================================================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestErrorMessage> handleGeneral(Exception ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        String currentUser = getCurrentUsername();
        String clientIP = getClientIP(request);

        // ✅ LOG COMPLETO para debugging (só no servidor)
        log.error("ERRO INTERNO NÃO TRATADO - ID: {} - Path: {} - IP: {} - User: {} - Classe: {} - Mensagem: {}",
                errorId, request.getRequestURI(), clientIP, currentUser,
                ex.getClass().getSimpleName(), ex.getMessage(), ex);

        // ✅ RESPOSTA SEGURA (sem vazamento de informações)
        RestErrorMessage error = RestErrorMessage.builder()
                .code("INTERNAL_ERROR")
                .message("Erro interno do servidor. Entre em contato com o suporte informando o ID: " + errorId)
                .timestamp(LocalDateTime.now())
                .errorId(errorId)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // ================================================================================================
    // MÉTODOS UTILITÁRIOS
    // ================================================================================================

    private String generateErrorId() {
        return UUID.randomUUID().toString();
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        return request.getRemoteAddr();
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "anonymous";
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return (bytes / (1024 * 1024)) + " MB";
    }
}