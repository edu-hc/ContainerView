package com.ftc.containerView.infra.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Coletor de métricas de erros da API.
 *
 * Este componente registra métricas no Micrometer, que são então
 * expostas via /actuator/prometheus para Grafana ou outras ferramentas.
 *
 * Métricas coletadas:
 * - api_errors_total: Contador de erros por tipo
 * - api_error_duration_seconds: Tempo para processar erros
 * - api_user_errors_total: Erros por tipo de usuário
 * - api_endpoint_errors_total: Erros por endpoint
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ErrorMetricsCollector {

    private final MeterRegistry meterRegistry;

    // Cache de contadores para evitar criar novos a cada chamada
    private final ConcurrentMap<String, Counter> errorCounters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> errorTimers = new ConcurrentHashMap<>();

    /**
     * Registra um erro geral da API.
     *
     * @param errorCode Código do erro (ex: "USER_NOT_FOUND", "VALIDATION_ERROR")
     * @param httpStatus Status HTTP como string (ex: "404", "500")
     * @param endpoint URI do endpoint (ex: "/users/123")
     */
    public void recordError(String errorCode, String httpStatus, String endpoint) {
        String key = errorCode + "_" + httpStatus + "_" + cleanEndpoint(endpoint);

        Counter counter = errorCounters.computeIfAbsent(key, k ->
                Counter.builder("api_errors_total")
                        .description("Total de erros da API por tipo")
                        .tag("error_code", errorCode)
                        .tag("http_status", httpStatus)
                        .tag("endpoint", cleanEndpoint(endpoint))
                        .tag("severity", getSeverity(httpStatus))
                        .register(meterRegistry)
        );

        counter.increment();

        log.debug("Métrica de erro registrada - Code: {}, Status: {}, Endpoint: {}",
                errorCode, httpStatus, cleanEndpoint(endpoint));
    }

    /**
     * Registra erro específico por endpoint.
     * Útil para identificar endpoints problemáticos.
     */
    public void recordEndpointError(String endpoint, String httpStatus) {
        String cleanEndpoint = cleanEndpoint(endpoint);

        Counter.builder("api_endpoint_errors_total")
                .description("Erros por endpoint")
                .tag("endpoint", cleanEndpoint)
                .tag("http_status", httpStatus)
                .tag("severity", getSeverity(httpStatus))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Registra erro por tipo de usuário.
     * Útil para detectar abuse ou problemas específicos de roles.
     */
    public void recordUserError(String userRole, String errorCode) {
        if (userRole == null) userRole = "anonymous";

        Counter.builder("api_user_errors_total")
                .description("Erros por tipo de usuário")
                .tag("user_role", userRole.toLowerCase())
                .tag("error_code", errorCode)
                .register(meterRegistry)
                .increment();

        log.debug("Métrica de usuário registrada - Role: {}, Error: {}", userRole, errorCode);
    }

    /**
     * Inicia timer para medir tempo de processamento de erro.
     *
     * Uso:
     * Timer.Sample sample = metricsCollector.startTimer();
     * // ... processar erro ...
     * metricsCollector.recordDuration(sample, "user_not_found");
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Para timer e registra duração.
     *
     * @param sample Timer sample iniciado com startTimer()
     * @param operation Nome da operação (ex: "user_not_found", "validation_error")
     */
    public void recordDuration(Timer.Sample sample, String operation) {
        Timer timer = errorTimers.computeIfAbsent(operation, op ->
                Timer.builder("api_error_duration_seconds")
                        .description("Tempo para processar erros")
                        .tag("operation", operation)
                        .register(meterRegistry)
        );

        sample.stop(timer);
    }

    /**
     * Registra erro crítico que precisa atenção imediata.
     */
    public void recordCriticalError(String errorCode, String context) {
        Counter.builder("api_critical_errors_total")
                .description("Erros críticos que precisam atenção imediata")
                .tag("error_code", errorCode)
                .tag("context", context)
                .register(meterRegistry)
                .increment();

        log.warn("ERRO CRÍTICO registrado nas métricas - Code: {}, Context: {}", errorCode, context);
    }

    /**
     * Registra tentativa de acesso negado.
     * Útil para detectar tentativas de ataque.
     */
    public void recordSecurityEvent(String eventType, String userInfo, String endpoint) {
        Counter.builder("api_security_events_total")
                .description("Eventos de segurança")
                .tag("event_type", eventType) // "access_denied", "invalid_token", etc.
                .tag("user_type", maskUserInfo(userInfo))
                .tag("endpoint", cleanEndpoint(endpoint))
                .register(meterRegistry)
                .increment();

        log.warn("Evento de segurança registrado - Type: {}, Endpoint: {}", eventType, endpoint);
    }

    // ================================================================================================
    // MÉTODOS UTILITÁRIOS
    // ================================================================================================

    /**
     * Limpa endpoint para métricas (remove IDs específicos).
     * Exemplo: /users/123 -> /users/{id}
     */
    private String cleanEndpoint(String uri) {
        if (uri == null) return "unknown";

        return uri
                .replaceAll("/\\d+", "/{id}")                           // /123 -> /{id}
                .replaceAll("/[a-f0-9-]{36}", "/{uuid}")               // UUID -> /{uuid}
                .replaceAll("/[A-Z]\\d{4,}", "/{container_id}")        // C1234 -> /{container_id}
                .replaceAll("\\?.*", "")                               // Remove query params
                .toLowerCase();
    }

    /**
     * Determina severidade baseada no status HTTP.
     */
    private String getSeverity(String httpStatus) {
        if (httpStatus == null) return "unknown";

        switch (httpStatus.substring(0, 1)) {
            case "4": return "client_error";
            case "5": return "server_error";
            default: return "other";
        }
    }

    /**
     * Mascara informações de usuário para privacidade.
     */
    private String maskUserInfo(String userInfo) {
        if (userInfo == null || userInfo.equals("anonymous")) {
            return "anonymous";
        }

        // Categorizar por tipo ao invés de expor dados reais
        if (userInfo.toLowerCase().contains("admin")) {
            return "admin";
        } else if (userInfo.toLowerCase().contains("gerente")) {
            return "manager";
        } else {
            return "user";
        }
    }

    /**
     * Método para obter estatísticas atuais (útil para debugging).
     */
    public String getMetricsStatus() {
        return String.format(
                "ErrorMetricsCollector Status:\n" +
                        "- Error Counters: %d\n" +
                        "- Error Timers: %d\n" +
                        "- Registry Type: %s",
                errorCounters.size(),
                errorTimers.size(),
                meterRegistry.getClass().getSimpleName()
        );
    }
}