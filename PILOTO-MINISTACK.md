# Piloto: MiniStack no lugar do LocalStack

## Por quê

A partir de 23/03/2026 a LocalStack arquivou o repositório Community
(Apache 2.0) e consolidou tudo numa imagem única que exige conta e token. O
plano gratuito ("Hobby") é **restrito a uso não-comercial** — inviável pra
usar oficialmente numa empresa sem contratar um plano pago (Base/Ultimate,
cobrado por usuário).

Esta branch adota o **MiniStack**, um emulador AWS local independente
(escrito do zero, não é fork do LocalStack), licenciado sob MIT, sem
conta/token — mesmo padrão de **edge port único** (`4566`) do LocalStack,
mesma API AWS padrão.

## Arquitetura: edge port único + profile Spring

**Edge port**: uma única porta HTTP (`4566`) recebe as chamadas de todos os
serviços emulados; o MiniStack lê o SigV4 da requisição pra rotear pro
serviço/operação certo — mesma lógica de edge port do LocalStack, então
nenhuma configuração de porta por serviço é necessária (`dynamodb.endpoint`,
`s3.endpoint`, `sqs.endpoint`... todos apontam pro mesmo `4566`).

**Profile Spring como único gatilho da emulação**: a troca entre "falando
com o MiniStack" e "falando com a AWS real" **não é uma variável de
ambiente solta** — é a presença ou ausência do profile `local`:

| | `application.yml` (padrão) | `application-local.yml` (`SPRING_PROFILES_ACTIVE=local`) |
|---|---|---|
| `credentials.access-key/secret-key` | **ausente** — SDK usa a default credential provider chain (role do ECS/EC2/IRSA) | `test` / `test` |
| `*.endpoint` (dynamodb, s3, sns, sqs, secretsmanager, parameterstore, ses) | **ausente** — SDK resolve os endpoints reais da AWS pela região | `http://ministack:4566` |
| `region.static` | `${AWS_REGION:us-east-1}` (parametrizável) | herda do padrão |

Isso importa porque o padrão anterior (`endpoint: ${AWS_ENDPOINT_URL:http://localhost:4566}`)
**sempre** atribui algum valor à chave `endpoint` — vindo da variável de
ambiente ou do fallback `localhost`. Não existe, nesse formato, um estado de
"endpoint ausente": esquecer de configurar a variável em produção faz o
serviço tentar falar com `localhost:4566` silenciosamente, em vez de falhar
alto. Com o profile, a ausência de `SPRING_PROFILES_ACTIVE=local` já é, por
si só, o estado de produção — nada para lembrar de configurar corretamente.

Nenhuma classe Java dos três microsserviços muda entre os dois cenários — só
qual(is) arquivo(s) de profile o Spring carrega.

## O que mudou nesta branch

- `docker-compose.yml`: serviço `localstack` → `ministack`, imagem
  `localstack/localstack:3.7` → `ministackorg/ministack:latest`. Healthcheck
  ajustado para `/_ministack/health`.
- Variáveis de ambiente `SERVICES` e `DEBUG` (específicas do LocalStack)
  foram **removidas** — o MiniStack habilita os 60+ serviços por padrão, sem
  precisar de uma lista explícita.
- **Persistência corrigida**: `PERSIST_STATE=1` + `S3_PERSIST=1` sozinhos não
  bastam — os diretórios padrão do MiniStack (`STATE_DIR=/tmp/ministack-state`,
  `S3_DATA_DIR=/tmp/ministack-data/s3`) ficam em `/tmp`, fora de qualquer
  volume montado. Agora `STATE_DIR` e `S3_DATA_DIR` apontam para dentro do
  volume nomeado `ministack-data:/data`, então os dados sobrevivem a um
  `docker compose restart`.
- **Provisionamento simplificado**: o container `init` dedicado (imagem
  `amazon/aws-cli`) foi removido. O MiniStack aceita o mesmo hook `ready.d`
  que o LocalStack usava — o script roda **dentro do próprio container do
  emulador**, montado em `/docker-entrypoint-initaws.d/ready.d`, com
  credenciais/endpoint injetados automaticamente pelo próprio MiniStack. O
  healthcheck só fica `healthy` depois que o script termina, então os 3
  microsserviços continuam dependendo só de `ministack: condition:
  service_healthy` — sem precisar de um `depends_on` extra.
