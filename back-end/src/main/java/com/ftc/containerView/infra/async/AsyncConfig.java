package com.ftc.containerView.infra.async;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuração para habilitar processamento assíncrono.
 *
 * Necessário para que o ErrorAlertService possa enviar emails
 * em background sem bloquear o processamento de requisições.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    // As configurações de pool de threads estão no application.properties:
    // spring.task.execution.pool.core-size=2
    // spring.task.execution.pool.max-size=5
    // spring.task.execution.pool.queue-capacity=100
    // spring.task.execution.thread-name-prefix=alert-
}