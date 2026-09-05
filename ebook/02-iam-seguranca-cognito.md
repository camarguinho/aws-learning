[⬅ Sumário](../README.md) · Capítulo 2 de 18

# 2. IAM, Segurança e Amazon Cognito

## AWS IAM (Identity and Access Management)

IAM controla **quem** (autenticação) pode fazer **o quê** (autorização) em
quais recursos. Conceitos cobrados no exame:

- **Usuário (User)**: identidade de longa duração, normalmente uma pessoa
  ou uma aplicação legada. Evite usar para aplicações.
- **Role (papel)**: identidade **assumida temporariamente**, sem
  credenciais fixas. É a forma recomendada de dar permissão a uma
  aplicação (EC2, ECS Task, Lambda) para chamar outros serviços AWS —
  as credenciais são geradas e rotacionadas automaticamente pelo STS
  (Security Token Service).
- **Policy (política)**: documento JSON declarando `Effect` (Allow/Deny),
  `Action` (ex.: `dynamodb:GetItem`), `Resource` (ARN) e, opcionalmente,
  `Condition`.
- **Princípio do menor privilégio**: cada identidade só deve poder fazer
  exatamente o necessário. É o critério nº1 de segurança cobrado no exame.

### Como o projeto aplicaria isso em produção

Cada microsserviço teria sua própria **IAM Role** (uma ECS Task Role ou uma
Instance Profile de EC2), sem chave de acesso fixa:

```jsonc
// Policy da role do catalog-service (exemplo)
{
  "Version": "2012-10-17",
  "Statement": [
    { "Effect": "Allow", "Action": ["dynamodb:GetItem", "dynamodb:PutItem", "dynamodb:DeleteItem", "dynamodb:Scan"],
      "Resource": "arn:aws:dynamodb:us-east-1:ACCOUNT_ID:table/Product" },
    { "Effect": "Allow", "Action": ["s3:GetObject", "s3:PutObject"],
      "Resource": "arn:aws:s3:::ecommerce-product-images/*" }
  ]
}
```

Note que a policy não dá `dynamodb:*` nem acesso a outras tabelas/buckets —
só o que o `catalog-service` de fato usa (`ProductRepository`,
`ProductImageService`). O `order-service` teria sua própria role, restrita
a `sns:Publish` no tópico específico e leitura no seu segredo do Secrets
Manager.

Localmente (via LocalStack), usamos credenciais estáticas fake
(`test`/`test`, ver `application.yml` de cada serviço) só porque o
LocalStack não valida IAM por padrão — isso **nunca** deve acontecer em uma
conta AWS real.

## Amazon Cognito

Cognito resolve autenticação/autorização de usuários finais (não confundir
com IAM, que é para recursos AWS/aplicações internas):

- **User Pools**: diretório de usuários gerenciado, com sign-up/sign-in,
  MFA, verificação de e-mail (poderia substituir nosso envio manual de
  e-mail de boas-vindas via SES em uma tela de cadastro), e emissão de
  tokens **JWT** (OIDC) após login.
- **Identity Pools (Federated Identities)**: trocam um token (do User Pool,
  Google, Facebook, etc.) por credenciais AWS temporárias — útil quando um
  app cliente precisa chamar a AWS diretamente (ex.: upload direto no S3).

### Onde o Cognito se encaixaria no case

O catálogo de produtos é público (não exige login), mas criar um pedido
deveria exigir um cliente autenticado. Em produção, o `order-service`
validaria o JWT emitido pelo Cognito como um **OAuth2 Resource Server**:

```yaml
# order-service/application.yml (perfil "cognito", desabilitado por padrão)
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX
```

Com a dependência `spring-boot-starter-oauth2-resource-server`, o Spring
Security valida assinatura, expiração e emissor do token automaticamente —
nenhuma linha de código extra é necessária além da configuração acima e de
um `SecurityFilterChain` liberando `/swagger-ui/**` e `/actuator/health`.

Não habilitamos esse perfil por padrão no `docker-compose.yml` porque o
Cognito não tem um emulador completo no LocalStack Community — mas o
código de configuração acima é o que você usaria na AWS real, e vale como
material de estudo e como próximo passo natural de evolução do projeto.

## Boas práticas de segurança aplicadas no projeto

- **Nenhum segredo em código ou em `application.yml` commitado** — a senha
  do RDS vem do Secrets Manager (ver `order-service/application.yml`,
  propriedade `spring.config.import`), não de uma variável hardcoded.
- **Buckets S3 privados por padrão** — o bucket de imagens nunca é acessado
  via URL pública; sempre via URL pré-assinada com expiração curta (ver
  `ProductImageService.java`).
- **Criptografia em trânsito**: todos os SDKs da AWS usam HTTPS/TLS por
  padrão para as chamadas de API.
- **Criptografia em repouso**: RDS, S3, DynamoDB e SQS suportam
  criptografia at-rest com chaves gerenciadas pelo **AWS KMS** — em uma
  conta real, isso é habilitado na criação do recurso (Terraform/CDK) e
  não exige mudança de código.

## Para o exame

Pontos frequentemente cobrados:

- IAM é **global** (não é por região); os recursos que ele protege, sim.
- Roles não têm credenciais de longo prazo — sempre prefira Roles a Users
  para aplicações.
- Diferença entre **autenticação** (Cognito User Pools / IAM) e
  **autorização federada** (Cognito Identity Pools / IAM Roles assumidas
  via STS `AssumeRoleWithWebIdentity`).
- Security Groups (stateful, a nível de instância/recurso) vs. Network
  ACLs (stateless, a nível de subnet) — aprofundado no
  [Capítulo 3](03-redes-vpc.md).

---
**Anterior:** [← 1. Fundamentos AWS](01-fundamentos-aws-well-architected.md) | **Próximo:** [3. Redes: VPC →](03-redes-vpc.md)
