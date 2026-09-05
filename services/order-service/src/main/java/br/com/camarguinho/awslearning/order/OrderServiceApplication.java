package br.com.camarguinho.awslearning.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada do order-service.
 *
 * <p>Serviços AWS explorados neste microsserviço:</p>
 * <ul>
 *     <li><b>Amazon RDS (PostgreSQL)</b> — persistência transacional dos pedidos;</li>
 *     <li><b>AWS Secrets Manager</b> — credenciais do banco, injetadas em tempo de
 *     bootstrap via {@code spring.config.import=aws-secretsmanager:...};</li>
 *     <li><b>AWS Systems Manager Parameter Store</b> — configuração externa
 *     (URL do banco, etc.) via {@code spring.config.import=aws-parameterstore:...};</li>
 *     <li><b>Amazon SNS</b> — publicação do evento {@code OrderCreatedEvent}
 *     para fan-out a múltiplos consumidores (aqui, o notification-service via
 *     uma fila SQS inscrita no tópico).</li>
 * </ul>
 */
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
