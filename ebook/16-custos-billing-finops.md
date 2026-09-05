[⬅ Sumário](../README.md) · Capítulo 16 de 18

# 16. Custos, Billing e FinOps

Este capítulo cobre, serviço por serviço, **como você paga**, uma
**estimativa de custo mensal** para este case rodando em baixa escala, e
**como configurar alarmes** para nunca ser surpreendido pela fatura.

## 16.1 Modelo de cobrança por serviço

| Serviço | Como é cobrado | O que mais pesa |
|---|---|---|
| **ECS Fargate** (compute dos 3 serviços) | vCPU-hora + GB de memória-hora, por task rodando | Manter tasks superdimensionadas ou muitas réplicas ociosas |
| **Application Load Balancer** | Por hora + LCU (Load Balancer Capacity Unit, baseada em conexões/dados/regras) | Tráfego alto ou muitas regras de roteamento |
| **NAT Gateway** | Por hora **+ por GB processado** | Tráfego de saída alto (ex.: chamadas externas via subnet privada) |
| **RDS (PostgreSQL)** | Instância-hora + armazenamento GB-mês + IOPS extras (se provisionados) + backup além do incluso | Instância superdimensionada rodando 24/7, Multi-AZ dobra o custo de instância |
| **DynamoDB (on-demand)** | Por milhão de leituras/escritas + GB armazenado | Scans completos frequentes (`scanAll`, usado no `findAll` do catálogo) em tabelas grandes |
| **ElastiCache (Redis)** | Por nó-hora, 24/7, independente do uso | Nó superdimensionado ou "esquecido" ligado |
| **S3** | GB armazenado/mês + requisições (GET/PUT) + transferência de saída | Muitos objetos pequenos com alto volume de GET, ou dados nunca movidos para classes mais baratas |
| **SQS** | Por milhão de requisições (após faixa gratuita) | Polling agressivo (short polling) em vez de long polling |
| **SNS** | Por milhão de publicações + por notificação entregue | Fan-out para muitos assinantes de alto volume |
| **SES** | Por e-mail enviado (com faixa gratuita se enviado de dentro da AWS) | Volume alto sem revisão de bounce/complaint (pode gerar suspensão, não só custo) |
| **Secrets Manager** | Por segredo armazenado/mês + por 10 mil chamadas de API | Muitos segredos, ou aplicações que buscam o segredo a cada requisição em vez de cachear |
| **SSM Parameter Store (Standard)** | **Gratuito** | — (Advanced tier é pago, não usado aqui) |
| **CloudWatch** | Métricas custom (por métrica-mês) + Logs (ingestão + armazenamento) + Alarmes (por alarme-mês) | Métricas com dimensões de alta cardinalidade, logs nunca expirados |
| **Data Transfer** | Saída para a internet é paga (por GB, com faixa gratuita mensal); entre serviços na mesma AZ costuma ser gratuita | Tráfego entre AZs diferentes tem custo; entre regiões, mais ainda |

## 16.2 Estimativa de custo mensal (carga baixa, 1 ambiente)

