package br.com.camarguinho.awslearning.catalog.service;

import br.com.camarguinho.awslearning.catalog.domain.Product;
import br.com.camarguinho.awslearning.catalog.repository.ProductRepository;
import br.com.camarguinho.awslearning.catalog.web.dto.ProductRequest;
import br.com.camarguinho.awslearning.catalog.web.dto.ProductResponse;
import br.com.camarguinho.awslearning.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageService productImageService;

    @InjectMocks
    private ProductService productService;

    @Test
    void createShouldPersistProductWithGeneratedId() {
        var request = new ProductRequest("Mouse Gamer", "Mouse 16000 DPI", new BigDecimal("199.90"), 50);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.create(request);

        assertThat(response.productId()).isNotBlank();
        assertThat(response.name()).isEqualTo("Mouse Gamer");
        assertThat(response.stockQuantity()).isEqualTo(50);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void findByIdShouldThrowWhenProductDoesNotExist() {
        when(productRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById("unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
