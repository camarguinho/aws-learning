[⬅ Sumário](../README.md) · Capítulo 14 de 18

# 14. Cloud Native e Boas Práticas AWS

## O que "cloud native" significa na prática (não é buzzword)

Uma aplicação cloud native é projetada **para** a nuvem, não apenas
**hospedada na** nuvem. Os princípios abaixo, todos aplicados neste
projeto, são o que diferencia os dois:

### 1. Stateless compute

Os três microsserviços não guardam estado de sessão em memória entre
requisições (a única exceção deliberada e documentada é o histórico de
notificações em memória do `notification-service`, feito só para fins
didáticos de inspeção local). Isso permite rodar **N réplicas
intercambiáveis** atrás de um load balancer, e o Auto Scaling pode
adicionar/remover instâncias a qualquer momento sem afetar requisições em
andamento nas demais.

### 2. Configuração externalizada (12-factor app)

Nenhuma configuração sensível ou dependente de ambiente está no código ou
compilada no artefato — tudo vem de fora: Secrets Manager, Parameter
Store, variáveis de ambiente (`docker-compose.yml`). O mesmo JAR roda em
qualquer ambiente (local, staging, produção) apenas trocando de onde a
configuração é lida — ver [Capítulo 11](11-config-secrets-parameter-store.md).

### 3. Falha é esperada, não excepcional

Toda chamada entre serviços (HTTP síncrono ou para um serviço AWS) está
protegida por **circuit breaker, retry e/ou bulkhead** (Resilience4j, ver
[Capítulo 15](15-resiliencia-resilience4j.md)). O código assume que a
rede, o S3, o catalog-service podem falhar a qualquer momento — e
degrada de forma previsível (fallback) em vez de propagar o erro cru.

### 4. Desacoplamento via eventos

O `order-service` não sabe (nem deveria saber) que o `notification-service`
existe — ele publica um fato de domínio no SNS. Isso é o oposto de um
monólito com chamadas diretas em cadeia, onde uma falha em um componente
distante derruba o fluxo inteiro.

### 5. Observabilidade em primeira classe

Métricas de negócio (não só técnicas) são instrumentadas desde o começo
(`NotificationMetrics`), não adicionadas depois "quando der um
problema em produção". Ver [Capítulo 12](12-observabilidade-cloudwatch.md).

### 6. Infraestrutura descartável

Cada serviço tem um `Dockerfile` que produz uma imagem imutável — subir
uma nova versão é substituir o container, não corrigir um servidor "no
local". O `docker-compose.yml` já expressa essa infraestrutura como
código; em produção, o equivalente seria Terraform/CDK/CloudFormation
(fora do escopo deste projeto, mas o próximo passo natural de evolução).

## Checklist do AWS Well-Architected aplicado

Revisitando os 6 pilares do [Capítulo 1](01-fundamentos-aws-well-architected.md)
com exemplos concretos do código:

- ✅ **Segurança**: buckets privados + URL assinada (`ProductImageService`),
  segredos fora do código (`order-service/application.yml`), exceções
  mapeadas sem vazar stacktrace ao cliente (`GlobalExceptionHandler`).
- ✅ **Confiabilidade**: DLQ no SQS, circuit breaker/retry/bulkhead,
  optimistic locking no DynamoDB (`@DynamoDbVersionAttribute`).
- ✅ **Performance**: cache-aside no Redis, upload direto ao S3 (sem
  proxy), acesso por chave no DynamoDB.
- ✅ **Excelência operacional**: Actuator + health checks em todos os
  serviços, logs estruturados, métricas customizadas.
- ✅ **Otimização de custo**: on-demand no DynamoDB, TTL de cache
  configurável, alarmes de billing recomendados no
  [Capítulo 16](16-custos-billing-finops.md).
- ⚠️ **Sustentabilidade**: fora do escopo prático deste projeto de estudo,
  mas o princípio (dimensionar certo, desligar o que não usa, usar
  serverless/on-demand sempre que o padrão de acesso permitir) está
  implícito em quase todas as escolhas acima.

## O que este projeto **não** faz (e por quê é honesto dizer isso)

Para manter o escopo didático e executável localmente sem uma conta AWS:

- Não usa Infra as Code (Terraform/CDK) — o `docker-compose.yml` cumpre
  esse papel localmente.
- Não implementa autenticação de usuário final de ponta a ponta (Cognito
  está documentado no [Capítulo 2](02-iam-seguranca-cognito.md) como
  próximo passo, com o código de configuração pronto, mas desabilitado).
- Não tem pipeline de CI/CD — o foco é o runtime da aplicação e os
  serviços AWS que ela consome.

Reconhecer esses limites é, em si, uma prática de arquitetura saudável:
escopo claro é melhor que "fazer tudo pela metade".

---
**Anterior:** [← 13. Arquitetura](13-arquitetura-da-solucao.md) | **Próximo:** [15. Resiliência com Resilience4j →](15-resiliencia-resilience4j.md)
