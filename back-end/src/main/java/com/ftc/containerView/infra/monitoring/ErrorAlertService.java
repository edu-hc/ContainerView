package com.ftc.containerView.infra.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serviço de alertas automáticos para erros da API.
 *
 * Funcionalidades:
 * 1. Alertas imediatos para erros críticos (5xx)
 * 2. Alertas por threshold (muitos erros em pouco tempo)
 * 3. Cooldown para evitar spam de emails
 * 4. Categorização por severidade
 * 5. Templates de email informativos
 *
 * Configuração via application.properties:
 * app.monitoring.alerts.email=admin@company.com
 * app.monitoring.alerts.threshold=5
 * app.monitoring.alerts.cooldown-minutes=10
 * app.monitoring.alerts.enabled=true
 */
@Service
@RequiredArgsConstructor
@Slf4j
@EnableAsync
public class ErrorAlertService {

    private final JavaMailSender emailSender;

    // Contadores thread-safe
    private final AtomicInteger totalErrorCount = new AtomicInteger(0);
    private final AtomicInteger criticalErrorCount = new AtomicInteger(0);
    private final AtomicLong lastCriticalAlertTime = new AtomicLong(0);
    private final AtomicLong lastThresholdAlertTime = new AtomicLong(0);

    // Configurações via properties
    @Value("${app.monitoring.alerts.email:admin@company.com}")
    private String alertEmail;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.monitoring.alerts.threshold:5}")
    private int errorThreshold;

    @Value("${app.monitoring.alerts.cooldown-minutes:10}")
    private int cooldownMinutes;

    @Value("${app.monitoring.alerts.enabled:true}")
    private boolean alertsEnabled;

    @Value("${app.monitoring.alerts.critical-cooldown-minutes:5}")
    private int criticalCooldownMinutes;

    @Value("${spring.application.name:ContainerView}")
    private String applicationName;

    // ================================================================================================
    // MÉTODOS PRINCIPAIS DE ALERTA
    // ================================================================================================

    /**
     * Processa um erro e decide se deve enviar alerta.
     *
     * @param errorCode Código do erro (ex: "INTERNAL_ERROR")
     * @param errorId ID único do erro para rastreamento
     * @param path Endpoint onde ocorreu o erro
     * @param details Detalhes do erro (ex: exception message)
     * @param userInfo Informação do usuário (para contexto)
     */
    public void processError(String errorCode, String errorId, String path, String details, String userInfo) {
        if (!alertsEnabled) {
            log.debug("Alertas desabilitados - ignorando erro {}", errorId);
            return;
        }

        totalErrorCount.incrementAndGet();

        ErrorSeverity severity = determineErrorSeverity(errorCode);

        switch (severity) {
            case CRITICAL:
                handleCriticalError(errorCode, errorId, path, details, userInfo);
                break;
            case HIGH:
                handleHighSeverityError(errorCode, errorId, path, details, userInfo);
                break;
            case MEDIUM:
            case LOW:
                handleThresholdBasedAlert(errorCode, errorId, path, details);
                break;
        }

        log.debug("Erro processado para alertas - Code: {}, Severity: {}, ID: {}",
                errorCode, severity, errorId);
    }

    /**
     * Força envio de alerta (útil para testes).
     */
    public void sendTestAlert() {
        if (!alertsEnabled) {
            log.warn("Tentativa de enviar alerta de teste, mas alertas estão desabilitados");
            return;
        }

        sendAlert(
                "TESTE DE ALERTA - " + applicationName,
                buildTestAlertMessage(),
                AlertType.TEST
        );
    }

    // ================================================================================================
    // HANDLERS POR SEVERIDADE
    // ================================================================================================

    /**
     * Processa erros críticos (5xx) - alerta imediato.
     */
    private void handleCriticalError(String errorCode, String errorId, String path, String details, String userInfo) {
        criticalErrorCount.incrementAndGet();
        long now = System.currentTimeMillis();
        long lastAlert = lastCriticalAlertTime.get();

        // Cooldown para evitar spam de alertas críticos
        if (now - lastAlert > criticalCooldownMinutes * 60 * 1000) {
            sendAlert(
                    "ERRO CRÍTICO - " + applicationName,
                    buildCriticalAlertMessage(errorCode, errorId, path, details, userInfo),
                    AlertType.CRITICAL
            );

            lastCriticalAlertTime.set(now);
            log.warn("Alerta crítico enviado - Error ID: {}, Code: {}", errorId, errorCode);
        } else {
            log.debug("Alerta crítico em cooldown - Error ID: {}", errorId);
        }
    }

    /**
     * Processa erros de alta severidade - alerta com menos urgência.
     */
    private void handleHighSeverityError(String errorCode, String errorId, String path, String details, String userInfo) {
        // Alta severidade ainda gera alerta, mas com cooldown maior
        long now = System.currentTimeMillis();
        long lastAlert = lastCriticalAlertTime.get();

        if (now - lastAlert > (criticalCooldownMinutes * 2) * 60 * 1000) {
            sendAlert(
                    "ERRO DE ALTA SEVERIDADE - " + applicationName,
                    buildHighSeverityAlertMessage(errorCode, errorId, path, details, userInfo),
                    AlertType.HIGH_SEVERITY
            );

            lastCriticalAlertTime.set(now);
        }
    }

    /**
     * Processa erros baseado em threshold - muitos erros em pouco tempo.
     */
    private void handleThresholdBasedAlert(String errorCode, String errorId, String path, String details) {
        if (totalErrorCount.get() >= errorThreshold) {
            long now = System.currentTimeMillis();
            long lastAlert = lastThresholdAlertTime.get();

            if (now - lastAlert > cooldownMinutes * 60 * 1000) {
                sendAlert(
                        "THRESHOLD DE ERROS ATINGIDO - " + applicationName,
                        buildThresholdAlertMessage(totalErrorCount.get(), errorCode, errorId, path),
                        AlertType.THRESHOLD
                );

                lastThresholdAlertTime.set(now);
                totalErrorCount.set(0); // Reset contador
                log.warn("Alerta de threshold enviado - {} erros detectados", errorThreshold);
            }
        }
    }

    // ================================================================================================
    // ENVIO DE EMAILS
    // ================================================================================================

    /**
     * Envia alerta por email de forma assíncrona.
     */
    @Async
    protected void sendAlert(String subject, String message, AlertType alertType) {
        try {
            SimpleMailMessage emailMessage = new SimpleMailMessage();
            emailMessage.setTo(alertEmail);
            emailMessage.setSubject(subject);
            emailMessage.setText(message);
            emailMessage.setFrom(fromEmail);

            emailSender.send(emailMessage);

            log.info("Alerta enviado com sucesso - Type: {}, Para: {}", alertType, alertEmail);

        } catch (Exception e) {
            log.error("FALHA ao enviar alerta por email - Type: {}, Error: {}", alertType, e.getMessage(), e);

            // TODO: Implementar fallback (Slack, SMS, etc.)
            handleAlertFailure(alertType, e);
        }
    }

    /**
     * Trata falhas no envio de alertas.
     */
    private void handleAlertFailure(AlertType alertType, Exception error) {
        // Por enquanto só loga, mas poderia:
        // 1. Tentar enviar via Slack
        // 2. Gravar em arquivo para retry posterior
        // 3. Enviar SMS de emergência
        log.error("SISTEMA DE ALERTAS FALHANDO - Type: {}, verificar configuração de email!", alertType);
    }

    // ================================================================================================
    // TEMPLATES DE MENSAGEM
    // ================================================================================================

    private String buildCriticalAlertMessage(String errorCode, String errorId, String path, String details, String userInfo) {
        return String.format(
                "ERRO CRÍTICO DETECTADO\n\n" +
                        "Sistema: %s\n" +
                        "Timestamp: %s\n" +
                        "Severidade: CRÍTICA - AÇÃO IMEDIATA NECESSÁRIA\n\n" +
                        "Detalhes do Erro:\n" +
                        "├─ Código: %s\n" +
                        "├─ ID: %s\n" +
                        "├─ Endpoint: %s\n" +
                        "├─ Usuário: %s\n" +
                        "└─ Detalhes: %s\n\n" +
                        "AÇÕES RECOMENDADAS:\n" +
                        "1. Verificar logs imediatamente\n" +
                        "2. Monitorar métricas de sistema\n" +
                        "3. Verificar conectividade com serviços externos\n\n" +
                        "LINKS ÚTEIS:\n" +
                        "• Logs: http://localhost:8080/actuator/loggers\n" +
                        "• Métricas: http://localhost:8080/actuator/metrics\n" +
                        "• Health: http://localhost:8080/actuator/health\n\n" +
                        "Este alerta tem cooldown de %d minutos.",
                applicationName,
                getCurrentTimestamp(),
                errorCode, errorId, path, maskUserInfo(userInfo), details,
                criticalCooldownMinutes
        );
    }

    private String buildHighSeverityAlertMessage(String errorCode, String errorId, String path, String details, String userInfo) {
        return String.format(
                "ERRO DE ALTA SEVERIDADE\n\n" +
                        "Sistema: %s\n" +
                        "Timestamp: %s\n\n" +
                        "Detalhes:\n" +
                        "├─ Código: %s\n" +
                        "├─ ID: %s\n" +
                        "├─ Endpoint: %s\n" +
                        "├─ Usuário: %s\n" +
                        "└─ Detalhes: %s\n\n" +
                        "Verificar se há padrão de falhas ou problemas sistêmicos.\n\n" +
                        "Métricas: http://localhost:8080/actuator/metrics",
                applicationName, getCurrentTimestamp(),
                errorCode, errorId, path, maskUserInfo(userInfo), details
        );
    }

    private String buildThresholdAlertMessage(int errorCount, String lastErrorCode, String lastErrorId, String lastPath) {
        return String.format(
                "THRESHOLD DE ERROS ATINGIDO\n\n" +
                        "Sistema: %s\n" +
                        "Timestamp: %s\n\n" +
                        "Estatísticas:\n" +
                        "├─ Total de erros: %d (últimos %d minutos)\n" +
                        "├─ Threshold configurado: %d\n" +
                        "├─ Último erro: %s\n" +
                        "├─ Último ID: %s\n" +
                        "└─ Último endpoint: %s\n\n" +
                        "Possíveis causas:\n" +
                        "• Alto volume de tráfego\n" +
                        "• Problemas de rede\n" +
                        "• Dados de entrada inválidos\n" +
                        "• Falha em serviços externos\n\n" +
                        "Verificar se há problemas sistêmicos ou picos de uso.",
                applicationName, getCurrentTimestamp(),
                errorCount, cooldownMinutes, errorThreshold,
                lastErrorCode, lastErrorId, lastPath
        );
    }

    private String buildTestAlertMessage() {
        return String.format(
                "TESTE DE ALERTA\n\n" +
                        "Sistema: %s\n" +
                        "Timestamp: %s\n\n" +
                        "Este é um alerta de teste para verificar se o sistema de monitoramento está funcionando corretamente.\n\n" +
                        "Configuração atual:\n" +
                        "├─ Email de alerta: %s\n" +
                        "├─ Threshold: %d erros\n" +
                        "├─ Cooldown: %d minutos\n" +
                        "├─ Alertas habilitados: %s\n" +
                        "└─ Total de erros desde o último reset: %d\n\n" +
                        "Se você recebeu este email, o sistema de alertas está funcionando!",
                applicationName, getCurrentTimestamp(),
                alertEmail, errorThreshold, cooldownMinutes, alertsEnabled, totalErrorCount.get()
        );
    }

    // ================================================================================================
    // UTILITÁRIOS
    // ================================================================================================

    private ErrorSeverity determineErrorSeverity(String errorCode) {
        if (errorCode == null) return ErrorSeverity.LOW;

        switch (errorCode.toUpperCase()) {
            case "INTERNAL_ERROR":
            case "DATABASE_ERROR":
            case "EXTERNAL_SERVICE_ERROR":
                return ErrorSeverity.CRITICAL;

            case "IMAGE_STORAGE_ERROR":
            case "EMAIL_SEND_ERROR":
            case "AUTHENTICATION_FAILED":
                return ErrorSeverity.HIGH;

            case "VALIDATION_ERROR":
            case "ACCESS_DENIED":
                return ErrorSeverity.MEDIUM;

            default:
                return ErrorSeverity.LOW;
        }
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    private String maskUserInfo(String userInfo) {
        if (userInfo == null || userInfo.equals("anonymous")) {
            return "usuário anônimo";
        }
        return "usuário autenticado";
    }

    /**
     * Reseta contadores (útil para testes ou manutenção).
     */
    public void resetCounters() {
        totalErrorCount.set(0);
        criticalErrorCount.set(0);
        log.info("Contadores de erro resetados");
    }

    /**
     * Retorna estatísticas atuais.
     */
    public String getAlertStats() {
        return String.format(
                "ErrorAlertService Stats:\n" +
                        "├─ Alertas habilitados: %s\n" +
                        "├─ Email configurado: %s\n" +
                        "├─ Threshold: %d erros\n" +
                        "├─ Cooldown: %d minutos\n" +
                        "├─ Total de erros: %d\n" +
                        "├─ Erros críticos: %d\n" +
                        "└─ Último alerta crítico: %s",
                alertsEnabled, alertEmail, errorThreshold, cooldownMinutes,
                totalErrorCount.get(), criticalErrorCount.get(),
                lastCriticalAlertTime.get() > 0 ? "há " +
                        ((System.currentTimeMillis() - lastCriticalAlertTime.get()) / 60000) + " minutos" : "nunca"
        );
    }

    // ================================================================================================
    // INICIALIZAÇÃO
    // ================================================================================================

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        log.info("Sistema de alertas inicializado - Email: {}, Threshold: {}, Habilitado: {}",
                alertEmail, errorThreshold, alertsEnabled);

        if (alertsEnabled) {
            // Opcional: enviar alerta de inicialização
            // sendTestAlert();
        }
    }

    // ================================================================================================
    // ENUMS
    // ================================================================================================

    public enum ErrorSeverity {
        CRITICAL, HIGH, MEDIUM, LOW
    }

    public enum AlertType {
        CRITICAL, HIGH_SEVERITY, THRESHOLD, TEST
    }
}