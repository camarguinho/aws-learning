[⬅ Sumário](../README.md) · Capítulo 15 de 18

# 15. Resiliência com Resilience4j

## Por que resiliência é parte da arquitetura AWS (não um "extra")

Em uma arquitetura distribuída, toda chamada de rede pode falhar: um outro
microsserviço, o S3, o SES. O AWS Well-Architected trata isso como
premissa ("tudo falha o tempo todo", citação clássica de arquitetos da
AWS) — o papel do Resilience4j aqui é **conter e degradar com graça** essas
falhas, em vez de deixá-las se propagarem e derrubarem serviços saudáveis.

## Os três padrões usados no projeto

### 1. Circuit Breaker

Interrompe chamadas a uma dependência que está falhando consistentemente,
"abrindo o circuito" e falhando rápido (sem sequer tentar a chamada) por
um tempo, antes de testar (`HALF_OPEN`) se a dependência voltou.

Usado em `CatalogClient.java` (order-service → catalog-service) e em
`ProductImageService.java` (catalog-service → S3):

```java
@CircuitBreaker(name = "catalog-service", fallbackMethod = "fallbackFindProduct")
@Bulkhead(name = "catalog-service")
@Retry(name = "catalog-service")
public ProductDto findProduct(String productId) { ... }
```

Configuração (`order-service/application.yml`):

```yaml
resilience4j:
  circuitbreaker:
    instances:
      catalog-service:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
        permitted-number-of-calls-in-half-open-state: 3
```

Leitura: a cada 10 chamadas (janela deslizante), se pelo menos 5 já
ocorreram e 50% ou mais falharam, o circuito abre por 10 segundos; depois
disso, permite 3 chamadas de teste antes de decidir se fecha de novo.

### 2. Retry

Repete automaticamente uma chamada que falhou, útil para falhas
**transitórias** (glitch de rede, throttling momentâneo do SES/S3):

```yaml
resilience4j:
  retry:
    instances:
      catalog-service:
        max-attempts: 3
        wait-duration: 200ms
```

Cuidado conceitual (cobrado em entrevistas): retry sem limite ou sem
backoff pode **amplificar** uma sobrecarga já existente na dependência —
por isso o `max-attempts` é sempre baixo, e o Circuit Breaker existe
exatamente para parar de tentar quando o retry sozinho não é suficiente.

### 3. Bulkhead

Limita quantas chamadas **concorrentes** um cliente pode ter em voo para
uma dependência, isolando o impacto de uma lentidão (analogia náutica: um
compartimento estanque não deixa toda a água invadir o navio):

```yaml
resilience4j:
  bulkhead:
    instances:
      catalog-service:
        max-concurrent-calls: 10
```

Sem isso, uma lentidão no `catalog-service` poderia esgotar todas as
threads do `order-service` esperando resposta, derrubando também
endpoints do `order-service` que não dependem do catálogo.

## Observando resiliência em ação

`Resilience4jEventLoggingConfig.java` (módulo `common`) registra listeners
que logam cada transição de estado do circuit breaker e cada tentativa de
retry — abra os logs do `order-service` e derrube o `catalog-service`
(`docker compose stop catalog-service`) para ver o circuito abrir na
prática, e o fallback (`CatalogUnavailableException`, mapeado para
HTTP 503 por `GlobalExceptionHandler`) responder ao cliente de forma
controlada em vez de travar a requisição.

## Fallback: falhar de forma útil

Cada padrão acima só é completo com uma decisão de **fallback** — o que
fazer quando a chamada não pode ser completada:

- `CatalogClient.fallbackFindProduct`: lança `CatalogUnavailableException`
  (HTTP 503) — não há valor seguro para "inventar" preço/estoque.
- `ProductImageService.fallbackSignedUrl`: retorna `null` — a foto do
  produto é informação degradável; o cliente pode mostrar um placeholder
  em vez de quebrar a página inteira.

Essa distinção — o que pode degradar silenciosamente vs. o que deve falhar
explicitamente — é uma decisão de negócio, não só técnica, e vale a pena
justificá-la em qualquer entrevista técnica.

## Para discussões técnicas/entrevistas

- Resilience4j é a sucessora "leve" do Hystrix (descontinuado pela
  Netflix) no ecossistema Spring — baseada em decoradores funcionais, sem
  a dependência de RxJava.
- Combinar Circuit Breaker + Retry exige ordem cuidadosa: o Retry deve
  estar **dentro** do Circuit Breaker (repetir antes de contar como falha
  para o circuito) — no Spring, isso é resolvido pela ordem correta de
  aplicação dos decorators, configurável via `resilience4j.retry.instances.*`.
- Timeout explícito (não coberto em detalhe aqui, mas configurável via
  `TimeLimiter` do Resilience4j) deveria complementar esses três padrões
  em uma implementação de produção mais completa.

---
**Anterior:** [← 14. Cloud Native](14-cloud-native-boas-praticas.md) | **Próximo:** [16. Custos, Billing e FinOps →](16-custos-billing-finops.md)
