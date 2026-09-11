# Piloto: MiniStack no lugar do LocalStack

## Por quê

Em 23/03/2026 a LocalStack arquivou o repositório Community (Apache 2.0) e
consolidou tudo numa imagem única que exige conta e token. O plano gratuito
("Hobby") é **restrito a uso não-comercial** — inviável pra usar oficialmente
numa empresa sem contratar um plano pago (Base/Ultimate, cobrado por usuário).

Esta branch testa o **MiniStack**, um emulador AWS local independente
(escrito do zero, não é fork do LocalStack), licenciado sob MIT, sem
conta/token — mesma porta `4566`, mesma API AWS padrão.

## O que mudou

- `docker-compose.yml`: serviço `localstack` → `ministack`, imagem
  `localstack/localstack:3.7` → `ministackorg/ministack:latest`.
- Healthcheck ajustado para o endpoint próprio do MiniStack
  (`/_ministack/health`, diferente do `/_localstack/health`).
- Variáveis de ambiente `SERVICES`, `DEBUG` e `PERSISTENCE` (específicas do
  LocalStack) foram **removidas** — o MiniStack parece habilitar os 60+
  serviços por padrão, sem precisar de uma lista explícita. **A persistência
  de dados entre restarts do container precisa ser validada no piloto** —
  se o MiniStack tiver uma flag equivalente, adicionar aqui.
- Provisionamento (bucket, tabela, filas, tópico, segredo, parâmetros) saiu do
  hook `ready.d` específico do LocalStack e virou um serviço `init` dedicado
  (imagem `amazon/aws-cli`), que roda o script uma vez após o emulador ficar
  saudável — não depende de nenhuma convenção interna do MiniStack, só da API
  AWS padrão. Script novo em `infra/ministack/init/init-aws.sh`.
- Nenhuma linha de `application.yml` dos três microsserviços mudou (endpoint
  segue configurado via `AWS_ENDPOINT_URL`, só o valor passou a apontar pro
  hostname `ministack` em vez de `localstack`).
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
- [ ] Confirmar se dados persistem entre `docker compose restart` (ver nota
      sobre `PERSISTENCE` acima)
- [ ] Anotar aqui qualquer erro, mensagem ou comportamento diferente do
      LocalStack, se encontrado
