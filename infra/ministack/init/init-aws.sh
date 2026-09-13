#!/bin/sh
# Hook "ready.d" nativo do MiniStack: este script roda automaticamente,
# DENTRO do próprio container do emulador, assim que a API fica pronta para
# receber requisições (mesma convenção herdada do LocalStack). Provisiona os
# recursos AWS usados pelo case de e-commerce.
#
# AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_DEFAULT_REGION e
# AWS_ENDPOINT_URL já vêm injetados automaticamente pelo MiniStack neste
# script — nenhuma configuração manual de credenciais/endpoint é necessária,
# e a CLI "aws" já vem embutida na imagem.
#
# O SQS, porém, ignora a env var genérica AWS_ENDPOINT_URL: em us-east-1 o
# botocore resolve o endpoint legado global do serviço (queue.amazonaws.com)
# e a chamada acaba indo pra AWS real, que rejeita a credencial de teste com
# "InvalidClientTokenId" (S3 e DynamoDB não têm esse problema — só o SQS).
# Por isso toda chamada aqui usa "aws" via este wrapper, que passa
# --endpoint-url explicitamente e funciona de forma consistente pra
# qualquer serviço, independente dessa particularidade do SQS.
aws() { command aws --endpoint-url "$AWS_ENDPOINT_URL" "$@"; }

set -eu

echo "==> [S3] Criando bucket de imagens de produto"
aws s3 mb s3://ecommerce-product-images

echo "==> [DynamoDB] Criando tabela de catálogo de produtos"
# Nome "product" (minúsculo, singular) porque o DynamoDbTemplate do Spring
# Cloud AWS NÃO usa o simple name da classe anotada @DynamoDbBean como está;
# o DefaultDynamoDbTableNameResolver converte para snake_case minúsculo
# ("Product" -> "product") antes de resolver o nome da tabela.
# create-table não é idempotente (ResourceInUseException se já existir) — e
# com PERSIST_STATE=1 a tabela sobrevive a um restart do container, então o
# ready.d roda de novo sobre estado que já tem a tabela.
if aws dynamodb describe-table --table-name product >/dev/null 2>&1; then
  echo "    tabela product já existe (estado persistido) — pulando"
else
  aws dynamodb create-table \
    --table-name product \
    --attribute-definitions AttributeName=productId,AttributeType=S \
    --key-schema AttributeName=productId,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST
fi

echo "==> [SQS] Criando fila de eventos de pedido + DLQ"
aws sqs create-queue --queue-name order-events-dlq
aws sqs create-queue --queue-name order-events-queue

echo "==> [SNS] Criando tópico de notificações de pedido"
aws sns create-topic --name order-notifications-topic

echo "==> [SNS->SQS] Inscrevendo a fila no tópico (fan-out, raw message delivery)"
TOPIC_ARN=$(aws sns list-topics --query "Topics[?ends_with(TopicArn, ':order-notifications-topic')].TopicArn" --output text)
QUEUE_ARN=$(aws sqs get-queue-attributes \
  --queue-url "$AWS_ENDPOINT_URL/000000000000/order-events-queue" \
  --attribute-names QueueArn --query "Attributes.QueueArn" --output text)
aws sns subscribe --topic-arn "$TOPIC_ARN" --protocol sqs --notification-endpoint "$QUEUE_ARN" \
  --attributes '{"RawMessageDelivery":"true"}'

echo "==> [Secrets Manager] Criando segredo com credenciais do RDS"
# Mesmo motivo do guard do DynamoDB acima: create-secret não é idempotente
# (ResourceExistsException) e o segredo sobrevive a um restart com estado
# persistido.
if aws secretsmanager describe-secret --secret-id /ecommerce/order-service/db-credentials >/dev/null 2>&1; then
  echo "    segredo já existe (estado persistido) — pulando"
else
  aws secretsmanager create-secret \
    --name /ecommerce/order-service/db-credentials \
    --secret-string '{"username":"orders_app","password":"orders_app_pwd"}'
fi

echo "==> [SSM Parameter Store] Criando parâmetros de configuração"
aws ssm put-parameter --name /ecommerce/order-service/db-url \
  --value "jdbc:postgresql://postgres:5432/orders" --type String --overwrite
aws ssm put-parameter --name /ecommerce/catalog-service/cache-ttl-seconds \
  --value "300" --type String --overwrite
aws ssm put-parameter --name /ecommerce/notification-service/from-email \
  --value "no-reply@ecommerce.example.com" --type String --overwrite

echo "==> [SES] Verificando identidade de e-mail remetente (sandbox)"
aws ses verify-email-identity --email-address no-reply@ecommerce.example.com

echo "==> Provisionamento no MiniStack concluído com sucesso."
