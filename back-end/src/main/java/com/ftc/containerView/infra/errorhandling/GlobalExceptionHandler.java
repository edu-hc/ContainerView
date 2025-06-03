package com.ftc.containerView.infra.errorhandling;

import com.ftc.containerView.infra.errorhandling.exceptions.*;
import com.ftc.containerView.infra.monitoring.ErrorAlertService;
import com.ftc.containerView.infra.monitoring.ErrorMetricsCollector;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
 * Exception Handler com Sistema de Monitoramento Integrado.
 *
 * Baseado no seu código existente, mas com adição de:
 * - 📊 Métricas automáticas (ErrorMetricsCollector)
 * - 🚨 Alertas inteligentes (ErrorAlertService)
 * - 📈 Observabilidade completa
 *
 * Mantém compatibilidade com RestErrorMessage existente.
 */
@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorMetricsCollector metricsCollector;
    private final ErrorAlertService alertService;

    // ================================================================================================
    // EXCEÇÕES CUSTOMIZADAS DE NEGÓCIO (4xx) - MÉTRICAS SEM ALERTAS
    // ================================================================================================

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<RestErrorMessage> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        Timer.Sample sample = metricsCollector.startTimer();
        String errorId = generateErrorId();

        log.warn("Usuário não encontrado - ID: {} - Path: {} - IP: {} - Detalhes: {}",
                errorId, request.getRequestURI(), getClientIP(request), ex.getMessage());

        // 📊 MÉTRICAS (erro de negócio, sem alertas)
        metricsCollector.recordError("USER_NOT_FOUND", "404", request.getRequestURI());
        metricsCollector.recordDuration(sample, "user_not_found");

        RestErrorMessage error = RestErrorMessage.builder()
                .status(HttpStatus.NOT_FOUND)
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
        Timer.Sample sample = metricsCollector.startTimer();
        String errorId = generateErrorId();

        log.warn("Operação não encontrada - ID: {} - Path: {} - IP: {} - Detalhes: {}",
                errorId, request.getRequestURI(), getClientIP(request), ex.getMessage());

        // 📊 MÉTRICAS
        metricsCollector.recordError("OPERATION_NOT_FOUND", "404", request.getRequestURI());
        metricsCollector.recordDuration(sample, "operation_not_found");

        RestErrorMessage error = RestErrorMessage.builder()
                .status(HttpStatus.NOT_FOUND)
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
        Timer.Sample sample = metricsCollector.startTimer();
        String errorId = generateErrorId();

        log.warn("Container não encontrado - ID: {} - Path: {} - IP: {} - Detalhes: {}",
                errorId, request.getRequestURI(), getClientIP(request), ex.getMessage());

        // 📊 MÉTRICAS
        metricsCollector.recordError("CONTAINER_NOT_FOUND", "404", request.getRequestURI());
        metricsCollector.recordDuration(sample, "container_not_found");

        RestErrorMessage error = RestErrorMessage.builder()
                .status(HttpStatus.NOT_FOUND)
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
        Timer.Sample sample = metricsCollector.startTimer();
        String errorId = generateErrorId();

        log.warn("Container já existe - ID: {} - Path: {} - IP: {} - Detalhes: {}",
                errorId, request.getRequestURI(), getClientIP(request), ex.getMessage());

        // 📊 MÉTRICAS
        metricsCollector.recordError("CONTAINER_ALREADY_EXISTS", "409", request.getRequestURI());
        metricsCollector.recordDuration(sample, "container_exists");

        RestErrorMessage error = RestErrorMessage.builder()
                .status(HttpStatus.CONFLICT)
                .code("CONTAINER_ALREADY_EXISTS")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .errorId(errorId)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // ================================================================================================
    // EXCEÇÕES DE INFRAESTRUTURA (5xx) - MÉTRICAS + ALERTAS CRÍTICOS
    // ================================================================================================

    @ExceptionHandler(ImageStorageException.class)
    public ResponseEntity<RestErrorMessage> handleImageStorage(ImageStorageException ex, HttpServletRequest request) {
        Timer.Sample sample = metricsCollector.startTimer();
        String errorId = generateErrorId();
        String currentUser = getCurrentUsername();

        log.error("Erro no armazenamento de imagem - ID: {} - Path: {} - IP: {} - User: {} - Erro: {}",
                errorId, request.getRequestURI(), getClientIP(request), currentUser, ex.getMessage(), ex);

        // 📊 MÉTRICAS + 🚨 ALERTAS (infraestrutura é crítico)
        metricsCollector.recordError("IMAGE_STORAGE_ERROR", "500", request.getRequestURI());
        metricsCollector.recordDuration(sample, "image_storage_error");

        // Alerta para erros de infraestrutura
        alertService.processError("IMAGE_STORAGE_ERROR", errorId, request.getRequestURI(),
                ex.getMessage(), currentUser);

        RestErrorMessage error = RestErrorMessage.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .code("IMAGE_STORAGE_ERROR")
                .message("Erro ao processar imagem. Tente novamente.")
                .timestamp(LocalDateTime.now())
                .errorId(errorId)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // ================================================================================================
    // EXCEÇÕES DE SEGURANÇA (401/403) - MÉTRICAS + ALERTAS POR THRESHOLD
    // ================================================================================================

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<RestErrorMessage> handleUsernameNotFound(UsernameNotFoundException ex, HttpServletRequest request) {
        Timer.Sample sample = metricsCollector.startTimer();
        String errorId = generateErrorId();
        String clientIP = getClientIP(request);

        log.warn("Tentativa de login falhada - ID: {} - IP: {} - Path: {}",
                errorId, clientIP, request.getRequestURI());

        // 📊 MÉTRICAS + evento de segurança
        metricsCollector.recordError("AUTHENTICATION_FAILED", "401", request.getRequestURI());
        metricsCollector.recordSecurityEvent("failed_login", clientIP, request.getRequestURI());
        metricsCollector.recordDuration(sample, "authentication_failed");

        // 🚨 ALERTAS apenas se muitas tentativas (threshold)
        alertService.processError("AUTHENTICATION_FAILED", errorId, request.getRequestURI(),
                "Falha na autenticação", clientIP);

        RestErrorMessage error = RestErrorMessage.builder()
                .status(HttpStatus.UNAUTHORIZED)
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
        Timer.Sample sample = metricsCollector.startTimer();
        String errorId = generateErrorId();
        String currentUser = getCurrentUsername();

        log.warn("Falha na autenticação - ID: {} - Path: {} - IP: {} - Tipo: {}",
                errorId, request.getRequestURI(), getClientIP(request), ex.getClass().getSimpleName());

        // 📊 MÉTRICAS + evento de segurança
        metricsCollector.recordError("AUTHENTICATION_FAILED", "401", request.getRequestURI());
        metricsCollector.recordSecurityEvent("authentication_failed", currentUser, request.getRequestURI());
        metricsCollector.recordDuration(sample, "authentication_failed");

        // 🚨 ALERTAS por threshold
        alertService.processError("AUTHENTICATION_FAILED", errorId, request.getRequestURI(),
                ex.getMessage(), currentUser);

        RestErrorMessage error = RestErrorMessage.builder()
                .status(HttpStatus.UNAUTHORIZED)
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
        Timer.Sample sample = metricsCollector.startTimer();
        String errorId = generateErrorId();
        String currentUser = getCurrentUsername();

        log.warn("Acesso negado - ID: {} - Path: {} - IP: {} - User: {}",
                errorId, request.getRequestURI(), getClientIP(request), currentUser);

        // 📊 MÉTRICAS + evento de segurança
        metricsCollector.recordError("ACCESS_DENIED", "403", request.getRequestURI());
        metricsCollector.recordSecurityEvent("access_denied", currentUser, request.getRequestURI());
        metricsCollector.recordDuration(sample, "access_denied");

        // 🚨 ALERTAS por threshold
        alertService.processError("ACCESS_DENIED", errorId, request.getRequestURI(),
                ex.getMessage(), currentUser);

        RestErrorMessage error = RestErrorMessage.builder()
                .status(HttpStatus.FORBIDDEN)
                .code("ACCESS_DENIED")
                .message("Acesso negado - permissões insuficientes")
                .timestamp(LocalDateTime.now())
                .errorId(errorId)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // ================================================================================================
    // EXCEÇÕES DE VALIDAÇÃO E PARÂMETROS (400) - MÉTRICAS SEM ALERTAS
    // ================================================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestErrorMessage> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Timer.Sample sample = metricsCollector.startTimer();
        String errorId = generateErrorId();

        String violations = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);

        log.warn("Erro de validação - ID: {} - Path: {} - IP: {} - Violações: {}",
                errorId, request.getRequestURI(), getClientIP(request), violations);

        // 📊 MÉTRICAS (validação não gera alertas)
        metricsCollector.recordError("VALIDATION_ERROR", "400", request.getRequestURI());
        metricsCollector.recordDuration(sample, "validation_error");

        RestErrorMessage error = RestErrorMessage.builder()
                .status(HttpStatus.BAD_REQUEST)
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
        Timer.Sample sample = metricsCollector.startTimer();
        String errorId = generateErrorId();

        log.warn("Parâmetro obrigatório ausente - ID: {} - Path: {} - IP: {} - Parâmetro: {}",
                errorId, request.getRequestURI(), getClientIP(request), ex.getParameterName());

        // 📊 MÉTRICAS
        metricsCollector.recordError("MISSING_PARAMETER", "400", request.getRequestURI());
        metricsCollector.recordDuration(sample, "missing_parameter");

        RestErrorMessage error = RestErrorMessage.builder()
                .status(HttpStatus.BAD_REQUEST)
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
        Timer.Sample sample = metricsCollector.startTimer();
        String errorId = generateErrorId();

        log.warn("Tipo de parâmetro inválido - ID: {} - Path: {} - IP: {} - Parâmetro: {} - Valor: {}",
                errorId, request.getRequestURI(), getClientIP(request), ex.getName(), ex.getValue());

        // 📊 MÉTRICAS
        metricsCollector.recordError("INVALID_PARAMETER_TYPE", "400", request.getRequestURI());
        metricsCollector.recordDuration(sample, "invalid_parameter_type");

        RestErrorMessage error = RestErrorMessage.builder()
                .status(HttpStatus.BAD_REQUEST)
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
        Timer.Sample sample = metricsCollector.startTimer();
        String errorId = generateErrorId();

        log.warn("Upload muito grande - ID: {} - Path: {} - IP: {} - Tamanho máximo: {}",
                errorId, request.getRequestURI(), getClientIP(request), ex.getMaxUploadSize());

        // 📊 MÉTRICAS
        metricsCollector.recordError("FILE_TOO_LARGE", "400", request.getRequestURI());
        metricsCollector.recordDuration(sample, "file_too_large");

        RestErrorMessage error = RestErrorMessage.builder()
                .status(HttpStatus.BAD_REQUEST)
                .code("FILE_TOO_LARGE")
                .message("Arquivo muito grande. Tamanho máximo permitido: " + formatBytes(ex.getMaxUploadSize()))
                .timestamp(LocalDateTime.now())
                .errorId(errorId)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ================================================================================================
    // HANDLER GENÉRICO - ÚLTIMO RECURSO (CRÍTICO) - MÉTRICAS + ALERTA IMEDIATO
    // ================================================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestErrorMessage> handleGeneral(Exception ex, HttpServletRequest request) {
        Timer.Sample sample = metricsCollector.startTimer();
        String errorId = generateErrorId();
        String currentUser = getCurrentUsername();
        String clientIP = getClientIP(request);

        // ✅ LOG COMPLETO para debugging (só no servidor)
        log.error("ERRO INTERNO NÃO TRATADO - ID: {} - Path: {} - IP: {} - User: {} - Classe: {} - Mensagem: {}",
                errorId, request.getRequestURI(), clientIP, currentUser,
                ex.getClass().getSimpleName(), ex.getMessage(), ex);

        // 📊 MÉTRICAS CRÍTICAS
        metricsCollector.recordError("INTERNAL_ERROR", "500", request.getRequestURI());
        metricsCollector.recordCriticalError("INTERNAL_ERROR", "unhandled_exception");
        metricsCollector.recordUserError(getUserRole(currentUser), "INTERNAL_ERROR");
        metricsCollector.recordDuration(sample, "internal_error");

        // 🚨 ALERTA CRÍTICO IMEDIATO
        alertService.processError("INTERNAL_ERROR", errorId, request.getRequestURI(),
                ex.getMessage(), currentUser);

        // ✅ RESPOSTA SEGURA (sem vazamento de informações)
        RestErrorMessage error = RestErrorMessage.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
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

    private String getUserRole(String username) {
        try {
            return SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().iterator().next().getAuthority();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return (bytes / (1024 * 1024)) + " MB";
    }
}