package br.com.camarguinho.awslearning.notification.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Contrato do evento consumido do SQS, espelhando o
 * {@code OrderCreatedEvent} publicado pelo order-service.
 *
 * <p>É intencional que cada microsserviço tenha sua própria cópia do
 * contrato do evento (schema on read, na fronteira do consumidor) em vez de
 * compartilhar uma classe via o módulo {@code common}: isso evita acoplar o
 * deploy dos dois serviços a uma mesma versão de biblioteca, permitindo que
 * evoluam de forma independente — um dos princípios de arquiteturas
 * orientadas a eventos.</p>
 */
public record OrderCreatedEvent(Long orderId, String customerId, BigDecimal total, Instant occurredAt) {
}
