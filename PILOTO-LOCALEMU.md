# Piloto: LocalEmu no lugar do LocalStack

## Por quê

Em 23/03/2026 a LocalStack arquivou o repositório Community (Apache 2.0) e
consolidou tudo numa imagem única que exige conta e token. O plano gratuito
("Hobby") é **restrito a uso não-comercial** — inviável pra usar oficialmente
numa empresa sem contratar um plano pago (Base/Ultimate, cobrado por usuário).

Esta branch testa o **LocalEmu**, um fork comunitário do código Apache 2.0 da
última versão Community da LocalStack (mesma porta `4566`, mesma API), sem
essa restrição de licença.

## O que mudou

- `docker-compose.yml`: serviço `localstack` → `localemu`, imagem
  `localstack/localstack:3.7` → `localemu/localemu:latest`.
- Provisionamento (bucket, tabela, filas, tópico, segredo, parâmetros) saiu do
  hook `ready.d` específico do LocalStack e virou um serviço `init` dedicado
  (imagem `amazon/aws-cli`), que roda o script uma vez após o emulador ficar
  saudável — não depende de nenhuma convenção interna do LocalEmu, só da API
  AWS padrão. Script novo em `infra/localemu/init/init-aws.sh`.
- Nenhuma linha de `application.yml` dos três microsserviços mudou (endpoint
  segue configurado via `AWS_ENDPOINT_URL`, só o valor passou a apontar pro
  hostname `localemu` em vez de `localstack`).
- O ebook (`ebook/`) continua descrevendo LocalStack — é conteúdo conceitual
  da certificação AWS SAA-C03, fora do escopo deste piloto.

## Como validar

```bash
docker compose up --build
```

Depois, siga o mesmo roteiro do [Capítulo 17 do ebook](ebook/17-guia-de-uso-swagger.md)
(criar produto → criar pedido → checar notificação → testar resiliência) e
confirme que tudo funciona identicamente ao comportamento já documentado com
o LocalStack.

## Checklist de observações (preencher durante o piloto)

- [ ] `docker compose up --build` sobe sem erro e todos os healthchecks passam
- [ ] `init` conclui com sucesso (bucket, tabela, filas, tópico, segredo e
      parâmetros criados)
- [ ] Fluxo de ponta a ponta do Capítulo 17 funciona sem diferença observável
- [ ] Teste de resiliência (derrubar o `catalog-service`) se comporta igual
- [ ] Anotar aqui qualquer erro, mensagem ou comportamento diferente do
      LocalStack, se encontrado
