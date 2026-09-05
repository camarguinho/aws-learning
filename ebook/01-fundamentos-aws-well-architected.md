[⬅ Sumário](../README.md) · Capítulo 1 de 18

# 1. Fundamentos AWS e o AWS Well-Architected Framework

## Regiões, Availability Zones e Edge Locations

- **Região (Region)**: uma área geográfica independente (ex.: `us-east-1`,
  `sa-east-1`). Cada região é isolada das demais — dados não replicam entre
  regiões a menos que você configure isso explicitamente. É a unidade
  básica de residência de dados e de latência para o usuário final.
- **Availability Zone (AZ)**: um ou mais datacenters fisicamente separados
  dentro de uma região, com energia/rede independentes, mas interligados
  por rede de baixíssima latência. Toda região tem no mínimo 3 AZs (SAA-C03
  cobra bastante esse número mínimo).
- **Edge Location**: pontos de presença usados por serviços como CloudFront
  e Route 53 para entregar conteúdo próximo do usuário final.

No nosso projeto, o `docker-compose.yml` fixa a região `us-east-1` — em uma
implantação real, você replicaria os microsserviços e o RDS entre pelo menos
duas AZs dessa região (ver diagrama de rede no
[Capítulo 13](13-arquitetura-da-solucao.md)).

## Modelo de responsabilidade compartilhada

A AWS é responsável pela segurança **da** nuvem (hardware, rede global,
virtualização, durabilidade física dos serviços gerenciados). Você é
responsável pela segurança **na** nuvem: configuração de IAM, criptografia
de dados, patch do seu código/SO (quando aplicável), grupos de segurança,
e classificação dos seus dados.

Isso é diretamente visível no projeto: a AWS garante a durabilidade do S3 e
do DynamoDB; nós garantimos que o bucket de imagens não seja público, que
o Security Group do RDS só aceite conexão dos microsserviços, e que os
segredos do banco fiquem no Secrets Manager em vez de hardcoded.

## AWS Well-Architected Framework — os 6 pilares

O exame SAA-C03 é fortemente ancorado neste framework. Os 6 pilares:

1. **Excelência Operacional** — automatizar operações, aprender com falhas.
   No projeto: Actuator + logs estruturados + métricas no CloudWatch.
2. **Segurança** — aplicar defesa em profundidade, princípio do menor
   privilégio. No projeto: IAM roles por serviço, Secrets Manager, buckets
   privados com URLs pré-assinadas.
3. **Confiabilidade** — recuperação automática de falhas, escalar
   horizontalmente. No projeto: circuit breaker/retry do Resilience4j, DLQ
   no SQS, Multi-AZ no RDS (produção).
4. **Eficiência de Performance** — usar o recurso certo para o padrão de
   acesso certo. No projeto: DynamoDB para leitura por chave a baixa
   latência, cache Redis para reduzir round-trips.
5. **Otimização de Custos** — pagar só pelo que se usa, monitorar gastos.
   Detalhado inteiramente no [Capítulo 16](16-custos-billing-finops.md).
6. **Sustentabilidade** — minimizar a pegada de carbono maximizando a
   utilização dos recursos provisionados (menos over-provisioning).

Cada capítulo de serviço deste ebook volta a esses pilares ao justificar uma
decisão de arquitetura — e o [Capítulo 14](14-cloud-native-boas-praticas.md)
consolida tudo isso em um checklist de boas práticas cloud native.

## Categorias de serviço que você vai encontrar

| Categoria | Serviços usados no projeto | Capítulo |
|---|---|---|
| Computo | (conceitual: EC2, ECS/Fargate, Lambda) | [4](04-computo-ec2-ecs-lambda.md) |
| Armazenamento | S3 | [5](05-armazenamento-s3.md) |
| Banco relacional | RDS | [6](06-banco-relacional-rds.md) |
| Banco NoSQL | DynamoDB | [7](07-banco-nosql-dynamodb.md) |
| Cache | ElastiCache | [8](08-cache-elasticache.md) |
| Mensageria | SQS, SNS | [9](09-mensageria-sqs-sns.md) |
| Comunicação | SES | [10](10-email-ses.md) |
| Configuração/Segredos | Secrets Manager, Parameter Store | [11](11-config-secrets-parameter-store.md) |
| Observabilidade | CloudWatch | [12](12-observabilidade-cloudwatch.md) |
| Identidade | IAM, Cognito | [2](02-iam-seguranca-cognito.md) |
| Rede | VPC | [3](03-redes-vpc.md) |

## Spring Cloud AWS: por que ele importa

O SDK Java da AWS (`software.amazon.awssdk`) é de baixo nível: você mesmo
monta clientes, credenciais, serialização, tratamento de exceção. O
**Spring Cloud AWS** é uma camada de integração que:

- Autoconfigura os clientes (`S3Client`, `DynamoDbEnhancedClient`,
  `SqsAsyncClient`, etc.) a partir de `application.yml`;
- Expõe abstrações de alto nível já conhecidas do ecossistema Spring
  (`S3Template`, `DynamoDbTemplate`, `SnsTemplate`, `@SqsListener`,
  `JavaMailSender` para SES);
- Integra o **Config Data API** do Spring Boot com Secrets Manager e
  Parameter Store (`spring.config.import=aws-secretsmanager:...`).

Você vai ver essa camada em ação em praticamente todo trecho de código deste
projeto — é o "Spring Data" da AWS.

---
**Anterior:** [← 0. Introdução](00-introducao-e-como-usar.md) | **Próximo:** [2. IAM, Segurança e Cognito →](02-iam-seguranca-cognito.md)
