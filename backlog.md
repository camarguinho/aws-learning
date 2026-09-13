# Backlog

Pontos identificados durante o desenvolvimento que ficam de fora do escopo
imediato, mas precisam ser resolvidos antes de considerar o case pronto para
produção real.

## Migração de schema versionada (Flyway/Liquibase)

`spring.jpa.hibernate.ddl-auto: update` no `order-service`
([application.yml](services/order-service/src/main/resources/application.yml))
infere o schema a partir das entidades JPA — aceitável só neste projeto de
estudo. Antes de apontar para um RDS real, trocar por uma ferramenta de
migração versionada, com o schema criado por migração explícita, não
inferido pelo Hibernate. Ver comentário já deixado no próprio
`application.yml` e a seção "Menor fricção possível para os serviços
pesados" em [PILOTO-MINISTACK.md](PILOTO-MINISTACK.md).

## Checklist de cadastro AWS + hardening da conta

Antes de provisionar qualquer recurso real, o cadastro na conta AWS
(free tier) precisa passar por:

- Escolher **Free Plan** no cadastro (não Paid Plan) — no Free Plan, se
  algo ficar esquecido ligado, a conta fecha (com 90 dias de carência pra
  reverter) em vez de gerar fatura surpresa. Cartão é exigido mas só é
  cobrado se der upgrade pra Paid Plan.
- **Nunca operar com o usuário root** no dia a dia: ativar MFA no root e
  criar um usuário/role IAM só com as permissões que este projeto precisa
  (lista em [PILOTO-MINISTACK.md](PILOTO-MINISTACK.md), seção "Pontos de
  fricção", item 1), com MFA também.
- Configurar um **AWS Budget** (Billing and Cost Management → Budgets)
  **antes** de criar o primeiro recurso, com alertas em 50/80/100% — passo
  a passo já documentado em
  [ebook/16-custos-billing-finops.md §16.5](ebook/16-custos-billing-finops.md).
- Confirmar a região `us-east-1` (já é o default assumido nos
  `application.yml` dos 3 serviços via `AWS_REGION`).
- Verificar o remetente (e, em sandbox, os destinatários de teste) no
  console do **SES** antes de testar o fluxo do `notification-service` —
  toda conta nova entra em sandbox.
- **RDS e ElastiCache não têm mais cota sempre-grátis separada** (mudança
  de jul/2025): consomem direto o crédito de sinal (US$ 100–200). Como
  cobram por hora ligados independente de uso, manter essas duas instâncias
  paradas fora das janelas de teste é o que mais estica o crédito.
