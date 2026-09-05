package br.com.camarguinho.awslearning.catalog.web;

import br.com.camarguinho.awslearning.catalog.service.ProductService;
import br.com.camarguinho.awslearning.catalog.web.dto.ImageUploadUrlResponse;
import br.com.camarguinho.awslearning.catalog.web.dto.ProductRequest;
import br.com.camarguinho.awslearning.catalog.web.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST do catálogo de produtos. Documentada automaticamente no
 * Swagger UI em {@code /swagger-ui.html} via springdoc-openapi.
 */
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Catálogo de produtos (Amazon DynamoDB + S3 + ElastiCache)")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Lista todos os produtos do catálogo")
    public List<ProductResponse> findAll() {
        return productService.findAll();
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Busca um produto por id (lê do cache Redis quando disponível)")
    public ProductResponse findById(@PathVariable String productId) {
        return productService.findById(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo produto")
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Atualiza um produto existente e invalida o cache")
    public ProductResponse update(@PathVariable String productId, @Valid @RequestBody ProductRequest request) {
        return productService.update(productId, request);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um produto do catálogo")
    public void delete(@PathVariable String productId) {
        productService.delete(productId);
    }

    @PostMapping("/{productId}/image-upload-url")
    @Operation(summary = "Gera uma URL pré-assinada do S3 para upload direto da imagem do produto")
    public ResponseEntity<ImageUploadUrlResponse> requestImageUploadUrl(@PathVariable String productId) {
        String url = productService.requestImageUploadUrl(productId);
        return ResponseEntity.ok(new ImageUploadUrlResponse(url));
    }
}
