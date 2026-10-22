[⬅ Sumário](../../README.md) · Trilha SAA-C03 · S8 de 8

# S8. Migração, transferência de dados e otimização de custos

Domínio de custo (20%) mais as questões de migração. Complementa o
[Capítulo 16 (Custos, Billing e FinOps)](../16-custos-billing-finops.md).

## Migração

### Estratégias (os 6 Rs)

| Estratégia | Significado |
|---|---|
| **Rehost** (lift and shift) | mover como está para a EC2 |
| **Replatform** | pequenas otimizações (ex.: banco para RDS) |
| **Repurchase** | trocar por um SaaS |
| **Refactor** | reescrever para cloud native |
| **Retire** | desligar o que não é mais necessário |
| **Retain** | manter onde está por enquanto |

### Serviços

| Serviço | Para quê |
|---|---|
| **Application Migration Service (MGN)** | rehost de servidores, com replicação contínua |
| **Database Migration Service (DMS)** | migra e replica bancos, com mínimo de inatividade; suporta migração contínua (CDC) |
| **Schema Conversion Tool / DMS Schema Conversion** | converte o schema entre engines diferentes (ex.: Oracle para PostgreSQL) |
| **Migration Hub** | acompanha migrações em um painel central |
| **Application Discovery Service** | inventaria o ambiente on-premises |

DMS: para migração entre **engines diferentes** (heterogênea), converta
primeiro o schema e depois migre os dados com DMS. Entre engines iguais, DMS sozinho
costuma bastar.

## Transferência de dados

| Opção | Quando usar |
|---|---|
| **DataSync** | transferir grandes volumes online (NFS, SMB, HDFS, S3) agendado e com verificação; mais rápido que ferramentas manuais |
| **Snowcone** | dispositivo portátil pequeno (poucos TB) para borda e transferência |
| **Snowball Edge** | dezenas a centenas de TB, offline, com opção de computação local |
| **Snowmobile** | escala de exabytes, em um contêiner transportado por caminhão |
| **Transfer Family** | SFTP, FTPS e FTP gerenciados, com destino S3 ou EFS |
| **S3 Transfer Acceleration** | acelera uploads de longa distância usando as bordas do CloudFront |
| **Multipart upload** | uploads grandes em partes, em paralelo, retomáveis |
| **Direct Connect** | link dedicado para transferência contínua |

A disponibilidade dos dispositivos da família Snow para novos clientes pode
mudar. Confirme a situação atual na documentação da AWS, mas mantenha o
conceito: transferência física offline para volumes grandes com banda limitada.

Regra prática: calcule o tempo de transferência. Se a banda disponível leva
semanas para mover o volume, a opção offline (**Snow**) vence. Se a conexão
resolve em dias e há transferência contínua, **DataSync** (online).

## Modelos de compra de computação

| Modelo | Característica | Desconto típico | Quando usar |
|---|---|---|---|
| **On-Demand** | sem compromisso, paga por segundo | 0% | carga imprevisível ou curta |
| **Reserved Instances** | compromisso de 1 ou 3 anos para uma configuração | até ~72% | carga estável e previsível |
| **Savings Plans** | compromisso de gasto por hora em 1 ou 3 anos | até ~72% | flexibilidade entre famílias/regiões (Compute SP); EC2 Instance SP é mais limitado e mais barato |
| **Spot** | capacidade ociosa; pode ser interrompida com aviso de 2 minutos | até ~90% | tolerante a interrupção (batch, CI, big data, stateless) |
| **Dedicated Host / Instance** | servidor físico dedicado | maior custo | licenças por soquete/núcleo, conformidade |
| **Capacity Reservations** | reserva de capacidade em uma AZ | sem desconto | garantir capacidade, sem compromisso de tempo |

Dicas:

- **Spot** nunca para cargas que não toleram interrupção (banco principal).
- Combine: base com Savings Plans/RI e picos com On-Demand ou Spot.
- Lambda e Fargate também têm Savings Plans (Compute Savings Plans).

## Custos de transferência de dados

| Fluxo | Custo |
|---|---|
| Entrada da internet para a AWS | geralmente grátis |
| Saída da AWS para a internet | cobrada por GB |
| Entre AZs da mesma região | cobrada (por GB, em cada direção) |
| Entre regiões | cobrada |
| Dentro da mesma AZ, via IP privado | grátis |
| S3/DynamoDB via Gateway Endpoint | sem custo de processamento |
| NAT Gateway | por hora e por GB processado |

Otimizações:

- Use **Gateway Endpoint** para S3/DynamoDB em vez de passar pelo NAT.
- Use **CloudFront** para reduzir a saída e melhorar a performance.
- Mantenha componentes que conversam muito na **mesma AZ**, quando a
  resiliência permitir.

## Armazenamento e custo

- **S3**: classes Standard, Intelligent-Tiering, Standard-IA, One Zone-IA,
  Glacier Instant Retrieval, Glacier Flexible Retrieval e Glacier Deep
  Archive; **lifecycle policies** movem objetos entre classes
  (detalhes no [Capítulo 5](../05-armazenamento-s3.md)).
- **EBS**: migre de gp2 para gp3; exclua volumes e snapshots órfãos.
- **EFS**: use lifecycle para IA.
- **Intelligent-Tiering** quando o padrão de acesso é desconhecido.

## Bancos de dados e custo

- **Aurora Serverless v2** e **DynamoDB on-demand** para cargas imprevisíveis.
- **Reserved Instances** de RDS para bancos estáveis.
- **Read replicas** em vez de aumentar a instância principal, quando o gargalo é leitura.

## Ferramentas de gestão de custos

| Ferramenta | Função |
|---|---|
| **Cost Explorer** | analisa e projeta o gasto; recomenda Savings Plans/RI |
| **Budgets** | alertas e ações quando o gasto passa de um limite |
| **Cost and Usage Report (CUR)** | o dado mais detalhado de custo, para análise própria |
| **Cost Anomaly Detection** | alerta de gastos anormais |
| **Cost allocation tags** | atribuem custo a times/projetos |
| **Compute Optimizer** | recomenda o tamanho certo (rightsizing) de EC2, EBS, Lambda |
| **Trusted Advisor** | checagens de custo, segurança e tolerância a falhas |
| **Pricing Calculator** | estima o custo antes de implantar |
| **Organizations (cobrança consolidada)** | soma o uso das contas para alcançar descontos por volume |

## Para o exame

- **Carga estável por anos** → Reserved/Savings Plans. **Batch tolerante a falhas** → Spot.
- **Licença por servidor físico** → Dedicated Host.
- **Dezenas de TB com banda limitada** → Snowball Edge. **Online e contínuo** → DataSync.
- **SFTP gerenciado para o S3** → Transfer Family.
- **Reduzir custo de NAT para S3/DynamoDB** → Gateway Endpoint.
- **Oracle para PostgreSQL** → conversão de schema + DMS.
- **Rightsizing** → Compute Optimizer. **Alerta de limite de gasto** → Budgets.

---
**Anterior:** [← S7. Analytics, integração e containers](07-analytics-integracao-containers.md) | **Próximo:** [Banco de questões →](questoes-de-cenario.md)
