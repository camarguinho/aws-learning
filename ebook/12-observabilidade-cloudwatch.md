[⬅ Sumário](../README.md) · Capítulo 12 de 18

# 12. Observabilidade: Amazon CloudWatch

## O que é

CloudWatch é o serviço central de observabilidade da AWS, com três pilares:

- **Metrics**: séries temporais numéricas (ex.: CPUUtilization de uma
  instância, número de mensagens em uma fila SQS, uma métrica de negócio
  customizada como "notificações enviadas").
- **Logs**: CloudWatch Logs centraliza logs de aplicações e serviços
  gerenciados, com **Logs Insights** para consultas ad-hoc.
- **Alarms**: disparam uma ação (notificação SNS, Auto Scaling, etc.)
  quando uma métrica ultrapassa um limiar por um período definido.

## Métricas customizadas no projeto

O `notification-service` publica métricas de negócio (não apenas técnicas)
usando **Micrometer**, o mesmo padrão de instrumentação usado com
Prometheus/Datadog — o código de negócio não conhece o SDK do CloudWatch:

```java
// NotificationMetrics.java
public void incrementProcessed() {
    meterRegistry.counter("notifications.processed").increment();
}
public void incrementFailed() {
    meterRegistry.counter("notifications.failed").increment();
}
```

A dependência `micrometer-registry-cloudwatch2` autoconfigura um
`CloudWatchMeterRegistry` que exporta essas métricas periodicamente. A
configuração (`application.yml` do `notification-service`):

```yaml
management:
  cloudwatch:
    metrics:
      export:
        namespace: EcommerceAwsLearning
        step: 1m
        enabled: ${CLOUDWATCH_METRICS_ENABLED:false}
```

Está **desabilitado por padrão** no ambiente local (LocalStack Community
tem suporte limitado a `PutMetricData`, e não queremos custo/ruído por
padrão) — em produção, basta `CLOUDWATCH_METRICS_ENABLED=true` para
publicar de verdade, sem alterar nenhuma linha de código.

## Resilience4j + CloudWatch

`Resilience4jEventLoggingConfig.java` (módulo `common`) registra listeners
que logam toda transição de estado de circuit breaker
(CLOSED → OPEN → HALF_OPEN) e toda tentativa de retry. Em produção, esses
mesmos eventos alimentariam métricas do CloudWatch
(`resilience4j.circuitbreaker.state`, já exposta automaticamente via
Actuator + Micrometer quando `management.endpoints.web.exposure.include`
inclui `metrics`) — permitindo criar um **Alarm** que dispara se um
circuito ficar `OPEN` por muito tempo, sinal de uma dependência
persistentemente degradada.

## Alarmes de custo (preview do Capítulo 16)

CloudWatch Alarms não servem só para saúde técnica — combinados com o
**AWS Budgets**, são a base do controle de custo do
[Capítulo 16](16-custos-billing-finops.md): um alarme pode monitorar a
métrica `EstimatedCharges` (publicada automaticamente na região
`us-east-1` quando o billing alert está habilitado na conta) e notificar
via SNS/e-mail antes que a fatura do mês saia do esperado.

## Para o exame

- CloudWatch Logs precisa de um **CloudWatch Logs Agent/Unified CloudWatch
  Agent** (ou integração nativa, como o driver de logs do ECS) para
  enviar logs de uma instância EC2 — não é automático como em Lambda/ECS
  Fargate com log driver `awslogs`.
- Métricas customizadas (`PutMetricData`) são cobradas por métrica-mês
  (por combinação de namespace+nome+dimensões) — evite dimensões de alta
  cardinalidade (ex.: um ID de usuário como dimensão) para não gerar
  milhares de métricas distintas.
- Alarmes têm 3 estados: `OK`, `ALARM`, `INSUFFICIENT_DATA` (dados
  insuficientes para avaliar, comum logo após a criação do alarme).
- CloudWatch Logs Insights usa uma linguagem de consulta própria (não SQL
  puro) para buscar/agregar sobre volumes grandes de log rapidamente.

---
**Anterior:** [← 11. Config e Segredos](11-config-secrets-parameter-store.md) | **Próximo:** [13. Arquitetura da Solução →](13-arquitetura-da-solucao.md)
