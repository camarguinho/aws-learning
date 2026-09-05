package br.com.camarguinho.awslearning.catalog.repository;

import br.com.camarguinho.awslearning.catalog.domain.Product;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.List;
import java.util.Optional;

/**
 * Acesso à tabela DynamoDB {@code Products} usando o {@link DynamoDbTemplate}
 * do Spring Cloud AWS — a camada de abstração equivalente ao
 * {@code JdbcTemplate}/{@code JpaRepository}, mas para o cliente "enhanced"
 * do DynamoDB. O template já resolve a tabela pelo nome anotado em
 * {@code @DynamoDbBean} (por convenção, o nome da classe: {@code Product});
 * o nome real da tabela é configurado em {@code application.yml}.
 */
@Repository
public class ProductRepository {

    private final DynamoDbTemplate dynamoDbTemplate;

    public ProductRepository(DynamoDbTemplate dynamoDbTemplate) {
        this.dynamoDbTemplate = dynamoDbTemplate;
    }

    public Product save(Product product) {
        return dynamoDbTemplate.save(product);
    }

    public Optional<Product> findById(String productId) {
        Key key = Key.builder().partitionValue(productId).build();
        return Optional.ofNullable(dynamoDbTemplate.load(key, Product.class));
    }

    public List<Product> findAll() {
        return dynamoDbTemplate.scanAll(Product.class).items().stream().toList();
    }

    public void deleteById(String productId) {
        Key key = Key.builder().partitionValue(productId).build();
        dynamoDbTemplate.delete(key, Product.class);
    }
}
