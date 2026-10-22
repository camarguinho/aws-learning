[⬅ Sumário](../../README.md) · Trilha SAA-C03 · Mapa de cobertura

# Mapa de cobertura: o que o repositório cobre e o que falta

Este mapa mostra, serviço a serviço, onde cada tema do SAA-C03 é tratado.
Use-o como checklist: marque cada linha quando conseguir explicar o serviço e
dizer quando escolhê-lo em vez de uma alternativa.

Legenda: ✅ coberto com prática no projeto, 📖 coberto só em texto, 🆕 coberto
na trilha SAA-C03 (sem prática no projeto).

## Segurança (30%)

| Tema | Onde | Status |
|---|---|---|
| IAM (users, roles, policies) | [Cap. 2](../02-iam-seguranca-cognito.md) | ✅ |
| Cognito | [Cap. 2](../02-iam-seguranca-cognito.md) | 📖 |
| Secrets Manager, Parameter Store | [Cap. 11](../11-config-secrets-parameter-store.md) | ✅ |
| Security Group, NACL, VPC Endpoint | [Cap. 3](../03-redes-vpc.md) | 📖 |
| KMS, criptografia | citado de passagem nos Caps. 2, 5, 6 e 11; aprofundado em [S3](03-seguranca-kms-waf-governanca.md) | 🆕 |
| WAF, Shield, GuardDuty, Inspector, Macie | [S3](03-seguranca-kms-waf-governanca.md) | 🆕 |
| Organizations, SCP, Control Tower, Identity Center | [S3](03-seguranca-kms-waf-governanca.md) | 🆕 |
| CloudTrail, Config, ACM | [S3](03-seguranca-kms-waf-governanca.md) | 🆕 |

## Resiliência (26%)

| Tema | Onde | Status |
|---|---|---|
| Multi-AZ e Read Replicas no RDS | [Cap. 6](../06-banco-relacional-rds.md) | 📖 |
| SQS, SNS, DLQ | [Cap. 9](../09-mensageria-sqs-sns.md) | ✅ |
| Circuit breaker, retry, bulkhead | [Cap. 15](../15-resiliencia-resilience4j.md) | ✅ |
| ELB (ALB, NLB, GWLB) | [S1](01-elb-e-auto-scaling.md) | 🆕 |
| Auto Scaling | [S1](01-elb-e-auto-scaling.md) | 🆕 |
| Route 53, políticas de roteamento | [S2](02-route53-cloudfront-global-accelerator.md) | 🆕 |
| Estratégias de DR, AWS Backup | [S4](04-dr-e-backup.md) | 🆕 |

## Performance (24%)

| Tema | Onde | Status |
|---|---|---|
| EC2, ECS/Fargate, Lambda | [Cap. 4](../04-computo-ec2-ecs-lambda.md) | 📖 |
| S3 | [Cap. 5](../05-armazenamento-s3.md) | ✅ |
| DynamoDB | [Cap. 7](../07-banco-nosql-dynamodb.md) | ✅ |
| ElastiCache | [Cap. 8](../08-cache-elasticache.md) | ✅ |
| CloudFront, Global Accelerator | [S2](02-route53-cloudfront-global-accelerator.md) | 🆕 |
| EBS, EFS, FSx | [S5](05-armazenamento-ebs-efs-fsx.md) | 🆕 |
| Kinesis, Glue, Athena, Redshift, EMR | [S7](07-analytics-integracao-containers.md) | 🆕 |
| API Gateway, EventBridge, Step Functions | [S7](07-analytics-integracao-containers.md) | 🆕 |
| EKS, ECR | [S7](07-analytics-integracao-containers.md) | 🆕 |

## Custo (20%)

| Tema | Onde | Status |
|---|---|---|
| Billing, Budgets, Cost Explorer | [Cap. 16](../16-custos-billing-finops.md) | 📖 |
| Modelos de compra (On-Demand, Reserved, Savings Plans, Spot) | [Cap. 4](../04-computo-ec2-ecs-lambda.md) e [Cap. 16](../16-custos-billing-finops.md); aprofundado em [S8](08-migracao-transferencia-e-custos.md) | 📖 |
| Classes de armazenamento S3 e ciclo de vida | [Cap. 5](../05-armazenamento-s3.md) | ✅ |
| Custos de transferência de dados | [S8](08-migracao-transferencia-e-custos.md) | 🆕 |

## Transversal

| Tema | Onde | Status |
|---|---|---|
| Rede híbrida (Direct Connect, VPN, Transit Gateway, PrivateLink) | [S6](06-conectividade-hibrida.md) | 🆕 |
| Migração (DMS, SCT, MGN, Snow, DataSync, Transfer Family) | [S8](08-migracao-transferencia-e-custos.md) | 🆕 |

## Limites do LocalStack para estudo

O LocalStack emula bem S3, SQS, SNS, DynamoDB, Secrets Manager, SSM e SES. Ele
não reproduz de forma útil o comportamento de ELB, Auto Scaling, CloudFront,
Route 53, falha de AZ ou DR. Os [laboratórios no console](labs.md) cobrem essa
lacuna.

---
**Anterior:** [← Plano de estudos](00-plano-de-estudos.md) | **Próximo:** [S1. ELB e Auto Scaling →](01-elb-e-auto-scaling.md)
