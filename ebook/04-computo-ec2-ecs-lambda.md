[⬅ Sumário](../README.md) · Capítulo 4 de 18

# 4. Computação: EC2, ECS/Fargate e Lambda

O exame cobra três formas principais de rodar código na AWS. Nosso projeto
roda localmente em containers Docker "puros" (para não exigir conta AWS),
mas as três opções abaixo mapeiam diretamente para como você o levaria a
produção.

## Amazon EC2

Máquinas virtuais sob demanda. Você escolhe a **AMI** (imagem, ex.: Amazon
Linux 2023), o **tipo de instância** (família de CPU/RAM/rede, ex.:
`t3.medium`, `m6i.large`) e é responsável por SO, patches e
runtime (JVM, neste caso).

Modelos de compra (peso grande na prova):

| Modelo | Quando usar | Desconto típico |
|---|---|---|
| On-Demand | carga imprevisível, curto prazo | referência (100%) |
| Reserved/Savings Plans | carga estável e previsível, 1-3 anos | até ~72% |
| Spot | tolerante a interrupção (batch, CI, filas) | até ~90% |

## Amazon ECS (com Fargate)

ECS orquestra containers. Com o launch type **Fargate**, você não gerencia
EC2 nenhuma — só define CPU/memória por task e a AWS provisiona a
infraestrutura por trás. É o alvo natural dos nossos três microsserviços:
cada `Dockerfile` (`services/*/Dockerfile`) vira uma **Task Definition**, e
cada serviço Spring Boot vira um **ECS Service** com Auto Scaling baseado em
CPU/memória ou fila (ex.: escalar `notification-service` pelo tamanho da
fila `order-events-queue` via CloudWatch Alarm).

Por que Fargate e não EC2 puro aqui? Menor operação (sem patch de SO), bom
encaixe com cargas com pico variável (Black Friday do e-commerce fictício)
e cobrança por vCPU/memória realmente alocada por task.

## AWS Lambda

Execução **serverless** por evento, cobrada por invocação + tempo de
execução (arredondado ao ms), com **cold start** possível. Não usamos
Lambda nos três serviços principais (eles são APIs long-running, mais
adequadas a ECS), mas é o serviço certo para tarefas pontuais e orientadas
a evento deste mesmo case — por exemplo:

- Uma Lambda assinante do tópico SNS `order-notifications-topic` que roda
  uma checagem de fraude simples, em paralelo ao `notification-service`
  (fan-out do SNS para múltiplos consumidores, sem tocar no código do
  `order-service`).
- Uma Lambda agendada (EventBridge Schedule) que gera um relatório diário
  de pedidos a partir do RDS.

## Como isso mapeia para o `docker-compose.yml`

Cada Dockerfile (`services/catalog-service/Dockerfile`,
`services/order-service/Dockerfile`,
`services/notification-service/Dockerfile`) faz um build multi-stage: a
primeira etapa compila com Maven, a segunda copia só o JAR final para uma
imagem `eclipse-temurin:21-jre-alpine`, minimizando o tamanho da imagem —
a mesma imagem que você enviaria ao **Amazon ECR** e apontaria em uma Task
Definition do ECS Fargate.

## Para o exame

- Fargate remove a necessidade de gerenciar o cluster EC2 subjacente, mas
  ainda cobra por vCPU/GB-hora alocado à task (não é "grátis até rodar" —
  isso é a Lambda, dentro do free tier de invocações/tempo).
- Auto Scaling de ECS pode reagir a métricas do **Application Auto
  Scaling** (CPU, memória, ou uma métrica customizada do CloudWatch, como
  profundidade de fila SQS).
- Lambda tem limite de tempo de execução (15 min) — não é adequado para
  processos longos ou para servir uma API HTTP síncrona de alto volume
  sustentado (aí entra ECS/EC2 atrás de um ALB).

---
**Anterior:** [← 3. Redes: VPC](03-redes-vpc.md) | **Próximo:** [5. Armazenamento: Amazon S3 →](05-armazenamento-s3.md)
