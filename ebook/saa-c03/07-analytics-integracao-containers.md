[⬅ Sumário](../../README.md) · Trilha SAA-C03 · S7 de 8

# S7. Analytics, integração de aplicações e containers

Domínio de performance. O [Capítulo 4](../04-computo-ec2-ecs-lambda.md) cobre EC2, ECS/Fargate e Lambda;
o [Capítulo 9](../09-mensageria-sqs-sns.md) cobre SQS e SNS. Aqui estão os serviços de dados e de
integração que completam o conteúdo da prova.

## Ingestão e streaming de dados

| Serviço | Papel | Observação |
|---|---|---|
| **Kinesis Data Streams** | stream em tempo real, com shards; você gerencia consumidores | retenção de 24 h a 365 dias; reprocessável |
| **Kinesis Data Firehose** (hoje Amazon Data Firehose) | entrega o stream a destinos (S3, Redshift, OpenSearch, HTTP) sem escrever consumidor | quase em tempo real; transformação com Lambda |
| **Managed Service for Apache Flink** | processa streams com SQL/Flink | análises em tempo real |
| **Amazon MSK** | Apache Kafka gerenciado | para quem já usa Kafka |
| **DMS** | migra e replica bancos | ver [S8](08-migracao-transferencia-e-custos.md) |

Kinesis Data Streams vs. SQS: o stream mantém a ordem por shard, permite
**vários consumidores** lendo os mesmos dados e reprocessamento; a fila entrega cada
mensagem a um consumidor e a remove depois de processada.

## Data lake e analytics

| Serviço | Papel |
|---|---|
| **S3** | armazenamento do data lake |
| **Glue** | ETL serverless e **Data Catalog** (metadados) |
| **Athena** | SQL serverless direto sobre arquivos no S3; paga por dado varrido; use Parquet/ORC e particionamento para reduzir custo |
| **Redshift** | data warehouse colunar para consultas analíticas pesadas; **Redshift Spectrum** consulta o S3 |
| **EMR** | clusters gerenciados de Hadoop/Spark para big data |
| **Lake Formation** | governança e permissões do data lake |
| **QuickSight** | BI e dashboards |
| **OpenSearch Service** | busca e análise de logs |

Padrões frequentes:

- "SQL ad hoc sobre dados no S3, sem servidor" → **Athena**.
- "Data warehouse com muitas consultas complexas" → **Redshift**.
- "Spark/Hadoop de grande escala" → **EMR**.
- "ETL serverless" → **Glue**.

## Integração de aplicações

| Serviço | Papel | Quando usar |
|---|---|---|
| **SQS** | fila, desacopla produtor e consumidor | absorver picos, processamento assíncrono |
| **SNS** | publicação/assinatura, fan-out | notificar vários destinos |
| **SNS + SQS** | fan-out com filas | um evento processado por vários consumidores independentes |
| **EventBridge** | barramento de eventos com regras, agendamento e integrações SaaS | arquitetura orientada a eventos |
| **Step Functions** | orquestra fluxos com estados, retries e tratamento de erro | workflows de vários passos |
| **API Gateway** | expõe APIs REST, HTTP e WebSocket; autenticação, throttling, cache | porta de entrada de APIs e serverless |
| **AppSync** | GraphQL gerenciado | APIs GraphQL |
| **Amazon MQ** | ActiveMQ/RabbitMQ gerenciado | migrar apps que já usam esses brokers, sem reescrever |

Escolhas clássicas:

- Migrar sem alterar código que usa JMS/AMQP → **Amazon MQ**. Para
  aplicações novas, prefira SQS/SNS.
- Fila **FIFO** quando a ordem e a ausência de duplicatas importam; fila
  **standard** quando se precisa de throughput máximo.
- **DLQ** isola mensagens que falham repetidamente
  (já usada no [Capítulo 9](../09-mensageria-sqs-sns.md)).

### API Gateway

- Tipos: **REST** (mais recursos: cache, chaves de API, planos de uso), **HTTP**
  (mais simples e barato) e **WebSocket**.
- Autenticação: IAM, Cognito user pools, **Lambda authorizer**.
- **Throttling** e cache protegem o backend.
- Integra com Lambda, ALB (via VPC Link), serviços AWS e endpoints HTTP.

## Containers

| Serviço | Papel |
|---|---|
| **ECS** | orquestrador de containers da AWS, simples e integrado |
| **EKS** | Kubernetes gerenciado; para portabilidade e ecossistema Kubernetes |
| **Fargate** | execução serverless de containers (ECS ou EKS); sem gerenciar instâncias |
| **ECR** | registro de imagens de containers, com varredura de vulnerabilidades |
| **App Runner** | publica aplicações web de container ou código com o mínimo de operação |

ECS ou EKS na EC2 dão mais controle sobre as instâncias; Fargate elimina a
gestão de servidores. Escolha EKS quando houver requisito de Kubernetes ou
portabilidade multi-cloud; caso contrário, ECS é mais simples.

## Compute adicional que a prova cita

- **Lambda**: limite de 15 minutos por execução; escala automática; concorrência
  reservada/provisionada. Gatilhos: S3, SQS, API Gateway, EventBridge e outros.
- **Elastic Beanstalk**: PaaS que provisiona EC2, ELB e ASG a partir do código.
- **Batch**: executa jobs em lote com compute gerenciado.
- **Outposts**: infraestrutura AWS dentro do seu data center.

## Infraestrutura como código

**CloudFormation** declara a infraestrutura em templates (stacks), com
**StackSets** para várias contas/regiões. Garante ambientes repetíveis e é
base do DR e dos laboratórios.

## Para o exame

- **SQL sem servidor sobre S3** → Athena. **Streaming com vários consumidores** →
  Kinesis Data Streams. **Entrega do stream ao S3 sem código** → Firehose.
- **Orquestrar vários passos com retry** → Step Functions.
- **Eventos entre serviços e agendamento** → EventBridge.
- **Fan-out** → SNS (+ SQS).
- **Reduzir custo do Athena** → Parquet, compressão e particionamento.

---
**Anterior:** [← S6. Conectividade híbrida](06-conectividade-hibrida.md) | **Próximo:** [S8. Migração, transferência e custos →](08-migracao-transferencia-e-custos.md)
