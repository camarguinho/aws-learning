[⬅ Sumário](../README.md) · Capítulo 17 de 18

# 17. Guia de Uso e Swagger/OpenAPI

## Subindo o ambiente

```bash
docker compose up --build
```

Aguarde os healthchecks (LocalStack, Postgres e Redis) — o
`docker-compose.yml` só inicia os microsserviços depois que a infra
declarada como dependência está saudável.

## Swagger UI de cada serviço

Cada microsserviço expõe sua documentação OpenAPI interativa via
springdoc-openapi:

| Serviço | Swagger UI | OpenAPI JSON |
|---|---|---|
| catalog-service | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| order-service | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| notification-service | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs |

Use o Swagger UI para explorar cada endpoint, ver os schemas de
request/response (gerados a partir dos `record` Java anotados com
`@Schema`) e disparar chamadas de teste diretamente do navegador ("Try it
out").

## Passo a passo: fluxo completo de ponta a ponta

### 1. Criar um produto no catálogo

```bash
curl -X POST http://localhost:8081/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Teclado Mecânico RGB",
        "description": "Switches azuis, iluminação RGB",
        "price": 349.90,
        "stockQuantity": 25
      }'
```

Resposta: `201 Created` com o `productId` gerado (um UUID). Guarde esse id.

### 2. (Opcional) Gerar URL de upload de imagem

```bash
curl -X POST http://localhost:8081/api/v1/products/{productId}/image-upload-url
```

Devolve uma `uploadUrl` pré-assinada do S3 (LocalStack). Um `PUT` nessa URL
com o binário da imagem simula o upload direto ao bucket.

### 3. Consultar o produto (primeira leitura = cache miss)

```bash
curl http://localhost:8081/api/v1/products/{productId}
```

Repita a mesma chamada: a segunda é servida pelo cache Redis
(`@Cacheable`) — para confirmar, acompanhe os logs do `catalog-service`
(não há log explícito de hit/miss por padrão, mas a latência da segunda
chamada é visivelmente menor; para inspeção mais direta, conecte no Redis
com `docker exec -it aws-learning-redis redis-cli KEYS "products*"`).

### 4. Criar um pedido

```bash
curl -X POST http://localhost:8082/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
        "customerId": "customer-123",
        "items": [ { "productId": "SEU_PRODUCT_ID", "quantity": 2 } ]
      }'
```

Internamente: o `order-service` chama o `catalog-service` (com circuit
breaker/retry/bulkhead), grava o pedido no Postgres, e publica o evento no
SNS. Resposta: `201 Created` com o total calculado.

### 5. Conferir a notificação processada

```bash
curl http://localhost:8083/api/v1/notifications
```

Deve listar a notificação do pedido recém-criado, com `status: SENT`. O
"envio" de e-mail via SES no LocalStack não entrega de fato uma caixa de
entrada — mas registra a chamada, suficiente para validar a integração
(em uma conta AWS real, fora do sandbox, o e-mail seria entregue de
verdade).

### 6. Testar resiliência (opcional, mas recomendado)

```bash
docker compose stop catalog-service
curl -X POST http://localhost:8082/api/v1/orders -H "Content-Type: application/json" \
  -d '{"customerId":"customer-123","items":[{"productId":"qualquer-id","quantity":1}]}'
```

Após a `catalog-service` estar consistentemente fora do ar, o circuit
breaker do `order-service` abre e a resposta passa a ser `503 Service
Unavailable` quase instantaneamente (sem esperar o timeout de rede) —
acompanhe os logs do `order-service` para ver a transição de estado
logada por `Resilience4jEventLoggingConfig`. Religue com
`docker compose start catalog-service`.

## Actuator: saúde e métricas

Todos os serviços expõem:

- `GET /actuator/health` — status agregado (inclui conectividade com
  Redis/DB quando aplicável);
- `GET /actuator/metrics` — lista de métricas disponíveis;
- `GET /actuator/prometheus` — métricas em formato Prometheus (incluindo
  as métricas do Resilience4j: `resilience4j.circuitbreaker.state`,
  `resilience4j.retry.calls`, etc., e as customizadas do
  `notification-service`: `notifications.processed`,
  `notifications.failed`).

## Encerrando o ambiente

```bash
docker compose down          # para os containers, mantém os volumes
docker compose down -v       # para e apaga os dados (Postgres/LocalStack)
```

---
**Anterior:** [← 16. Custos e Billing](16-custos-billing-finops.md) | **Próximo:** [18. Referências Oficiais AWS →](18-referencias.md)
