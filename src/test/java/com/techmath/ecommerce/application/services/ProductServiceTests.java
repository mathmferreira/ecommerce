package com.techmath.ecommerce.application.services;

import com.techmath.ecommerce.domain.entities.Product;
import com.techmath.ecommerce.domain.exceptions.InsufficientStockException;
import com.techmath.ecommerce.domain.repositories.ProductRepository;
import com.techmath.ecommerce.infrastructure.search.services.ProductSearchService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService - Unit Tests")
class ProductServiceTests {

    @Mock
    private ProductRepository repository;

    @Mock
    private ProductSearchService searchService;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        product = Product.builder()
                .id(productId)
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99))
                .category("Electronics")
                .stockQuantity(10)
                .build();
    }

    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() {
        when(repository.save(any(Product.class))).thenReturn(product);
        doNothing().when(searchService).syncProduct(any(Product.class));

        var result = productService.createProduct(product);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Product");
        verify(repository, times(1)).save(product);
        verify(searchService, times(1)).syncProduct(product);
    }

    @Test
    @DisplayName("Should get product by id successfully")
    void shouldGetProductByIdSuccessfully() {
        when(repository.findById(productId)).thenReturn(Optional.of(product));

        var result = productService.getProductById(productId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        verify(repository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when product not found")
    void shouldThrowEntityNotFoundExceptionWhenProductNotFound() {
        when(repository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found with id: " + productId);
    }

    @Test
    @DisplayName("Should update product successfully")
    void shouldUpdateProductSuccessfully() {
        var updatedProduct = Product.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(BigDecimal.valueOf(199.99))
                .category("Electronics")
                .stockQuantity(20)
                .build();

        when(repository.findById(productId)).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenReturn(product);
        doNothing().when(searchService).syncProduct(any(Product.class));

        var result = productService.updateProduct(productId, updatedProduct);

        assertThat(result).isNotNull();
        verify(repository, times(1)).findById(productId);
        verify(repository, times(1)).save(any(Product.class));
        verify(searchService, times(1)).syncProduct(any(Product.class));
    }

    @Test
    @DisplayName("Should update product stock successfully")
    void shouldUpdateProductStockSuccessfully() {
        var quantityToDecrease = 5;
        when(repository.save(any(Product.class))).thenReturn(product);
        doNothing().when(searchService).syncProduct(any(Product.class));

        var result = productService.updateProductStock(product, quantityToDecrease);

        assertThat(result).isNotNull();
        assertThat(result.getStockQuantity()).isEqualTo(5);
        verify(repository, times(1)).save(product);
        verify(searchService, times(1)).syncProduct(product);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when stock is insufficient")
    void shouldThrowInsufficientStockExceptionWhenStockIsInsufficient() {
        var quantityToDecrease = 15;

        assertThatThrownBy(() -> productService.updateProductStock(product, quantityToDecrease))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock for product: Test Product");
    }

    @Test
    @DisplayName("Should delete product successfully")
    void shouldDeleteProductSuccessfully() {
        when(repository.findById(productId)).thenReturn(Optional.of(product));
        doNothing().when(repository).delete(product);

        productService.deleteProduct(productId);

        verify(repository, times(1)).findById(productId);
        verify(repository, times(1)).delete(product);
    }

    @Test
    @DisplayName("Should check if product has stock")
    void shouldCheckIfProductHasStock() {
        assertThat(product.hasStock()).isTrue();
        assertThat(product.hasStock(5)).isTrue();
        assertThat(product.hasStock(15)).isFalse();
    }

}
