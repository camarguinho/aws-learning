[⬅ Sumário](../../README.md) · Trilha SAA-C03 · S3 de 8

# S3. Segurança: KMS, WAF, detecção e governança

Domínio de segurança: 30% da prova, o maior peso. Este capítulo complementa
o [Capítulo 2 (IAM)](../02-iam-seguranca-cognito.md) e o
[Capítulo 11 (Secrets Manager e Parameter Store)](../11-config-secrets-parameter-store.md).

## AWS KMS (Key Management Service)

Gerencia chaves de criptografia e integra com quase todos os serviços.

| Tipo de chave | Quem gerencia | Custo | Quando usar |
|---|---|---|---|
| AWS owned | AWS, invisível para você | grátis | criptografia padrão de alguns serviços |
| AWS managed (`aws/s3`, `aws/ebs`...) | AWS, na sua conta | grátis (paga uso) | criptografia simples, sem controle fino |
| Customer managed (CMK) | você | por chave + uso | controle de política, rotação, auditoria, compartilhamento entre contas |

Pontos que a prova cobra:

- **Rotação automática**: anual, nas chaves gerenciadas pelo cliente, se
  habilitada. As chaves AWS managed rotacionam automaticamente.
- **Key policy**: é obrigatória. Sem permissão na key policy, o IAM sozinho
  não basta.
- **Envelope encryption**: o KMS protege uma data key, e a data key criptografa os
  dados. Evita enviar dados grandes ao KMS.
- Chaves são **regionais**. Multi-Region keys permitem a mesma chave em várias
  regiões (útil em DR e replicação).
- **CloudHSM**: HSM dedicado, de uso exclusivo e com conformidade FIPS 140-2
  nível 3. Use quando o requisito exige controle total sobre o hardware e as
  chaves. Para o resto, KMS.
- Toda chamada ao KMS é registrada no **CloudTrail**.

### Criptografia por serviço

| Serviço | Em repouso | Observação |
|---|---|---|
| S3 | SSE-S3, SSE-KMS, SSE-C, DSSE-KMS | todo objeto novo é criptografado por padrão (SSE-S3) |
| EBS | KMS | snapshots de volume criptografado são criptografados |
| RDS | KMS, na criação | não dá para criptografar depois; é preciso copiar o snapshot criptografado |
| DynamoDB | sempre criptografado | escolha AWS owned, AWS managed ou CMK |
| EFS | KMS | em repouso na criação; em trânsito com TLS |

**ACM (Certificate Manager)** provê e renova certificados TLS públicos
gratuitos para ALB, CloudFront e API Gateway.

## Proteção de borda e de aplicação

| Serviço | Protege contra | Observação |
|---|---|---|
| **AWS WAF** | ataques de camada 7 (SQL injection, XSS, bots, rate limit por IP) | associa-se a ALB, CloudFront, API Gateway, AppSync |
| **AWS Shield Standard** | DDoS de camadas 3 e 4 | gratuito e automático |
| **Shield Advanced** | DDoS sofisticado | pago; inclui equipe de resposta (SRT) e proteção de custo |
| **AWS Firewall Manager** | gestão central de regras WAF, Shield e SGs | em várias contas via Organizations |
| **Network Firewall** | inspeção de tráfego da VPC (stateful, IPS) | para controle de saída/entrada na VPC |

Regra prática: SQL injection, XSS ou bloqueio por país/IP no ALB ou
CloudFront → WAF. DDoS volumétrico → Shield + CloudFront/Route 53.

## Detecção e investigação

| Serviço | O que faz |
|---|---|
| **GuardDuty** | detecção de ameaças analisando CloudTrail, VPC Flow Logs e DNS logs (comportamento anômalo, instâncias comprometidas) |
| **Inspector** | varredura automatizada de vulnerabilidades em EC2, imagens ECR e Lambda |
| **Macie** | descobre e classifica dados sensíveis (PII) no S3 |
| **Detective** | investiga a causa raiz de achados de segurança |
| **Security Hub** | consolida achados e verifica conformidade com padrões |
| **CloudTrail** | registra chamadas de API (quem fez o quê, quando, de onde) |
| **AWS Config** | histórico e conformidade de configuração de recursos (regras Config) |
| **Trusted Advisor** | recomendações de custo, segurança, performance, tolerância a falhas |

Diferença clássica: **CloudTrail** responde "quem chamou essa API?". **Config**
responde "como estava a configuração e ela está em conformidade?". **CloudWatch**
responde "como está o desempenho agora?".

## Governança multi-conta

- **AWS Organizations**: agrupa contas em OUs, com cobrança consolidada.
- **SCP (Service Control Policy)**: define o **teto** de permissões das contas
  de uma OU. Não concede permissão; só limita. Não afeta a conta de gerenciamento.
- **Control Tower**: cria e governa uma landing zone multi-conta com guardrails.
- **IAM Identity Center** (antigo AWS SSO): acesso único a várias contas e
  aplicações, com diretório próprio ou integração com Active Directory/IdP.
- **RAM (Resource Access Manager)**: compartilha recursos (subnets, Transit
  Gateway etc.) entre contas.

### Acesso entre contas

Padrão recomendado: uma **role** na conta de destino, com **trust policy** que
permite a conta de origem assumi-la via STS. Evita criar usuários IAM
duplicados e credenciais de longa duração.

## Credenciais e acesso a recursos

- Aplicações em EC2, ECS ou Lambda devem usar **roles** (instance profile,
  task role, execution role), nunca chaves embutidas no código.
- **Permission boundaries** limitam o máximo que uma entidade IAM pode ter.
- **Cognito**: user pools autenticam usuários da aplicação; identity pools
  trocam identidade por credenciais AWS temporárias.
- **Secrets Manager** rotaciona segredos automaticamente (RDS, por exemplo).
  Parameter Store é mais simples e barato, mas sem rotação nativa.

## Rede segura

- Dados em trânsito: TLS no ALB, CloudFront, API Gateway; **VPN** e Direct
  Connect com MACsec/IPsec para rede híbrida (ver [S6](06-conectividade-hibrida.md)).
- **VPC Flow Logs** registram o tráfego IP da VPC para auditoria.
- **S3 Block Public Access** no nível da conta evita buckets públicos por erro.
- **Bucket policy** com condição `aws:SecureTransport` força HTTPS.

## Para o exame

- Exigência de **chave controlada pelo cliente e auditável** → KMS CMK.
- **Chave em hardware dedicado** → CloudHSM.
- **Bloquear SQL injection** → WAF. **DDoS** → Shield.
- **Dados sensíveis no S3** → Macie. **Comportamento anômalo** → GuardDuty.
- **Limitar o que contas filhas podem fazer** → SCP.
- **Histórico de mudança de configuração** → Config. **Auditoria de API** → CloudTrail.
- **Compartilhar acesso entre contas sem usuários extras** → role + STS.

---
**Anterior:** [← S2. Route 53, CloudFront e Global Accelerator](02-route53-cloudfront-global-accelerator.md) | **Próximo:** [S4. DR e backup →](04-dr-e-backup.md)
