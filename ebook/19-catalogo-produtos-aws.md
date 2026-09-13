[⬅ Sumário](../README.md) · Capítulo 19 de 19

# 19. Catálogo Resumido de Produtos AWS por Categoria

A AWS tem mais de 200 serviços. Este capítulo é um **mapa de referência
rápida**: os produtos mais relevantes para o exame SAA-C03 e para o dia a
dia de mercado, agrupados por categoria (por **similaridade de propósito**,
não por ordem alfabética). Para cada um: o que é, que problema ele resolve,
um desenho mínimo de onde ele entra num fluxo típico, e o link oficial.

Os serviços já usados no código deste repositório (S3, DynamoDB, RDS,
ElastiCache, SQS/SNS, SES, IAM/Cognito, Secrets Manager/Parameter Store,
CloudWatch, EC2/ECS/Lambda, VPC) têm capítulo próprio e explicação de uso —
aqui eles aparecem de forma resumida, lado a lado com os "primos" da mesma
categoria que a prova também cobra.

## Computação

| Produto | O que faz | Problemas que resolve |
|---|---|---|
| **Amazon EC2** | Máquina virtual sob demanda, com controle total do SO | Precisa de servidor configurável, com root/administrador, cobrado por tempo de uso |
| **AWS Lambda** | Executa código sob demanda, sem provisionar servidor (FaaS) | Cargas esporádicas/event-driven; elimina custo de servidor ocioso |
| **Amazon ECS / AWS Fargate** | Orquestra containers Docker, com (EC2) ou sem (Fargate) gestão de servidor | Rodar múltiplos containers com deploy/scale automatizado, sem operar Kubernetes |
| **Amazon EKS** | Kubernetes gerenciado pela AWS | Times que já usam Kubernetes e querem o control plane gerenciado |
| **AWS Elastic Beanstalk** | PaaS: sobe app (Java, Node, etc.) e provisiona EC2/ELB/RDS automaticamente | Deploy rápido sem desenhar a infraestrutura manualmente |

```
Requisição
   │
   ├─► EC2 (servidor sempre ligado, você administra o SO)
   ├─► Lambda (função efêmera, cobrada por invocação/ms)
   └─► ECS/Fargate/EKS (containers orquestrados, várias réplicas)
```

