# E-commerce AWS + Spring Cloud AWS — estudo para o AWS Certified Solutions Architect – Associate

Projeto de estudo prático para a certificação **AWS Certified Solutions
Architect – Associate (SAA-C03)**: um e-commerce cloud native, dividido em
três microsserviços Spring Boot, que consome os principais serviços AWS
através do **Spring Cloud AWS**, com resiliência via **Resilience4j** e um
ebook completo explicando cada decisão.

Não é preciso instalar Java, Maven ou AWS CLI — **apenas Docker**:

```bash
docker compose up --build
```

| Serviço | Swagger UI | Serviços AWS |
|---|---|---|
| `catalog-service` | http://localhost:8081/swagger-ui.html | DynamoDB, S3, ElastiCache (Redis) |
| `order-service` | http://localhost:8082/swagger-ui.html | RDS (PostgreSQL), Secrets Manager, Parameter Store, SNS |
| `notification-service` | http://localhost:8083/swagger-ui.html | SQS, SES, CloudWatch |

Todos os três usam **IAM** (conceitualmente), rodam dentro de uma **VPC**
(em produção) e aplicam **Resilience4j** (circuit breaker, retry,
bulkhead) nas chamadas entre si e com a AWS.

## 📖 Sumário do ebook

O ebook completo está em [`ebook/`](ebook/), em capítulos Markdown
navegáveis (cada um tem link para o anterior/próximo no rodapé). Comece pelo
capítulo 0:

0. [Introdução e como usar este ebook](ebook/00-introducao-e-como-usar.md)
1. [Fundamentos AWS e o AWS Well-Architected Framework](ebook/01-fundamentos-aws-well-architected.md)
2. [IAM, Segurança e Amazon Cognito](ebook/02-iam-seguranca-cognito.md)
3. [Redes: Amazon VPC](ebook/03-redes-vpc.md)
4. [Computação: EC2, ECS/Fargate e Lambda](ebook/04-computo-ec2-ecs-lambda.md)
5. [Armazenamento: Amazon S3](ebook/05-armazenamento-s3.md)
6. [Banco de Dados Relacional: Amazon RDS](ebook/06-banco-relacional-rds.md)
7. [Banco de Dados NoSQL: Amazon DynamoDB](ebook/07-banco-nosql-dynamodb.md)
8. [Cache: Amazon ElastiCache (Redis)](ebook/08-cache-elasticache.md)
9. [Mensageria: Amazon SQS e SNS](ebook/09-mensageria-sqs-sns.md)
10. [E-mail Transacional: Amazon SES](ebook/10-email-ses.md)
11. [Configuração e Segredos: Parameter Store e Secrets Manager](ebook/11-config-secrets-parameter-store.md)
12. [Observabilidade: Amazon CloudWatch](ebook/12-observabilidade-cloudwatch.md)
13. [Arquitetura da Solução (diagramas)](ebook/13-arquitetura-da-solucao.md)
14. [Cloud Native e Boas Práticas AWS](ebook/14-cloud-native-boas-praticas.md)
15. [Resiliência com Resilience4j](ebook/15-resiliencia-resilience4j.md)
16. [Custos, Billing e FinOps](ebook/16-custos-billing-finops.md)
17. [Guia de Uso e Swagger/OpenAPI](ebook/17-guia-de-uso-swagger.md)
18. [Referências Oficiais AWS](ebook/18-referencias.md)

## 🗺️ Diagramas (draw.io)

Em [`ebook/diagrams/`](ebook/diagrams/) — abra os arquivos `.drawio` em
[app.diagrams.net](https://app.diagrams.net):

- [`01-arquitetura-geral.drawio`](ebook/diagrams/01-arquitetura-geral.drawio) — visão completa dos 3 serviços e dos serviços AWS
- [`02-fluxo-criacao-pedido.drawio`](ebook/diagrams/02-fluxo-criacao-pedido.drawio) — passo a passo da criação de um pedido
- [`03-rede-vpc.drawio`](ebook/diagrams/03-rede-vpc.drawio) — topologia de VPC/subnets/AZs de referência

## 🏗️ Estrutura do repositório

```
.
├── docker-compose.yml          # infra local: LocalStack, Postgres, Redis + os 3 serviços
├── infra/localstack/init/      # provisiona bucket S3, tabela DynamoDB, filas/tópicos, segredos e parâmetros
├── services/
│   ├── common/                 # exceções, tratamento de erro e logging de resiliência compartilhados
│   ├── catalog-service/        # DynamoDB + S3 + Redis
│   ├── order-service/          # RDS + Secrets Manager + Parameter Store + SNS
│   └── notification-service/   # SQS + SES + CloudWatch
└── ebook/                      # este ebook, em Markdown, + diagramas draw.io
```

## 🚀 Quickstart

```bash
git clone https://github.com/camarguinho/aws-learning.git
cd aws-learning
docker compose up --build
```

Depois, siga o [Capítulo 17 — Guia de Uso e Swagger](ebook/17-guia-de-uso-swagger.md)
para um passo a passo completo do fluxo de ponta a ponta (criar produto →
criar pedido → ver notificação → testar resiliência).

## 🧪 Rodando os testes

```bash
docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-21 mvn test
```

(ou, se você já tem Maven/Java localmente, simplesmente `mvn test` na raiz.)

## Licença

[MIT](LICENSE)
