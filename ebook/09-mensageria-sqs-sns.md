[⬅ Sumário](../README.md) · Capítulo 9 de 18

# 9. Mensageria: Amazon SQS e Amazon SNS

## Amazon SQS (Simple Queue Service)

Fila gerenciada, totalmente durável, para desacoplar produtor e consumidor.
Tipos:

- **Standard Queue** (usada no projeto): throughput praticamente ilimitado,
  ordenação **não garantida**, entrega **at-least-once** (uma mensagem pode
  chegar duplicada — o consumidor deve ser idempotente).
- **FIFO Queue**: ordenação garantida e entrega exactly-once, com
  throughput limitado (até 300 msg/s sem batching, 3000 com batching).

Conceitos cobrados na prova:

- **Visibility Timeout**: quando um consumidor recebe uma mensagem, ela
  fica "invisível" para outros consumidores por esse período. Se o
  consumidor não a apagar (ack) a tempo, ela volta a ficar visível.
- **Dead Letter Queue (DLQ)**: fila auxiliar para onde mensagens que
  falharam repetidamente (`maxReceiveCount` excedido) são movidas
  automaticamente, evitando reprocessamento infinito ("poison pill").
- **Long Polling**: o consumidor espera até N segundos por uma mensagem
  antes de retornar vazio, reduzindo chamadas de API vazias (e custo)
  comparado a Short Polling.

## Amazon SNS (Simple Notification Service)

Serviço de **pub/sub**: um produtor publica uma mensagem em um **tópico**,
e o SNS entrega uma cópia para **todos** os assinantes (fan-out) — SQS,
Lambda, HTTP/S, e-mail, SMS, push mobile.

## Como o projeto combina os dois: fan-out SNS → SQS

O `order-service` publica o evento de domínio no **SNS**
(`OrderEventPublisher.java`), não diretamente em uma fila:

```java
public void publish(OrderCreatedEvent event) {
    snsTemplate.convertAndSend(topicName, event);
}
```

A fila `order-events-queue` está inscrita nesse tópico (ver
`infra/localstack/init/init-aws.sh`, com `RawMessageDelivery=true` para o
corpo da mensagem SQS ser o JSON puro do evento, sem o envelope do SNS). O
`notification-service` consome dessa fila via `@SqsListener` (Spring Cloud
AWS SQS):

```java
@SqsListener("order-events-queue")
public void onOrderCreated(OrderCreatedEvent event) {
    emailNotificationService.sendOrderConfirmation(event);
    ...
}
```

### Por que não publicar direto na fila?

Porque **o `order-service` não deveria saber quem consome o evento**. Com
SNS no meio, adicionar um novo consumidor (uma fila de analytics, uma
Lambda de detecção de fraude, um webhook de parceiro) é só inscrever um
novo assinante no tópico — **zero mudança de código ou deploy** no
`order-service`. Esse desacoplamento produtor/consumidor é o ganho central
de uma arquitetura orientada a eventos, e é justamente o que o diagrama
[`02-fluxo-criacao-pedido.drawio`](diagrams/02-fluxo-criacao-pedido.drawio)
ilustra passo a passo.

## Dead Letter Queue em ação

A fila `order-events-dlq` (criada antes da fila principal no script de
init) recebe mensagens que o `notification-service` falhou em processar
repetidamente. Em `OrderEventListener.java`, uma falha no envio do e-mail
**relança a exceção**:

```java
} catch (Exception ex) {
    notificationMetrics.incrementFailed();
    notificationHistoryService.record(...);
    throw ex; // não confirma (ack) a mensagem — ela volta à fila
}
```

Não confirmar a mensagem é o que permite ao SQS reentregá-la (respeitando o
Visibility Timeout) e, após esgotar as tentativas configuradas na
Redrive Policy da fila, movê-la automaticamente para a DLQ — de onde um
operador pode inspecionar e reprocessar manualmente, sem perder a mensagem.

## Para o exame

- SQS Standard: **at-least-once**, sem ordenação; SQS FIFO: **exactly-once**,
  ordenado, throughput menor. Escolha baseada no requisito de negócio.
- SNS + SQS (fan-out) é o padrão de referência para múltiplos consumidores
  independentes de um mesmo evento — mais barato e simples que múltiplas
  filas alimentadas manualmente pelo produtor.
- Visibility Timeout deve ser maior que o tempo esperado de processamento
  da mensagem pelo consumidor, senão ela é reentregue prematuramente
  (processamento duplicado).
- SQS/SNS são cobrados por **número de requisições** (após uma faixa
  gratuita mensal) — filas/tópicos ociosos não custam nada além disso.

---
**Anterior:** [← 8. ElastiCache](08-cache-elasticache.md) | **Próximo:** [10. E-mail Transacional: Amazon SES →](10-email-ses.md)
