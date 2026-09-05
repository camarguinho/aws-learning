[⬅ Sumário](../README.md) · Capítulo 11 de 18

# 11. Configuração e Segredos: SSM Parameter Store e Secrets Manager

## AWS Systems Manager Parameter Store

Armazena parâmetros de configuração hierárquicos (`String`,
`StringList`, `SecureString`), organizados por caminho (ex.:
`/ecommerce/catalog-service/cache-ttl-seconds`). É **gratuito** no tier
Standard (com limite de tamanho/throughput) — a escolha certa para
configuração não sensível.

## AWS Secrets Manager

Especializado em **segredos** (senhas, chaves de API, tokens): suporta
**rotação automática** agendada (ex.: trocar a senha do RDS a cada 30 dias
sem intervenção manual, via uma Lambda de rotação gerenciada pela própria
integração RDS+Secrets Manager) e é cobrado por segredo armazenado + por
chamada de API — mais caro que o Parameter Store, mas com recursos que ele
não tem.

**Regra prática para o exame**: dado sensível e/ou que precisa rotacionar
automaticamente → Secrets Manager. Configuração não sensível → Parameter
Store (mais barato).

## Uso no projeto

Os dois aparecem lado a lado no `order-service`, via **Spring Boot Config
Data API**:

```yaml
# order-service/application.yml
spring:
  config:
    import:
      - aws-secretsmanager:/ecommerce/order-service/db-credentials
      - aws-parameterstore:/ecommerce/order-service/
  datasource:
    username: ${username}   # veio do JSON do segredo
    password: ${password}   # veio do JSON do segredo
    url: jdbc:postgresql://...
```

- `aws-secretsmanager:/ecommerce/order-service/db-credentials` busca o
  segredo (um JSON `{"username": "...", "password": "..."}`, criado em
  `infra/localstack/init/init-aws.sh`) e expõe cada chave do JSON como uma
  property Spring — sem nenhum código Java adicional.
- `aws-parameterstore:/ecommerce/order-service/` importa **todos** os
  parâmetros sob esse caminho (ex.: `db-url`) como properties, usando o
  último segmento do nome como chave.

Essa importação acontece **antes** do contexto Spring subir por completo
(bootstrap), então qualquer bean pode usar essas properties normalmente com
`@Value` ou `application.yml`.

O `catalog-service` usa apenas Parameter Store, para o TTL do cache
(`/ecommerce/catalog-service/cache-ttl-seconds`, lido em
`CacheConfig.java`) — não é um segredo, apenas configuração operacional que
se quer poder ajustar sem novo deploy.

## Por que isso importa (e não é só "boa prática")

Sem essa camada, a alternativa comum e ruim é: senha do banco direto no
`application.yml` (commitada no Git!) ou em variável de ambiente passada na
mão em cada ambiente. Ambas falham em auditoria (quem acessou a senha?),
rotação (trocar a senha exige redeploy manual em todo lugar que a usa) e
princípio do menor privilégio (a permissão de ler o segredo é controlada
por IAM, separadamente da permissão de rodar a aplicação).

## Para o exame

- Parameter Store `SecureString` usa KMS para criptografar o valor — mas
  ainda não tem rotação automática nativa como o Secrets Manager.
- Secrets Manager pode rotacionar segredos do RDS/Redshift/DocumentDB
  "out of the box" (a AWS fornece a Lambda de rotação pronta).
- Ambos os serviços são regionais — segredos/parâmetros não replicam entre
  regiões automaticamente (é preciso replicar explicitamente, se precisar
  de DR cross-region).
- Custo: Parameter Store Standard é gratuito; Secrets Manager cobra por
  segredo armazenado por mês + por 10 mil chamadas de API — outro item a
  observar no [Capítulo 16](16-custos-billing-finops.md) se você tiver
  centenas de segredos e chama-los a cada cold start de uma Lambda, por
  exemplo (nesse caso, cachear o segredo em memória reduz custo).

---
**Anterior:** [← 10. SES](10-email-ses.md) | **Próximo:** [12. Observabilidade: CloudWatch →](12-observabilidade-cloudwatch.md)