- **Troca AWS_ENDPOINT_URL → profile `local`**: os três microsserviços não
  recebem mais `AWS_ENDPOINT_URL` como variável de ambiente. Cada um ganhou
  um `application-local.yml` (ativado por `SPRING_PROFILES_ACTIVE=local` no
  `docker-compose.yml`) com as credenciais de teste e os endpoints do
  MiniStack; o `application.yml` de cada serviço não tem mais nenhuma dessas
  chaves — ver seção acima.
- O ebook (`ebook/`) continua descrevendo LocalStack — é conteúdo conceitual
  da certificação AWS SAA-C03, fora do escopo deste piloto.

## Como o MiniStack emula por baixo dos panos

- **Roteamento**: edge port único (`4566`), roteado por SigV4 — mesma lógica
  do LocalStack.
- **Serviços "leves"** (S3, DynamoDB, SQS, SNS, Secrets Manager, SSM, SES,
  CloudWatch Logs, IAM, CloudFormation): implementados em **Python puro**,
  estado em memória (`dict`/listas) — sem banco de dados real por trás.
- **Serviços "pesados"** (RDS, ElastiCache, ECS): sobem **infraestrutura
  real** via o Docker socket montado no compose — `CreateDBInstance` sobe um
  Postgres/MySQL de verdade, não uma simulação. **Não exercitado neste
  projeto**: o `postgres` e o `redis` do `docker-compose.yml` são containers
  diretos, criados pelo próprio compose, não provisionados via API do
  MiniStack — vale não afirmar "o MiniStack sobe nosso RDS" ao apresentar
  este case, porque não é o que ele faz aqui.
- **Endpoints de controle úteis pro piloto**: `/_ministack/health`
  (healthcheck), `/_ministack/reset` (limpa todo o estado — útil entre
  execuções de teste), `/_ministack/config` (reconfiguração em runtime).
  Nenhum desses é chamado pelo código da aplicação — só por humanos/CI.

## Como validar

```bash
docker compose up --build
```

Depois, siga o mesmo roteiro do [Capítulo 17 do ebook](ebook/17-guia-de-uso-swagger.md)
(criar produto → criar pedido → checar notificação → testar resiliência) e
confirme que tudo funciona identicamente ao comportamento já documentado com
o LocalStack.

## Pontos de fricção ao trocar para AWS real (fora do escopo deste piloto)

A troca de profile resolve a *conectividade*, não resolve tudo:

1. **IAM real** — o profile `local` aceita `access-key: test` sem checar
   permissão nenhuma; a role real do ECS/EC2 precisa da policy exata por
   ação (`dynamodb:PutItem`, `s3:GetObject`, `sns:Publish`,
   `sqs:SendMessage`, `secretsmanager:GetSecretValue`, `ssm:GetParameter`,
   `ses:SendEmail`, `cloudwatch:PutMetricData`).
2. **Nome de bucket S3 não é global** — `ecommerce-product-images` precisa de
   um nome único mundialmente na AWS real.
3. **SES em sandbox** — toda conta nova só envia pra e-mails verificados até
   pedir saída do sandbox.
4. **Provisionamento vira IaC** — `init-aws.sh` não existe em produção; os
   mesmos recursos precisam ser criados via Terraform/CDK/CloudFormation.
5. **Latência muda o Resilience4j** — limiares de circuit breaker/retry
   calibrados contra respostas locais quase-zero provavelmente precisam ser
   recalibrados contra latência de rede real.
6. **Custo só aparece na AWS real** — o `Scan` completo do `findAll` do
   catálogo é de graça e instantâneo no emulador; na DynamoDB real consome
   RCU e pode throttlar (ver capítulo 16 do ebook).

## Checklist de observações (preencher durante o piloto)

- [ ] `docker compose up --build` sobe sem erro e todos os healthchecks passam
- [ ] Hook `ready.d` conclui com sucesso (bucket, tabela, filas, tópico,
      segredo e parâmetros criados) — checar logs do container `ministack`
- [ ] Fluxo de ponta a ponta do Capítulo 17 funciona sem diferença observável
- [ ] Teste de resiliência (derrubar o `catalog-service`) se comporta igual
- [ ] Dados persistem entre `docker compose restart` (S3 e DynamoDB) — já
      corrigido nesta branch (`STATE_DIR`/`S3_DATA_DIR` dentro do volume),
      validar na prática
- [ ] Anotar aqui qualquer erro, mensagem ou comportamento diferente do
      LocalStack, se encontrado
