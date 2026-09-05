[⬅ Sumário](../README.md) · Capítulo 7 de 18

# 7. Banco de Dados NoSQL: Amazon DynamoDB

## O que é

DynamoDB é um banco **chave-valor / documento**, totalmente gerenciado e
serverless: você não provisiona servidores, e ele escala horizontalmente de
forma transparente (particionamento automático). Latência de leitura/escrita
de milissegundos de um dígito, independente do tamanho da tabela.

Conceitos centrais para o exame:

- **Partition Key (chave de partição)**: determina em qual partição física
  o item fica. Deve ter **alta cardinalidade** para distribuir a carga
  (evitar "hot partitions").
- **Sort Key (chave de ordenação, opcional)**: junto com a partition key,
  forma uma chave composta, permitindo múltiplos itens por partição
  ordenados.
- **Capacidade**: modo **On-Demand** (paga por requisição, escala
  automático — usado no projeto) ou **Provisioned** (você define RCU/WCU
  fixas, mais barato em carga previsível e estável).
- **GSI (Global Secondary Index)** / **LSI (Local Secondary Index)**:
  permitem consultar por atributos além da chave primária.
- **DynamoDB Streams**: captura de mudanças (CDC) na tabela, consumível por
  Lambda — útil para reagir a alterações de estoque, por exemplo.

## Modelagem no projeto

A tabela `Product` (criada em
`infra/localstack/init/init-aws.sh`) tem **apenas partition key**
(`productId`), sem sort key — porque o único padrão de acesso do catálogo é
"buscar produto por id" (`ProductRepository.findById`). Não há necessidade
de consultas por faixa nem de GSIs neste escopo do case.

```java
@DynamoDbBean
public class Product implements Serializable {
    @DynamoDbPartitionKey
    public String getProductId() { ... }

    @DynamoDbVersionAttribute
    public Long getVersion() { ... }
}
```

O atributo `version`, anotado com `@DynamoDbVersionAttribute`, ativa
**optimistic locking**: o SDK Enhanced Client rejeita uma escrita se a
versão lida pelo cliente já foi sobrescrita por outra escrita concorrente —
evita "lost update" sem precisar de um lock pessimista, que não existiria
nativamente aqui.

### Spring Cloud AWS: `DynamoDbTemplate`

Em vez de manipular diretamente `DynamoDbEnhancedClient` +
`DynamoDbTable<Product>`, `ProductRepository.java` usa o
`DynamoDbTemplate` do Spring Cloud AWS:

```java
public Optional<Product> findById(String productId) {
    Key key = Key.builder().partitionValue(productId).build();
    return Optional.ofNullable(dynamoDbTemplate.load(key, Product.class));
}
```

Essa é a mesma proposta de valor de um `JpaRepository`: menos boilerplate,
mais foco na regra de negócio.

## Modelagem de dados: pense no acesso, não na normalização

A maior diferença de mentalidade entre SQL e DynamoDB, muito cobrada na
prova: em um banco relacional você modela entidades e normaliza; no
DynamoDB você modela **em torno dos padrões de acesso** conhecidos
antecipadamente, e frequentemente desnormaliza (duplicando dados) para
resolver uma consulta com uma única operação, sem JOIN (o DynamoDB não tem
JOIN). É por isso que o catálogo (acesso simples por id) foi para DynamoDB
e os pedidos (consultas relacionais variadas, transação multi-tabela) foram
para RDS — ver a comparação completa no
[Capítulo 6](06-banco-relacional-rds.md).

## Para o exame

- DynamoDB oferece **consistência eventual** por padrão nas leituras (mais
  barata) ou **consistência forte** sob demanda (`ConsistentRead=true`,
  não usada aqui, com o dobro do custo de RCU).
- Transações no DynamoDB existem (`TransactWriteItems`), mas são limitadas
  a 100 itens/4MB por transação e custam o dobro de capacidade — não
  substituem um banco relacional para transações complexas.
- DAX (DynamoDB Accelerator) é um cache **totalmente gerenciado e
  compatível com a API do DynamoDB** para cargas de leitura extremamente
  intensas — diferente do Redis/ElastiCache genérico que usamos no
  catálogo (ver [Capítulo 8](08-cache-elasticache.md)), que fica na
  camada da aplicação, não é exclusivo do DynamoDB, e é reutilizável para
  qualquer tipo de dado.
- Billing on-demand cobra por milhão de requisições de leitura/escrita; o
  modo provisioned cobra por RCU/WCU-hora reservada, independente do uso.

---
**Anterior:** [← 6. RDS](06-banco-relacional-rds.md) | **Próximo:** [8. Cache: Amazon ElastiCache →](08-cache-elasticache.md)
