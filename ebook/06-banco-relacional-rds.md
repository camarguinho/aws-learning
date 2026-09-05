[⬅ Sumário](../README.md) · Capítulo 6 de 18

# 6. Banco de Dados Relacional: Amazon RDS

## O que é

RDS é um serviço **gerenciado** de banco relacional (a AWS cuida de patch
de SO/engine, backup automático, failover) para engines como PostgreSQL,
MySQL, MariaDB, SQL Server, Oracle e o Aurora (compatível com
MySQL/PostgreSQL, com armazenamento distribuído próprio da AWS).

Conceitos centrais para o exame:

- **Multi-AZ**: uma réplica **síncrona** em standby em outra AZ, promovida
  automaticamente em caso de falha da primária. É para **alta
  disponibilidade**, não para performance de leitura (a standby não aceita
  tráfego de leitura).
- **Read Replicas**: réplicas **assíncronas**, que podem estar na mesma
  região ou cross-region, usadas para **escalar leitura** (relatórios,
  dashboards) sem sobrecarregar a instância primária.
- **Backup automático** (retenção configurável) + **snapshots manuais**
  para pontos de restauração de longo prazo.
- **RDS Proxy**: pool de conexões gerenciado, útil com Lambda/muitas
  instâncias efêmeras para não esgotar as conexões do banco.

## Por que RDS (e não DynamoDB) para pedidos

O `order-service` usa PostgreSQL via RDS, enquanto o catálogo usa DynamoDB.
A escolha não é arbitrária — é o exercício central do capítulo
[14 - Cloud Native e Boas Práticas](14-cloud-native-boas-praticas.md):

- Um pedido tem **múltiplas tabelas relacionadas** (`orders` e
  `order_items`, ver `Order.java`/`OrderItem.java`) que precisam ser
  gravadas **atomicamente** — `OrderService.create()` está anotado com
  `@Transactional`, garantindo tudo-ou-nada;
- Pedidos são consultados de formas variadas (por cliente, por período, em
  relatórios) — um modelo relacional com JOINs é mais natural aqui do que
  um modelo de acesso por chave única;
- Já o catálogo é lido quase sempre **por identificador direto do
  produto**, o padrão de acesso onde o DynamoDB brilha (ver
  [Capítulo 7](07-banco-nosql-dynamodb.md)).

## Como o projeto se conecta ao RDS

O `order-service` **nunca** tem a senha do banco em `application.yml` em
texto puro. A configuração:

```yaml
spring:
  config:
    import:
      - aws-secretsmanager:/ecommerce/order-service/db-credentials
      - aws-parameterstore:/ecommerce/order-service/
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/orders
    username: ${username}
    password: ${password}
```

`username`/`password` vêm do JSON armazenado no **Secrets Manager**
(capítulo [11](11-config-secrets-parameter-store.md) detalha isso). Em
produção, você apontaria `DB_HOST` para o endpoint do RDS e ligaria
**rotação automática de credenciais** no Secrets Manager — a aplicação nem
precisa saber que a senha mudou, porque ela é lida a cada bootstrap.

Localmente, o container `postgres` do `docker-compose.yml` faz o papel do
RDS; a URL/porta são as mesmas, só o host muda.

## Boas práticas aplicadas (e o que falta para produção)

- Usamos `ddl-auto: update` do Hibernate **só para fins de estudo** — em
  produção, use uma ferramenta de migração versionada (Flyway/Liquibase),
  nunca deixe o ORM alterar o schema automaticamente.
- O RDS deve ficar em **subnets privadas de dados** (ver
  [Capítulo 3](03-redes-vpc.md)), sem rota para a internet.
- Habilite **criptografia em repouso** (KMS) na criação da instância —
  não é possível ativar depois sem recriar o banco, pegadinha clássica de
  prova.

## Para o exame

- Multi-AZ = alta disponibilidade (failover); Read Replica = escala de
  leitura. Não confundir os dois.
- RDS cobra por: tempo de instância provisionada (por segundo, com mínimo
  de 10 min), armazenamento provisionado (GB-mês), IOPS extras (se
  provisionados), backup além da retenção gratuita, e transferência de
  dados de saída.
- Aurora Serverless v2 escala capacidade automaticamente conforme carga —
  útil para cargas variáveis, sem precisar dimensionar uma instância fixa.
- Backup automático tem retenção de até 35 dias; snapshots manuais não
  expiram e continuam sendo cobrados até serem apagados.

---
**Anterior:** [← 5. Armazenamento S3](05-armazenamento-s3.md) | **Próximo:** [7. Banco NoSQL: Amazon DynamoDB →](07-banco-nosql-dynamodb.md)
