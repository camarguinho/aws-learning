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
