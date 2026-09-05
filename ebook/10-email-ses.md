[⬅ Sumário](../README.md) · Capítulo 10 de 18

# 10. E-mail Transacional: Amazon SES

## O que é

SES (Simple Email Service) é um serviço de envio (e recebimento) de e-mail
em escala, cobrado por e-mail enviado/recebido — muito mais barato que
operar seu próprio servidor SMTP, com boa entregabilidade por gerenciar
reputação de IP e conformidade com padrões anti-spam (SPF, DKIM, DMARC).

Conceitos para o exame:

- **Sandbox**: toda conta SES nova começa em modo sandbox — só envia para
  endereços/domínios **verificados manualmente**, com limite baixo de
  envio. É preciso solicitar saída do sandbox (production access) para
  enviar livremente.
- **Verified Identity**: um e-mail ou domínio precisa ser verificado antes
  de ser usado como remetente (`From`).
- **Configuration Sets**: agrupam regras de tracking (abertura, clique,
  bounce, complaint) e podem publicar eventos no SNS/CloudWatch.

## Uso no projeto

O `notification-service` envia o e-mail de confirmação de pedido ao
consumir o evento do SQS. O Spring Cloud AWS registra automaticamente um
`MailSender` (interface padrão do Spring) implementado sobre o SES quando
o starter `spring-cloud-aws-starter-ses` está no classpath — o código de
negócio não conhece o SDK da AWS:

```java
// EmailNotificationService.java
@Retry(name = "ses")
public void sendOrderConfirmation(OrderCreatedEvent event) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(...);
    message.setSubject("Pedido #" + event.orderId() + " confirmado");
    message.setText(...);
    mailSender.send(message);
}
```

O endereço remetente (`no-reply@ecommerce.example.com`) é verificado no
provisionamento local (`infra/localstack/init/init-aws.sh`,
`ses verify-email-identity`) e injetado via **SSM Parameter Store**
(`/ecommerce/notification-service/from-email`).

Há **retry** (Resilience4j) nessa chamada porque o SES pode devolver
throttling (limite de envios por segundo) em contas novas/sandbox — uma
nova tentativa com pequeno atraso normalmente resolve.

## Para o exame

- Toda identidade de remetente (e-mail ou domínio) precisa estar
  **verificada** antes do primeiro envio — mesmo fora do sandbox.
- SES não é um serviço de fila — se você precisa desacoplar o momento do
  "eu decidi enviar" do "eu de fato enviei" (como fizemos aqui, via
  SQS antes de chamar o SES), a combinação com SQS/SNS é o padrão
  recomendado, dando retry e durabilidade sem se tornar acoplado ao SES.
- SES cobra por e-mail enviado (com uma faixa gratuita quando enviado a
  partir de dentro da AWS, ex.: de dentro do EC2), além de custo extra por
  anexos grandes (calculado por 256 KB de dados).
- Amazon SNS também pode notificar por e-mail (assinatura tipo `email`)
  mas não é feito para e-mails transacionais ricos/templates — é para
  alertas simples de sistema (ex.: um alarme de billing, ver
  [Capítulo 16](16-custos-billing-finops.md)). Não confundir os dois usos
  na prova.

---
**Anterior:** [← 9. SQS e SNS](09-mensageria-sqs-sns.md) | **Próximo:** [11. Configuração e Segredos →](11-config-secrets-parameter-store.md)
