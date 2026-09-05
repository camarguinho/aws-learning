#!/bin/bash
# Executado automaticamente pelo LocalStack (hook ready.d) assim que o container sobe.
# Provisiona, via AWS CLI apontando para o LocalStack, os recursos que a aplicação
# espera encontrar: bucket S3, tabela DynamoDB, filas/tópicos SQS/SNS, segredo no
# Secrets Manager, parâmetros no SSM Parameter Store e identidade verificada no SES.
set -euo pipefail

export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1
ENDPOINT="http://localhost:4566"

awslocal() { aws --endpoint-url="$ENDPOINT" "$@"; }

echo "==> [S3] Criando bucket de imagens de produto"
awslocal s3 mb s3://ecommerce-product-images

echo "==> [DynamoDB] Criando tabela de catálogo de produtos"
# Nome "Product" (singular) porque o DynamoDbTemplate do Spring Cloud AWS
# resolve o nome da tabela pelo simple name da classe anotada @DynamoDbBean
# (br.com.camarguinho.awslearning.catalog.domain.Product) por padrão.
awslocal dynamodb create-table \
  --table-name Product \
  --attribute-definitions AttributeName=productId,AttributeType=S \
  --key-schema AttributeName=productId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST

echo "==> [SQS] Criando fila de eventos de pedido + DLQ"
awslocal sqs create-queue --queue-name order-events-dlq
awslocal sqs create-queue --queue-name order-events-queue

echo "==> [SNS] Criando tópico de notificações de pedido"
awslocal sns create-topic --name order-notifications-topic

echo "==> [SNS->SQS] Inscrevendo a fila no tópico (fan-out)"
TOPIC_ARN=$(awslocal sns list-topics --query "Topics[?ends_with(TopicArn, ':order-notifications-topic')].TopicArn" --output text)
QUEUE_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url "$ENDPOINT/000000000000/order-events-queue" \
  --attribute-names QueueArn --query "Attributes.QueueArn" --output text)
awslocal sns subscribe --topic-arn "$TOPIC_ARN" --protocol sqs --notification-endpoint "$QUEUE_ARN" \
  --attributes '{"RawMessageDelivery":"true"}'

echo "==> [Secrets Manager] Criando segredo com credenciais do RDS"
awslocal secretsmanager create-secret \
  --name /ecommerce/order-service/db-credentials \
  --secret-string '{"username":"orders_app","password":"orders_app_pwd"}'

echo "==> [SSM Parameter Store] Criando parâmetros de configuração"
awslocal ssm put-parameter --name /ecommerce/order-service/db-url \
  --value "jdbc:postgresql://postgres:5432/orders" --type String --overwrite
awslocal ssm put-parameter --name /ecommerce/catalog-service/cache-ttl-seconds \
  --value "300" --type String --overwrite
awslocal ssm put-parameter --name /ecommerce/notification-service/from-email \
  --value "no-reply@ecommerce.example.com" --type String --overwrite

echo "==> [SES] Verificando identidade de e-mail remetente (sandbox)"
awslocal ses verify-email-identity --email-address no-reply@ecommerce.example.com

echo "==> Provisionamento LocalStack concluído com sucesso."