- [Amazon EC2](https://aws.amazon.com/pt/ec2/)
- [AWS Lambda](https://aws.amazon.com/pt/lambda/)
- [Amazon ECS](https://aws.amazon.com/pt/ecs/) · [AWS Fargate](https://aws.amazon.com/pt/fargate/)
- [Amazon EKS](https://aws.amazon.com/pt/eks/)
- [AWS Elastic Beanstalk](https://aws.amazon.com/pt/elasticbeanstalk/)

## Armazenamento

| Produto | O que faz | Problemas que resolve |
|---|---|---|
| **Amazon S3** | Armazenamento de objetos (arquivos binários via API HTTP) | Guardar qualquer arquivo (imagens, backups, logs, data lake) de forma durável e barata |
| **Amazon EBS** | Disco de bloco anexado a uma instância EC2 | Volume de disco persistente para servidor (banco local, SO) |
| **Amazon EFS** | Sistema de arquivos NFS compartilhado entre várias instâncias/containers | Vários servidores precisam ler/escrever nos mesmos arquivos ao mesmo tempo |
| **Amazon S3 Glacier** | Classe de armazenamento do S3 para arquivamento de longo prazo | Backup/compliance com recuperação rara e custo mínimo |

```
EC2 ──► EBS (disco dedicado daquela instância)
EC2/ECS/Lambda ──► EFS (mesmo arquivo, várias instâncias)
Aplicação ──► S3 (objeto via API, não é disco) ──► Glacier (arquivamento, após X dias)
```

- [Amazon S3](https://aws.amazon.com/pt/s3/) (ver [Capítulo 5](05-armazenamento-s3.md))
- [Amazon EBS](https://aws.amazon.com/pt/ebs/)
- [Amazon EFS](https://aws.amazon.com/pt/efs/)
- [Amazon S3 Glacier](https://aws.amazon.com/pt/s3/storage-classes/glacier/)

## Banco de Dados

| Produto | O que faz | Problemas que resolve |
|---|---|---|
| **Amazon RDS** | Banco relacional gerenciado (PostgreSQL, MySQL, etc.) | Precisa de SQL/transações ACID sem administrar patch, backup e failover manualmente |
| **Amazon Aurora** | Banco relacional compatível com MySQL/PostgreSQL, motor próprio da AWS | Mesma necessidade do RDS, com mais performance/disponibilidade (storage distribuído) |
| **Amazon DynamoDB** | Banco NoSQL chave-valor/documento, totalmente gerenciado e serverless | Acesso previsível por chave, em qualquer escala, sem gerenciar servidor de banco |
| **Amazon ElastiCache** | Cache em memória (Redis ou Memcached) | Reduzir latência/carga em consultas repetidas ao banco |
| **Amazon Redshift** | Data warehouse colunar para consultas analíticas em larga escala | Consultas agregadas sobre bilhões de linhas (BI/analytics), fora do banco transacional |

```
Aplicação ──► RDS/Aurora (dados relacionais, transação ACID)
Aplicação ──► DynamoDB (acesso por chave, alta escala)
Aplicação ──► ElastiCache (cache-aside na frente do banco)
ETL ──► Redshift (consulta analítica em massa, fora do banco de produção)
```

- [Amazon RDS](https://aws.amazon.com/pt/rds/) (ver [Capítulo 6](06-banco-relacional-rds.md))
- [Amazon Aurora](https://aws.amazon.com/pt/rds/aurora/)
- [Amazon DynamoDB](https://aws.amazon.com/pt/dynamodb/) (ver [Capítulo 7](07-banco-nosql-dynamodb.md))
- [Amazon ElastiCache](https://aws.amazon.com/pt/elasticache/) (ver [Capítulo 8](08-cache-elasticache.md))
- [Amazon Redshift](https://aws.amazon.com/pt/redshift/)

## Redes e Entrega de Conteúdo

| Produto | O que faz | Problemas que resolve |
|---|---|---|
| **Amazon VPC** | Rede privada isolada, com subnets, rotas e regras de firewall | Isolar seus recursos AWS numa rede própria, controlando quem acessa o quê |
| **Elastic Load Balancing (ALB/NLB)** | Distribui tráfego entre várias instâncias/containers | Escalar horizontalmente sem ponto único de falha |
| **Amazon Route 53** | DNS gerenciado + health checks + roteamento (latência, geo, failover) | Resolver domínio para IP com alta disponibilidade e roteamento inteligente |
| **Amazon CloudFront** | CDN — cacheia conteúdo em edge locations globais | Reduzir latência para usuários globais e proteger a origem de tráfego direto |
| **Amazon API Gateway** | Porta de entrada gerenciada para APIs (REST/HTTP/WebSocket) | Expor Lambda/serviços como API com auth, throttling e versionamento, sem servidor |

```
Usuário ──► Route 53 (DNS) ──► CloudFront (cache de borda) ──► ALB (dentro da VPC) ──► instâncias/containers
Usuário ──► API Gateway ──► Lambda (arquitetura serverless, sem ALB/EC2)
```

- [Amazon VPC](https://aws.amazon.com/pt/vpc/) (ver [Capítulo 3](03-redes-vpc.md))
- [Elastic Load Balancing](https://aws.amazon.com/pt/elasticloadbalancing/)
- [Amazon Route 53](https://aws.amazon.com/pt/route53/)
- [Amazon CloudFront](https://aws.amazon.com/pt/cloudfront/)
- [Amazon API Gateway](https://aws.amazon.com/pt/api-gateway/)

## Integração e Mensageria

| Produto | O que faz | Problemas que resolve |
|---|---|---|
| **Amazon SQS** | Fila de mensagens (ponto a ponto, um consumidor processa cada mensagem) | Desacoplar produtor e consumidor; absorver picos sem perder mensagem |
| **Amazon SNS** | Pub/sub — publica uma mensagem para vários assinantes (fan-out) | Um evento precisa notificar vários sistemas diferentes ao mesmo tempo |
| **Amazon EventBridge** | Barramento de eventos entre serviços AWS, SaaS e sua própria aplicação | Rotear eventos por regra/conteúdo entre múltiplas fontes e destinos, sem código de integração ponto a ponto |
| **AWS Step Functions** | Orquestra várias etapas (Lambda, ECS, etc.) como uma máquina de estados visual | Fluxos com várias etapas, retries e branching, sem costurar tudo "na mão" |

```
Produtor ──► SNS (tópico) ──┬─► SQS (fila A) ──► consumidor A
                             └─► SQS (fila B) ──► consumidor B

Step Functions: [Passo 1: Lambda] ──► [Passo 2: ECS Task] ──► [Passo 3: SNS] (com retry/catch em cada seta)
```

- [Amazon SQS](https://aws.amazon.com/pt/sqs/) · [Amazon SNS](https://aws.amazon.com/pt/sns/) (ver [Capítulo 9](09-mensageria-sqs-sns.md))
- [Amazon EventBridge](https://aws.amazon.com/pt/eventbridge/)
- [AWS Step Functions](https://aws.amazon.com/pt/step-functions/)

## Segurança, Identidade e Conformidade

| Produto | O que faz | Problemas que resolve |
|---|---|---|
| **AWS IAM** | Controla quem (usuário/role/serviço) pode fazer o quê em quais recursos | Autenticação e autorização de acesso à própria conta AWS |
| **Amazon Cognito** | Autenticação/gestão de usuários finais da sua aplicação (não da conta AWS) | Login, cadastro, MFA e federação de identidade para o app, sem construir do zero |
| **AWS Secrets Manager** | Armazena e roda credenciais (senha de banco, API key) com rotação automática | Eliminar senha hardcoded no código/config |
| **AWS KMS** | Gerencia chaves de criptografia usadas por outros serviços (S3, RDS, EBS) | Criptografia em repouso com controle e auditoria centralizados da chave |
| **AWS WAF / AWS Shield** | Firewall de aplicação web / proteção contra DDoS | Bloquear payloads maliciosos (SQLi, XSS) e mitigar ataques volumétricos |

```
Usuário final ──► Cognito (login do app) ──► app ──► IAM Role (permissão para chamar AWS)
app ──► Secrets Manager (busca credencial em runtime, nunca no código)
S3/RDS/EBS ──► KMS (chave de criptografia gerenciada)
Internet ──► Shield (anti-DDoS) ──► WAF (regras de payload) ──► ALB/CloudFront
```

- [AWS IAM](https://aws.amazon.com/pt/iam/) (ver [Capítulo 2](02-iam-seguranca-cognito.md))
- [Amazon Cognito](https://aws.amazon.com/pt/cognito/) (ver [Capítulo 2](02-iam-seguranca-cognito.md))
- [AWS Secrets Manager](https://aws.amazon.com/pt/secrets-manager/) (ver [Capítulo 11](11-config-secrets-parameter-store.md))
- [AWS KMS](https://aws.amazon.com/pt/kms/)
- [AWS WAF](https://aws.amazon.com/pt/waf/) · [AWS Shield](https://aws.amazon.com/pt/shield/)

## Gerenciamento e Governança

| Produto | O que faz | Problemas que resolve |
|---|---|---|
| **Amazon CloudWatch** | Coleta métricas, logs e alarmes de todos os serviços | Saber se o sistema está saudável e ser alertado antes do cliente perceber |
| **AWS CloudTrail** | Registra todo chamada de API feita na conta (quem fez o quê, quando) | Auditoria de segurança e investigação de incidentes |
| **AWS Systems Manager (Parameter Store)** | Guarda parâmetros de configuração e automatiza operações em instâncias | Centralizar config por ambiente sem hardcode nem redeploy |
| **AWS CloudFormation** | Provisiona infraestrutura a partir de um template declarativo (IaC) | Reproduzir o mesmo ambiente (dev/stage/prod) de forma versionada e auditável |

```
Todos os serviços ──► CloudWatch (métricas/logs/alarmes)
Todas as chamadas de API ──► CloudTrail (log de auditoria, imutável)
Template YAML/JSON ──► CloudFormation ──► cria VPC + EC2 + RDS + IAM (infraestrutura como código)
```

- [Amazon CloudWatch](https://aws.amazon.com/pt/cloudwatch/) (ver [Capítulo 12](12-observabilidade-cloudwatch.md))
- [AWS CloudTrail](https://aws.amazon.com/pt/cloudtrail/)
- [AWS Systems Manager](https://aws.amazon.com/pt/systems-manager/) (ver [Capítulo 11](11-config-secrets-parameter-store.md))
- [AWS CloudFormation](https://aws.amazon.com/pt/cloudformation/)

## Analytics e Big Data

| Produto | O que faz | Problemas que resolve |
|---|---|---|
| **Amazon Kinesis** | Ingestão e processamento de streams de dados em tempo real | Capturar eventos de alto volume (cliques, IoT, logs) continuamente, não em lote |
| **Amazon Athena** | Consulta SQL direto sobre arquivos no S3, sem carregar em banco | Analisar dados brutos (CSV/Parquet) no S3 sob demanda, pagando só pela consulta |
| **AWS Glue** | ETL gerenciado (extrai, transforma e carrega dados) + catálogo de dados | Preparar/mover dados entre fontes sem escrever/operar jobs Spark manualmente |

```
Evento em tempo real ──► Kinesis (stream) ──► consumidor (Lambda/aplicação)
Arquivos brutos no S3 ──► Glue (ETL, catalogação) ──► S3 (dados tratados) ──► Athena (SQL ad-hoc)
```

- [Amazon Kinesis](https://aws.amazon.com/pt/kinesis/)
- [Amazon Athena](https://aws.amazon.com/pt/athena/)
- [AWS Glue](https://aws.amazon.com/pt/glue/)

## Machine Learning / IA Generativa

| Produto | O que faz | Problemas que resolve |
|---|---|---|
| **Amazon SageMaker** | Plataforma completa para treinar, ajustar e publicar modelos de ML próprios | Times de ciência de dados que precisam de todo o ciclo de vida do modelo gerenciado |
| **Amazon Bedrock** | Acesso via API a modelos de fundação (Claude, Titan, etc.) prontos, sem treinar nada | Adicionar IA generativa (chat, resumo, geração de texto) a uma aplicação sem operar infraestrutura de modelo |

```
Dados de treino ──► SageMaker (treina/hospeda modelo próprio) ──► endpoint de inferência ──► app
app ──► Bedrock (API gerenciada, modelo de terceiros já pronto) ──► resposta
```

- [Amazon SageMaker](https://aws.amazon.com/pt/sagemaker/)
- [Amazon Bedrock](https://aws.amazon.com/pt/bedrock/)

## Comunicação com o Usuário

| Produto | O que faz | Problemas que resolve |
|---|---|---|
| **Amazon SES** | Envio (e recebimento) de e-mail transacional em escala | Enviar e-mail de confirmação/notificação sem operar servidor SMTP próprio |

```
Aplicação ──► SES ──► caixa de entrada do cliente
```

- [Amazon SES](https://aws.amazon.com/pt/ses/) (ver [Capítulo 10](10-email-ses.md))

## Como usar este catálogo para estudar

Para o exame SAA-C03, a pegadinha mais comum é a prova descrever um
**problema** (ex.: "recuperar arquivos raramente acessados ao menor custo
possível" ou "desacoplar dois sistemas para que a queda de um não afete o
outro") e pedir o **produto certo** — não o contrário. Por isso as tabelas
acima priorizam a coluna "problemas que resolve": treine associando cenário
→ serviço, não decorando definição isolada.

---
**Anterior:** [← 18. Referências](18-referencias.md) | [⬆ Voltar ao Sumário](../README.md)
