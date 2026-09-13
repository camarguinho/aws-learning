package br.com.camarguinho.awslearning.catalog.service;

import br.com.camarguinho.awslearning.catalog.domain.Product;
import br.com.camarguinho.awslearning.catalog.repository.ProductRepository;
import br.com.camarguinho.awslearning.catalog.web.dto.ProductRequest;
import br.com.camarguinho.awslearning.catalog.web.dto.ProductResponse;
import br.com.camarguinho.awslearning.common.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Regras de negócio do catálogo de produtos.
 *
 * <p>As leituras individuais ({@link #findById(String)}) são cacheadas no
 * Amazon ElastiCache (Redis) via anotação {@code @Cacheable} — padrão
 * <i>cache-aside</i>: a aplicação primeiro consulta o cache; em caso de
 * miss, busca no DynamoDB e popula o cache para as próximas leituras.
 * Qualquer escrita invalida a entrada correspondente com
 * {@code @CacheEvict}, garantindo que o cliente nunca veja dado desatualizado
 * além do TTL configurado.</p>
 */
@Service
public class ProductService {

    private static final String CACHE_NAME = "products";

    private final ProductRepository productRepository;
    private final ProductImageService productImageService;

    public ProductService(ProductRepository productRepository, ProductImageService productImageService) {
        this.productRepository = productRepository;
        this.productImageService = productImageService;
    }

    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        product.setProductId(UUID.randomUUID().toString());
        applyRequest(product, request);
        return toResponse(productRepository.save(product));
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "#productId")
    public ProductResponse update(String productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + productId));
        applyRequest(product, request);
        return toResponse(productRepository.save(product));
    }

    @Cacheable(cacheNames = CACHE_NAME, key = "#productId")
    public ProductResponse findById(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + productId));
        return toResponse(product);
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "#productId")
    public void delete(String productId) {
        productRepository.deleteById(productId);
    }

    /** Gera uma URL pré-assinada de upload (S3) para a imagem deste produto. */
    @CacheEvict(cacheNames = CACHE_NAME, key = "#productId")
    public String requestImageUploadUrl(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + productId));
        String imageKey = "products/%s/%s.jpg".formatted(productId, UUID.randomUUID());
        product.setImageKey(imageKey);
        productRepository.save(product);
        return productImageService.createUploadUrl(imageKey);
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
    }

    private ProductResponse toResponse(Product product) {
        String imageUrl = productImageService.createReadUrl(product.getImageKey());
        return new ProductResponse(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                imageUrl);
    }
}
