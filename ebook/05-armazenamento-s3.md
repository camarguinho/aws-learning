[⬅ Sumário](../README.md) · Capítulo 5 de 18

# 5. Armazenamento: Amazon S3

## O que é

S3 (Simple Storage Service) é armazenamento de objetos: você guarda um
arquivo (objeto) identificado por uma **key** dentro de um **bucket**. Não é
um sistema de arquivos POSIX nem um banco de dados — é a base de
armazenamento durável (99.999999999% de durabilidade — "11 noves") mais
usada da AWS, para qualquer tipo de arquivo binário.

Classes de armazenamento (storage classes) relevantes para o exame:

| Classe | Caso de uso | Custo relativo |
|---|---|---|
| S3 Standard | acesso frequente | referência |
| S3 Standard-IA | acesso infrequente, recuperação rápida | menor armazenamento, cobra por recuperação |
| S3 One Zone-IA | infrequente, tolera perda de 1 AZ | ainda mais barato |
| S3 Glacier Instant/Flexible/Deep Archive | arquivamento, backup, compliance | muito mais barato, recuperação mais lenta |
| S3 Intelligent-Tiering | padrão de acesso desconhecido/variável | move automaticamente entre camadas |

## Uso no projeto: imagens de produto

O `catalog-service` usa o bucket `ecommerce-product-images` (criado no
provisionamento local em
`infra/localstack/init/init-aws.sh`) para guardar as fotos de produto. O
bucket **nunca é público** — todo acesso passa por uma URL pré-assinada
(presigned URL), gerada em
`services/catalog-service/src/main/java/.../service/ProductImageService.java`:

```java
@CircuitBreaker(name = "s3", fallbackMethod = "fallbackSignedUrl")
@Retry(name = "s3")
public String createReadUrl(String imageKey) {
    URL url = s3Template.createSignedGetURL(bucketName, imageKey, Duration.ofMinutes(15));
    return url.toString();
}
```

`S3Template` é a abstração de alto nível do **Spring Cloud AWS** sobre o
`S3Client`/`S3Presigner` do SDK — sem ela, você teria que montar
manualmente um `GetObjectPresignRequest` e um `S3Presigner`.

### Padrão de upload direto (cloud native)

Em vez de o cliente enviar a imagem binária para o `catalog-service` (que
depois a reenviaria ao S3 — consumindo CPU/memória/banda à toa), o serviço
apenas **assina uma URL de PUT** e devolve ao cliente
(`POST /api/v1/products/{id}/image-upload-url`, ver `ProductController.java`
e `ProductImageService#createUploadUrl`). O navegador/app então faz o
upload **diretamente** para o S3. Esse padrão:

- Escala o upload de arquivos independentemente da capacidade do
  microsserviço;
- Reduz custo de compute (o binário nunca passa pela sua aplicação);
- É exatamente o padrão que a AWS recomenda e que a prova cobra como
  "melhor prática de upload de arquivos grandes".

## Boas práticas de segurança em S3

- **Bloquear acesso público por padrão** (S3 Block Public Access) — a
  menos que o bucket seja explicitamente um site estático público.
  Habilitado por padrão em contas novas, e é assim que tratamos o bucket
  de imagens: privado, sempre via URL assinada.
- **Criptografia em repouso** com SSE-S3 (chave gerenciada pela AWS) ou
  SSE-KMS (chave gerenciada por você, com auditoria via CloudTrail).
- **Versionamento** para proteger contra sobrescrita/exclusão acidental.
- **Políticas de bucket + IAM** — controle de acesso em duas camadas.

## Para o exame

- S3 é um serviço **regional**, mas o nome do bucket é **globalmente
  único** em toda a AWS.
- Consistência: desde dezembro/2020, S3 oferece **strong read-after-write
  consistency** para todas as operações (PUTs e DELETEs) — pegadinha
  comum em provas antigas que mencionavam consistência eventual.
  A prova atual (SAA-C03) já assume consistência forte.
  entradas de blog/materiais desatualizados ainda falam em "eventual
  consistency" — desconsidere.
- Lifecycle Rules automatizam a transição entre classes de armazenamento
  (ex.: mover para Glacier após 90 dias) — forma automática de reduzir
  custo, tema do [Capítulo 16](16-custos-billing-finops.md).
- Presigned URLs herdam a permissão de quem as gerou e têm expiração
  configurável — não são um mecanismo de autenticação por si só.

---
**Anterior:** [← 4. Computo](04-computo-ec2-ecs-lambda.md) | **Próximo:** [6. Banco Relacional: Amazon RDS →](06-banco-relacional-rds.md)
