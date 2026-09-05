[⬅ Sumário](../README.md) · Capítulo 0 de 18

# 0. Introdução e como usar este ebook

## Objetivo

Este material foi criado para te preparar, na prática, para a certificação
**AWS Certified Solutions Architect – Associate (SAA-C03)** e, ao mesmo
tempo, ensinar como consumir os principais serviços AWS a partir de uma
aplicação Java real usando **Spring Cloud AWS**.

Em vez de decorar definições soltas de cada serviço, você vai:

1. Ler a explicação conceitual do serviço (o que o exame cobra);
2. Ver exatamente onde e como ele é usado no código deste repositório;
3. Rodar tudo localmente via Docker, sem instalar nada além do Docker;
4. Entender o modelo de cobrança do serviço e como controlar custo.

## O case: e-commerce cloud native

Para explorar os serviços de forma coesa (não isolada), construímos um
e-commerce simplificado, dividido em três microsserviços Spring Boot:

| Serviço | Responsabilidade | Serviços AWS principais |
|---|---|---|
| `catalog-service` | Catálogo de produtos | DynamoDB, S3, ElastiCache (Redis) |
| `order-service` | Criação e consulta de pedidos | RDS (PostgreSQL), Secrets Manager, Parameter Store, SNS |
| `notification-service` | Notificação assíncrona do pedido | SQS, SES, CloudWatch |

Todos os três se apoiam em **IAM** (identidade/permissão), rodam dentro de
uma **VPC** (rede) e usam **Resilience4j** para resiliência entre chamadas.

Isso cobre, de forma prática, os serviços mais cobrados no exame SAA-C03 e
também os mais comuns no dia a dia de mercado.

## Como rodar o projeto

Pré-requisito único: **Docker** e **Docker Compose**. Nada de Java, Maven ou
AWS CLI precisa estar instalado na sua máquina — tudo roda em containers.

```bash
# na raiz do repositório
docker compose up --build
```

Isso sobe, nesta ordem (por causa dos healthchecks configurados):

1. **LocalStack** — emula S3, DynamoDB, SQS, SNS, Secrets Manager, SSM,
   SES e CloudWatch localmente, na mesma API da AWS real;
2. **PostgreSQL** — emula o Amazon RDS;
3. **Redis** — emula o Amazon ElastiCache;
4. Os três microsserviços Spring Boot, já apontando para os endpoints acima.

Assim que subir, os serviços ficam disponíveis em:

- `catalog-service`: http://localhost:8081/swagger-ui.html
- `order-service`: http://localhost:8082/swagger-ui.html
- `notification-service`: http://localhost:8083/swagger-ui.html

O capítulo [17 - Guia de Uso e Swagger](17-guia-de-uso-swagger.md) mostra um
passo a passo completo de chamadas para testar o fluxo de ponta a ponta.

## Como este ebook está organizado

- **Capítulos 1 a 12**: um serviço AWS por capítulo (o que é, como o exame
  cobra, como usamos via Spring Cloud AWS, boas práticas, cobrança);
- **Capítulo 13**: a arquitetura completa da solução, com diagramas draw.io;
- **Capítulo 14**: princípios cloud native e AWS Well-Architected aplicados
  ao código deste repositório;
- **Capítulo 15**: resiliência com Resilience4j;
- **Capítulo 16**: custos, billing e como não ser surpreendido pela fatura;
- **Capítulo 17**: guia de uso das APIs (Swagger/OpenAPI);
- **Capítulo 18**: referências oficiais AWS para aprofundar cada tema.

Cada capítulo tem navegação **Anterior / Próximo** no rodapé — comece pelo
[Capítulo 1](01-fundamentos-aws-well-architected.md) e siga em sequência na
primeira leitura.

## Convenção de código

Todas as classes Java do projeto têm Javadoc explicando **por que** aquela
decisão de design foi tomada (não apenas o que o código faz). Sempre que um
capítulo menciona uma classe, o caminho completo do arquivo é citado — por
exemplo, `services/catalog-service/.../ProductService.java` — para você abrir
e ler o código com o capítulo ao lado.

---
**Próximo:** [1. Fundamentos AWS e Well-Architected Framework →](01-fundamentos-aws-well-architected.md)
