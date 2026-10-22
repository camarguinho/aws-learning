[⬅ Sumário](../../README.md) · Trilha SAA-C03 · S1 de 8

# S1. Elastic Load Balancing e Auto Scaling

Domínios: resiliência (26%) e performance (24%). Este é o par de serviços
que mais aparece nas questões de "alta disponibilidade e escalabilidade".

## Elastic Load Balancing (ELB)

O ELB distribui tráfego entre alvos (instâncias EC2, containers, IPs, funções
Lambda) em várias AZs e só envia para alvos saudáveis (health checks).

| | Application LB (ALB) | Network LB (NLB) | Gateway LB (GWLB) |
|---|---|---|---|
| Camada | 7 (HTTP/HTTPS, gRPC, WebSocket) | 4 (TCP, UDP, TLS) | 3 (IP) |
| Roteamento | por caminho, host, header, query string | por porta/protocolo | transparente, para appliances |
| IP estático / Elastic IP | não (usa DNS) | sim, um por AZ | não se aplica |
| Latência | boa | ultrabaixa, milhões de req/s | n/a |
| Alvos | EC2, IP, Lambda, ECS | EC2, IP, ALB | appliances de rede (firewall, IDS) |
| Integra com WAF | sim | não | não |
| Caso típico | web apps, microsserviços, APIs | jogos, IoT, TCP/UDP, IP fixo, PrivateLink | inspeção de tráfego de terceiros |

Pontos que a prova explora:

- **Cross-zone load balancing**: no ALB vem ligado por padrão e sem custo de
  transferência entre AZs; no NLB vem desligado e gera custo se for ligado.
- **Sticky sessions** (afinidade): o ALB usa cookie. Útil para estado em
  memória, mas prejudica a distribuição. A alternativa recomendada é tirar o
  estado da instância (ElastiCache, DynamoDB).
- **Conexão HTTPS**: o certificado vem do **ACM** e é associado ao listener. O
  ALB pode terminar o TLS (offload) e falar HTTP com os alvos.
- **SNI**: um listener serve vários certificados para vários domínios.
- **IP do cliente**: atrás de um ALB, o IP de origem vem no header
  `X-Forwarded-For`. O NLB preserva o IP de origem.
- **Connection draining** (deregistration delay): dá tempo às requisições em
  andamento antes de remover o alvo.
- Um ALB só enxerga alvos dentro da mesma região.

### Como se encaixa no case

A [topologia da VPC](../03-redes-vpc.md) já prevê o ALB em subnets públicas e a
aplicação em subnets privadas, com `sg-app` aceitando tráfego só de `sg-alb`.
Cada microsserviço seria um **target group**, e o ALB roteia por caminho
(`/catalog/*`, `/orders/*`) ou por host.

## EC2 Auto Scaling

Um **Auto Scaling Group (ASG)** mantém a quantidade desejada de instâncias,
substitui as que falham e escala de acordo com a demanda.

Componentes:

- **Launch template**: AMI, tipo de instância, SGs, role IAM, user data.
- **Capacidade**: mínima, desejada, máxima.
- **Subnets/AZs**: o ASG balanceia entre as AZs escolhidas. Para
  resiliência, use pelo menos duas.
- **Health checks**: do EC2 e, se integrado, do ELB. Com health check do ELB
  ligado, uma instância que falha na aplicação (mas está "viva" no EC2) é
  substituída.

### Políticas de escala

| Política | Como funciona | Quando usar |
|---|---|---|
| Target tracking | mantém uma métrica no alvo (ex.: CPU a 50%, requisições por alvo) | padrão recomendado, simples |
| Step scaling | degraus de escala conforme o tamanho do alarme CloudWatch | resposta graduada a picos |
| Simple scaling | uma ação por alarme, com cooldown | legado, evite |
| Scheduled | em horários definidos | demanda previsível (ex.: fechamento mensal) |
| Predictive | usa histórico para escalar antes da demanda | padrões cíclicos |

Detalhes que caem na prova:

- **Cooldown** e warm-up evitam escalar de novo antes de a ação anterior fazer
  efeito.
- **Lifecycle hooks** permitem executar passos antes de uma instância entrar
  em serviço ou ser terminada.
- **Mixed instances policy** combina On-Demand e Spot no mesmo ASG para reduzir
  custo mantendo uma base estável.
- **Warm pools** mantêm instâncias pré-inicializadas para reduzir o tempo de
  entrada em serviço.
- Escalar horizontalmente (mais instâncias) é a resposta padrão. Escalar
  verticalmente (instância maior) exige parada.

## Escala em outros serviços

| Serviço | Mecanismo |
|---|---|
| ECS | Service Auto Scaling (target tracking sobre CPU/memória/requisições) |
| Lambda | escala automaticamente; limite via concorrência reservada/provisionada |
| DynamoDB | modo on-demand ou Application Auto Scaling no modo provisionado |
| Aurora | Aurora Auto Scaling para réplicas de leitura; Aurora Serverless v2 |

## Para o exame

- ELB + ASG em **2 ou mais AZs** é a resposta para "tolerar a falha de uma AZ".
- Precisa de **IP fixo** ou de **TCP/UDP de altíssima performance** → NLB.
- Precisa de **roteamento por URL/host** ou de **WAF** → ALB.
- **Appliance de segurança de terceiros** no caminho do tráfego → GWLB.
- Aplicação com sessão em memória escalando mal → externalizar a sessão
  (ElastiCache) em vez de sticky sessions.
- Escala por fila: use o **tamanho da fila SQS por instância** como métrica
  de target tracking para workers.

## Pegadinhas comuns

- ELB não substitui o ASG: o ELB distribui, o ASG cria e remove instâncias.
- Health check apenas do EC2 não detecta falha na aplicação.
- ASG em uma só AZ não é alta disponibilidade.
- NLB não enxerga HTTP: sem roteamento por caminho.

---
**Anterior:** [← Mapa de cobertura](mapa-de-cobertura.md) | **Próximo:** [S2. Route 53, CloudFront e Global Accelerator →](02-route53-cloudfront-global-accelerator.md)