Estimativa de referência para uma implantação pequena e contínua na AWS
real (valores aproximados em USD, região `us-east-1`, sujeitos a mudança —
sempre confira a [AWS Pricing Calculator](https://calculator.aws) para um
número atual):

| Item | Configuração de referência | Custo mensal aproximado |
|---|---|---|
| ECS Fargate (3 serviços x 1 task, 0.25 vCPU / 0.5 GB) | 24/7 | ~US$ 25–35 |
| Application Load Balancer | 1 ALB, tráfego baixo | ~US$ 20 |
| NAT Gateway | 1 gateway, tráfego baixo | ~US$ 33 (fixo) + tráfego |
| RDS db.t4g.micro (Single-AZ) | 20 GB armazenamento | ~US$ 15–20 |
| ElastiCache cache.t4g.micro (1 nó) | — | ~US$ 12 |
| DynamoDB on-demand | Catálogo pequeno, poucas requisições | < US$ 1 |
| S3 | Poucas dezenas de imagens | < US$ 1 |
| SQS + SNS | Baixo volume | < US$ 1 (dentro da faixa gratuita) |
| SES | Poucas centenas de e-mails | < US$ 1 |
| Secrets Manager | 1 segredo | ~US$ 0,40 |
| CloudWatch (métricas + logs) | Uso moderado | ~US$ 3–5 |
| **Total aproximado** | | **~US$ 110–130/mês** |

O maior peso, de longe, é **infraestrutura sempre ligada** (Fargate, ALB,
NAT Gateway, RDS, ElastiCache) — não os serviços "sob demanda" (DynamoDB,
S3, SQS/SNS, SES), que custam centavos nessa escala. Essa é a lição
central de FinOps para este case: **componentes provisionados 24/7 dominam
a fatura em baixa escala; componentes serverless dominam só em alta
escala**.

## 16.3 Como calcular o custo da sua própria carga

1. Estime volume mensal por serviço (requisições ao catálogo, pedidos
   criados, e-mails enviados, GB de imagens armazenadas);
2. Jogue esses números na
   [AWS Pricing Calculator](https://calculator.aws) — ela tem
   calculadoras dedicadas para RDS, DynamoDB, S3, SQS, SNS, SES, etc.;
3. Use o **AWS Cost Explorer** (após alguns dias de uso real) para
   comparar a estimativa com o gasto real por serviço e por tag.

## 16.4 Como reduzir custo (aplicado a este projeto)

- **NAT Gateway**: usar **VPC Gateway Endpoints** (gratuitos) para S3 e
  DynamoDB elimina o tráfego desses serviços passando pelo NAT — já
  representado no diagrama de rede
  ([Capítulo 3](03-redes-vpc.md)). Interface Endpoints para SQS/SNS/Secrets
  Manager/SSM têm custo próprio, mas ainda podem sair mais barato que NAT
  em alto volume.
- **RDS**: usar Aurora Serverless v2 se a carga for muito variável (evita
  pagar por uma instância fixa dimensionada para o pico); ativar Multi-AZ
  **só em produção** (dobra o custo de instância) — ambientes de
  dev/staging não precisam.
- **DynamoDB**: evitar `scanAll` em tabelas grandes (usado hoje no
  `findAll` do catálogo, aceitável na escala do case, mas o primeiro
  candidato a otimizar/paginar se o catálogo crescer) — um Scan lê (e
  cobra) a tabela inteira.
- **ElastiCache**: dimensionar o menor nó que atenda a taxa de miss
  aceitável; um cache muito pequeno vira custo sem benefício (miss rate
  alto) — monitore a métrica `CacheHitRate`.
- **S3**: configurar **Lifecycle Rules** para mover imagens antigas/pouco
  acessadas para S3 Standard-IA ou Glacier.
- **CloudWatch**: definir retenção de logs (ex.: 30-90 dias) em vez do
  padrão "nunca expira" — logs antigos acumulados são custo puro.
- **Fargate/EC2**: usar Auto Scaling baseado em métrica real (CPU, ou
  profundidade da fila SQS para o `notification-service`) em vez de
  manter réplicas fixas superdimensionadas "por segurança".
- **Reserved/Savings Plans**: uma vez que a carga de produção se estabiliza
  e é previsível, comprometer capacidade por 1-3 anos reduz o custo de
  Fargate/RDS/ElastiCache significativamente frente a On-Demand.

## 16.5 Criando alarmes para não ser surpreendido pela fatura

### AWS Budgets (a ferramenta certa para orçamento)

1. Console AWS → **Billing and Cost Management** → **Budgets** →
   **Create budget**;
2. Tipo **Cost budget**, valor mensal (ex.: US$ 150);
3. Adicione uma ou mais **alert thresholds** (ex.: notificar em 50%, 80% e
   100% do orçamento previsto, e também sobre o **forecast** — a
   projeção de gasto até o fim do mês, que avisa **antes** de você
   realmente estourar);
4. Configure o destinatário (e-mail, ou um tópico SNS — o mesmo padrão de
   fan-out do [Capítulo 9](09-mensageria-sqs-sns.md) pode notificar
   Slack/PagerDuty via uma assinatura HTTP/Lambda no tópico).

### CloudWatch Billing Alarm (alternativa/complemento simples)

1. Habilite "Receive Billing Alerts" em **Billing preferences** (só
   precisa ser feito uma vez por conta);
2. Crie um Alarm no CloudWatch (região `us-east-1`, onde a métrica de
   billing sempre é publicada) sobre a métrica `EstimatedCharges`,
   disparando quando ultrapassar um limiar em USD;
3. Aponte a ação do alarme para um tópico SNS com sua assinatura de
   e-mail — o mesmo mecanismo de pub/sub usado pelo `order-service` no
   [Capítulo 9](09-mensageria-sqs-sns.md), agora para uma finalidade de
   FinOps em vez de domínio de negócio.

### AWS Cost Anomaly Detection

Serviço gratuito que usa machine learning para detectar **padrões
anômalos** de gasto (não apenas limiares fixos) — por serviço, conta ou
tag — e notifica via SNS/e-mail. Complementa o Budgets: o Budgets avisa
sobre limite absoluto; o Anomaly Detection avisa sobre "isso está gastando
muito mais que o normal para você", mesmo abaixo do limite total.

### Tags de custo

Marcar todos os recursos com tags consistentes (`Project=ecommerce-aws`,
`Environment=dev`) permite filtrar o Cost Explorer e os Budgets por
projeto/ambiente — essencial assim que você tiver mais de um projeto na
mesma conta.

---
**Anterior:** [← 15. Resilience4j](15-resiliencia-resilience4j.md) | **Próximo:** [17. Guia de Uso e Swagger →](17-guia-de-uso-swagger.md)
