[⬅ Sumário](../README.md) · Capítulo 8 de 18

# 8. Cache: Amazon ElastiCache (Redis)

## O que é

ElastiCache é um serviço gerenciado de cache em memória, com suporte a dois
engines: **Redis** (estruturas de dados ricas, replicação, persistência
opcional, pub/sub) e **Memcached** (mais simples, multi-thread, sem
persistência). O projeto usa Redis.

Por que cache importa no exame e na prática: reduzir latência (memória vs.
disco) e reduzir carga/custo no banco de dados primário, absorvendo leituras
repetidas.

## Padrão usado no projeto: Cache-Aside

`ProductService.java` implementa o padrão **cache-aside** (também chamado
lazy loading) via anotações declarativas do Spring:

```java
@Cacheable(cacheNames = "products", key = "#productId")
public ProductResponse findById(String productId) { ... }

@CacheEvict(cacheNames = "products", key = "#productId")
public ProductResponse update(String productId, ProductRequest request) { ... }
```

Fluxo:

1. Uma leitura primeiro consulta o Redis (`@Cacheable`);
2. Em caso de **cache miss**, o método real executa, busca no DynamoDB, e o
   resultado é automaticamente gravado no cache pelo Spring;
3. Qualquer escrita (`update`, `delete`, geração de URL de upload de
   imagem) invalida a entrada correspondente (`@CacheEvict`), evitando que
   o cliente veja dado desatualizado além do TTL.

O TTL (time-to-live) do cache é configurado em
`CacheConfig.java`, lido de uma property que, em uma implantação real,
viria do **SSM Parameter Store**
(`/ecommerce/catalog-service/cache-ttl-seconds` — ver
[Capítulo 11](11-config-secrets-parameter-store.md)), permitindo ajustar o
tempo de vida do cache sem novo deploy.

## Por que Redis e não Memcached aqui

Ainda que o caso de uso (guardar um objeto serializado por chave) seja
simples o bastante para qualquer um dos dois, Redis foi escolhido porque:

- Suporta persistência opcional (RDB/AOF) — útil se o cache também servir
  de estrutura auxiliar (ex.: contadores, filas simples) no futuro;
- Tem replicação nativa (Multi-AZ com failover automático no
  ElastiCache for Redis) — cobrado no exame como vantagem sobre Memcached;
- É o padrão de mercado hoje para a maioria dos novos projetos.

## Outros padrões de cache (para o exame)

| Padrão | Como funciona | Trade-off |
|---|---|---|
| Cache-aside (usado aqui) | App consulta o cache; em miss, busca a fonte e popula o cache | Simples; pode servir dado obsoleto até o TTL expirar |
| Write-through | Toda escrita vai para o cache e a fonte, sincronamente | Cache sempre atualizado; escrita mais lenta |
| Write-behind (write-back) | Escrita vai só ao cache, que persiste depois de forma assíncrona | Escrita rápida; risco de perda de dado se o cache cair antes de persistir |

## Para o exame

- ElastiCache **não é um banco de dados durável** — é volátil por padrão
  (mesmo com persistência Redis habilitada, não tem os mesmos SLAs de
  durabilidade do RDS/DynamoDB). Nunca trate o cache como fonte da
  verdade.
- Cluster Mode (Redis) permite particionamento (sharding) horizontal para
  datasets maiores que a memória de um único nó.
- ElastiCache é cobrado por **nó provisionado por hora**, 24/7, independente
  do tráfego — diferente do DynamoDB/S3, que têm componentes sob demanda.
  Isso o torna um dos itens a vigiar de perto no
  [Capítulo 16](16-custos-billing-finops.md): um cache superdimensionado
  "esquecido" rodando custa dinheiro mesmo sem uso.
- Amazon MemoryDB for Redis é a opção **durável** compatível com Redis
  (multi-AZ com log de transação persistente) quando você precisa de Redis
  como banco primário, não apenas cache — diferente do ElastiCache.

---
**Anterior:** [← 7. DynamoDB](07-banco-nosql-dynamodb.md) | **Próximo:** [9. Mensageria: SQS e SNS →](09-mensageria-sqs-sns.md)
