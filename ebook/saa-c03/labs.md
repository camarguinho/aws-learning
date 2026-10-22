[⬅ Sumário](../../README.md) · Trilha SAA-C03 · Laboratórios

# Laboratórios no console AWS

O [LocalStack](https://docs.localstack.cloud/) emula bem S3, SQS, SNS, DynamoDB
e Secrets Manager (os serviços do projeto), mas não reproduz ELB, Auto Scaling,
CloudFront, Route 53 nem falha de AZ. Estes laboratórios cobrem essa lacuna
numa conta AWS real.

## Antes de começar

- Use uma **conta de estudo**, separada da conta de trabalho.
- Proteja o usuário raiz com MFA e trabalhe com um usuário/role de menor privilégio.
- Faça o **Lab 0** primeiro. Ele evita surpresa na fatura.
- Todo lab tem um passo de **limpeza**. Não pule. Recursos esquecidos geram custo.
- Os preços e o que entra no Free Tier mudam. Confira a página de preços do
  serviço antes de cada lab.
- Escolha uma região e fique nela em todos os labs.

Como registrar: ao fim de cada lab, escreva em uma frase o que você observou e o
que a prova pergunta sobre aquilo.

---

## Lab 0: Alerta de orçamento

Capítulo de apoio: [Capítulo 16](../16-custos-billing-finops.md).

1. Em **Billing → Budgets**, crie um orçamento de custo mensal com um valor baixo
   (por exemplo, US$ 10).
2. Configure alertas por e-mail em 50%, 80% e 100% do valor.
3. Ative o **Cost Anomaly Detection**.

Objetivo: perceber que custo é um requisito da arquitetura, e conhecer Budgets
e Anomaly Detection para as questões do domínio de custo.

---

## Lab 1: ALB + Auto Scaling em duas AZs

Capítulo de apoio: [S1](01-elb-e-auto-scaling.md).

1. Crie um **launch template** com Amazon Linux, instância pequena e um *user
   data* que instale e inicie um servidor web e escreva o ID da instância e a AZ
   na página.
2. Crie um **target group** (HTTP) e um **Application Load Balancer** em duas
   subnets públicas de AZs diferentes.
3. Crie um **Auto Scaling Group** com mínimo 2, desejado 2, máximo 4, em duas
   AZs, anexado ao target group, com **health check do ELB** habilitado.
4. Adicione uma política de **target tracking** (por exemplo, CPU em 50%).
5. Acesse o DNS do ALB várias vezes e observe a página alternando entre instâncias.
6. **Experimento de falha:** termine uma instância manualmente. Observe o ASG
   criando outra e o ALB deixando de enviar tráfego à instância removida.
7. Gere carga na CPU (uma ferramenta de stress na instância) e observe a escala.

Pergunte-se: o que aconteceria com apenas uma AZ? Com health check só do EC2?

**Limpeza:** exclua o ASG (isso termina as instâncias), o ALB, o target group e o
launch template.

---

## Lab 2: S3 privado atrás do CloudFront com OAC

Capítulo de apoio: [S2](02-route53-cloudfront-global-accelerator.md).

1. Crie um bucket S3 **privado** (Block Public Access ligado) e envie um arquivo
   `index.html`.
2. Crie uma distribuição **CloudFront** com o bucket como origem e **Origin Access
   Control**. Aplique a bucket policy que o console sugere.
3. Acesse a página pelo domínio do CloudFront. Depois tente o endereço direto do
   S3 e confirme o bloqueio.
4. Altere o arquivo, acesse de novo e observe o cache. Crie uma **invalidation**
   e observe a atualização.

Pergunte-se: por que o certificado do CloudFront precisa estar em us-east-1?
Qual a diferença para um bucket público?

**Limpeza:** desabilite e exclua a distribuição, esvazie e exclua o bucket.

---

## Lab 3: Roteamento no Route 53

Capítulo de apoio: [S2](02-route53-cloudfront-global-accelerator.md).

Exige um domínio. Se você não tiver um, pule os passos 1 a 2 e estude apenas os
conceitos, pois registrar um domínio tem custo anual.

1. Crie uma *hosted zone* para o seu domínio (ou um subdomínio).
2. Crie um registro **Alias** apontando para o ALB do Lab 1.
3. Crie um **health check** para o endpoint e uma política de **failover** com
   um recurso secundário (por exemplo, uma página estática no S3).
4. Derrube o primário e observe a troca (lembre do TTL).
5. Experimente **weighted** com dois endpoints e proporções diferentes.

**Limpeza:** exclua os registros, os health checks e a hosted zone (ela é cobrada
por mês).

---

## Lab 4: KMS, S3 e CloudTrail

Capítulo de apoio: [S3](03-seguranca-kms-waf-governanca.md).

1. Crie uma **chave KMS gerenciada pelo cliente** com rotação automática.
2. Crie um bucket com criptografia padrão **SSE-KMS** usando essa chave.
3. Envie um objeto e confirme nas propriedades que a criptografia usa a sua chave.
4. No **CloudTrail**, procure os eventos `GenerateDataKey` e `Decrypt` gerados
   pelo upload e pelo download.
5. Remova a permissão de uso da chave de uma role de teste e tente ler o objeto.
   Observe a negação.

Pergunte-se: por que o IAM sozinho não basta sem a *key policy*?

**Limpeza:** exclua o objeto e o bucket. Agende a exclusão da chave KMS (existe um
período de espera de 7 a 30 dias, e a chave segue cobrada nesse intervalo).

---

## Lab 5: Gateway Endpoint do S3 e NAT Gateway

Capítulo de apoio: [S6](06-conectividade-hibrida.md) e [Capítulo 3](../03-redes-vpc.md).

O NAT Gateway cobra por hora. Faça este lab em uma sessão curta.

1. Crie uma VPC com uma subnet pública e uma privada, e um NAT Gateway.
2. Em uma instância na subnet privada (acesso por Session Manager), acesse o S3
   e observe que o tráfego sai pelo NAT.
3. Crie um **Gateway Endpoint** para o S3 e associe-o à route table privada.
   Confirme a nova rota (prefix list do S3).
4. Repita o acesso e observe que agora ele usa o endpoint.

Pergunte-se: o que você economizou em transferência pelo NAT? Por que o gateway
endpoint não serve para acesso vindo do on-premises?

**Limpeza:** exclua o NAT Gateway primeiro (e libere o Elastic IP), depois a
instância, o endpoint e a VPC.

---

## Lab 6: RDS Multi-AZ e failover

Capítulo de apoio: [Capítulo 6](../06-banco-relacional-rds.md) e [S4](04-dr-e-backup.md).

O Multi-AZ dobra o custo da instância e pode não estar no Free Tier. Faça com a
menor instância e destrua no mesmo dia.

1. Crie uma instância RDS (PostgreSQL ou MySQL) pequena com **Multi-AZ**.
2. Anote a AZ atual. Conecte-se pelo endpoint DNS.
3. Faça **Reboot com failover**. Observe a mudança de AZ e a interrupção
   curta, e que o endpoint DNS continua o mesmo.
4. Crie uma **read replica** e observe que ela tem outro endpoint.
5. Tire um snapshot manual e restaure-o em uma nova instância.

Pergunte-se: Multi-AZ aumenta a leitura? Qual a diferença entre failover e
promoção de réplica?

**Limpeza:** exclua as instâncias (a replica primeiro), sem snapshot final se
não precisar, e apague os snapshots manuais.

---

## Lab 7: AWS Backup

Capítulo de apoio: [S4](04-dr-e-backup.md).

1. Crie um **backup vault** e um **plano de backup** diário com retenção curta.
2. Atribua recursos por **tag** (um volume EBS pequeno de teste).
3. Dispare um **backup sob demanda** e acompanhe o job.
4. Restaure o ponto de recuperação em um novo volume.
5. Configure uma **cópia para outra região** na regra do plano.

**Limpeza:** exclua os pontos de recuperação, o plano, o vault e os volumes.

---

## Checklist final dos labs

- [ ] Lab 0: orçamento e alertas
- [ ] Lab 1: ALB + ASG
- [ ] Lab 2: S3 + CloudFront + OAC
- [ ] Lab 3: Route 53
- [ ] Lab 4: KMS + S3 + CloudTrail
- [ ] Lab 5: Gateway Endpoint + NAT
- [ ] Lab 6: RDS Multi-AZ
- [ ] Lab 7: AWS Backup

---
**Anterior:** [← Banco de questões](questoes-de-cenario.md) | **Próximo:** [Diário de erros →](diario-de-erros.md)
