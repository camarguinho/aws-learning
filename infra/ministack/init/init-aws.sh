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
set -eu

echo "==> [S3] Criando bucket de imagens de produto"
aws s3 mb s3://ecommerce-product-images

echo "==> [DynamoDB] Criando tabela de catálogo de produtos"
# Nome "Product" (singular) porque o DynamoDbTemplate do Spring Cloud AWS
# resolve o nome da tabela pelo simple name da classe anotada @DynamoDbBean.
aws dynamodb create-table \
  --table-name Product \
  --attribute-definitions AttributeName=productId,AttributeType=S \
  --key-schema AttributeName=productId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST

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
aws secretsmanager create-secret \
  --name /ecommerce/order-service/db-credentials \
  --secret-string '{"username":"orders_app","password":"orders_app_pwd"}'

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
