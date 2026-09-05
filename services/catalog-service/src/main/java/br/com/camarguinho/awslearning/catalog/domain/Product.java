package br.com.camarguinho.awslearning.catalog.domain;

import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Representa um item da tabela DynamoDB {@code Products}.
 *
 * <p>O DynamoDB é um banco NoSQL chave-valor/documento: não existe schema
 * fixo de colunas, apenas uma chave primária obrigatória. Aqui usamos apenas
 * uma partition key ({@code productId}) porque todo acesso de leitura do
 * catálogo é feito por identificador direto — o padrão de acesso, e não a
 * normalização, é o que guia o modelo de dados no DynamoDB.</p>
 *
 * <p>O atributo {@code version} habilita <i>optimistic locking</i>: o SDK
 * (via {@link DynamoDbVersionAttribute}) rejeita uma escrita concorrente que
 * tenha lido uma versão desatualizada do item, evitando perda de atualização
 * (lost update) sem precisar de lock pessimista.</p>
 */
@DynamoDbBean
public class Product implements Serializable {

    private String productId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageKey;
    private Long version;

    public Product() {
    }

    @DynamoDbPartitionKey
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    /**
     * Chave (key) do objeto no bucket S3 de imagens, ou {@code null} se o
     * produto ainda não possui imagem cadastrada.
     */
    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    @DynamoDbVersionAttribute
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
