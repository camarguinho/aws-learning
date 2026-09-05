package br.com.camarguinho.awslearning.notification.domain;

import java.time.Instant;

/** Registro em memória de uma notificação processada, exposto via API para fins didáticos/observabilidade. */
public record NotificationRecord(Long orderId, String customerId, String channel, String status, Instant sentAt) {
}
