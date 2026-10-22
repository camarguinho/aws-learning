[⬅ Sumário](../../README.md) · Trilha SAA-C03 · S4 de 8

# S4. Recuperação de desastres e backup

Domínio de resiliência. Aqui a prova pede para casar o requisito de negócio
(quanto tempo e quanto dado posso perder) com a estratégia de menor custo que
o atende.

## RPO e RTO

- **RPO (Recovery Point Objective)**: quanto dado você aceita perder, medido em
  tempo. Define a frequência de backup/replicação.
- **RTO (Recovery Time Objective)**: quanto tempo você aceita ficar fora do ar.
  Define o quanto da infraestrutura precisa estar pronto.

Quanto menores o RPO e o RTO, maior o custo.

## As 4 estratégias de DR

| Estratégia | O que fica rodando na região de DR | RTO | RPO | Custo |
|---|---|---|---|---|
| **Backup e restauração** | nada; só backups copiados | horas | horas | o mais baixo |
| **Pilot light** | núcleo mínimo (ex.: banco replicado), app desligada | dezenas de minutos | minutos | baixo |
| **Warm standby** | versão reduzida, funcional, escalada na falha | minutos | segundos a minutos | médio |
| **Multi-site ativo/ativo** | capacidade total nas duas regiões | quase zero | quase zero | o mais alto |

Como decidir: leia o RTO/RPO do enunciado e escolha a estratégia mais barata que
cumpre. "Recuperar em horas, custo mínimo" é backup e restauração. "Tempo de
inatividade quase zero" é ativo/ativo.

## Alta disponibilidade vs. recuperação de desastres

- **Multi-AZ**: tolera falha de uma AZ, dentro da região. É alta disponibilidade.
- **Multi-Region**: tolera falha de uma região inteira. É DR.

Se o enunciado fala em "falha de região" ou "desastre", Multi-AZ não basta.

## Recursos por serviço

| Serviço | Mecanismo de resiliência/DR |
|---|---|
| **RDS** | Multi-AZ (HA, failover automático); read replicas (escala de leitura, podem ser promovidas e ficar em outra região); snapshots automáticos e manuais; cópia de snapshot entre regiões |
| **Aurora** | 6 cópias em 3 AZs; até 15 réplicas de leitura; **Aurora Global Database** (replicação entre regiões com latência típica abaixo de 1 s, RPO de segundos); Backtrack (voltar no tempo, Aurora MySQL) |
| **DynamoDB** | PITR (restauração para um ponto no tempo até 35 dias); **global tables** (multi-região, ativo/ativo); backups on-demand |
| **S3** | 11 noves de durabilidade; versionamento; **CRR** (replicação entre regiões) e SRR (mesma região); MFA Delete; Object Lock |
| **EBS** | snapshots (incrementais) armazenados no S3; Data Lifecycle Manager automatiza; cópia entre regiões |
| **EFS** | replicação entre regiões; AWS Backup |
| **EC2** | AMIs copiáveis entre regiões; Auto Scaling em várias AZs; **Elastic Disaster Recovery (DRS)** para replicar servidores continuamente |
| **Route 53** | failover por health check (ver [S2](02-route53-cloudfront-global-accelerator.md)) |

## AWS Backup

Serviço central para definir **planos de backup** (agendamento, retenção,
ciclo de vida) e aplicá-los a vários serviços (EC2, EBS, RDS, DynamoDB, EFS,
FSx, S3 e outros).

- **Backup vault** com **Vault Lock** (WORM) protege contra exclusão, inclusive
  por administradores.
- Cópia entre regiões e entre contas, para proteção contra ransomware e
  falha de conta.
- Políticas aplicadas por tags e centralizadas via Organizations.

## Desacoplamento e resiliência de aplicação

- Filas (SQS) absorvem picos e falhas temporárias do consumidor
  ([Capítulo 9](../09-mensageria-sqs-sns.md)).
- Sem estado nas instâncias: sessões em ElastiCache/DynamoDB, arquivos em S3.
- Circuit breaker, retry e bulkhead no nível do código
  ([Capítulo 15](../15-resiliencia-resilience4j.md)).
- Infraestrutura como código (CloudFormation) reconstrói o ambiente de DR de
  forma repetível.

## Para o exame

- **Pilot light**: a parte crítica (banco) já roda e replica; o resto sobe sob
  demanda. **Warm standby**: tudo roda, só que em escala menor.
- Banco relacional com **DR entre regiões e RPO de segundos** → Aurora Global
  Database.
- DynamoDB **ativo/ativo multi-região** → global tables.
- Proteger backups contra exclusão → Backup Vault Lock, S3 Object Lock, MFA Delete.
- Failover automático **dentro da região** em RDS → Multi-AZ. Failover
  **entre regiões** → promover read replica ou usar Aurora Global.
- Réplicas de leitura **não são** failover automático no RDS convencional.

## Pegadinhas comuns

- Multi-AZ do RDS não aumenta performance de leitura; é para disponibilidade.
- Snapshot do RDS não cobre a falha de região, a menos que seja copiado para
  outra região.
- Replicação S3 (CRR) só replica objetos novos após ativada, e exige
  versionamento nos dois buckets.

---
**Anterior:** [← S3. Segurança](03-seguranca-kms-waf-governanca.md) | **Próximo:** [S5. EBS, EFS e FSx →](05-armazenamento-ebs-efs-fsx.md)
