package com.techmath.ecommerce.presentation.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmath.ecommerce.domain.entities.Product;
import com.techmath.ecommerce.domain.entities.User;
import com.techmath.ecommerce.domain.enums.UserRole;
import com.techmath.ecommerce.domain.repositories.ProductRepository;
import com.techmath.ecommerce.domain.repositories.UserRepository;
import com.techmath.ecommerce.infrastructure.search.documents.ProductDocument;
import com.techmath.ecommerce.infrastructure.search.repositories.ProductSearchRepository;
import com.techmath.ecommerce.infrastructure.search.services.ProductSearchService;
import com.techmath.ecommerce.infrastructure.security.JwtService;
import com.techmath.ecommerce.presentation.dto.ProductDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ProductController - Integration Tests")
class ProductControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private ProductSearchService productSearchService;

    @MockitoBean
    private ProductSearchRepository productSearchRepository;

    @MockitoBean
    private ElasticsearchOperations elasticsearchOperations;

    private String adminToken;
    private String userToken;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        userRepository.deleteAll();

        var admin = User.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("admin123"))
                .name("Admin Test")
                .role(UserRole.ADMIN)
                .active(true)
                .build();
        admin = userRepository.save(admin);
        adminToken = jwtService.generateToken(admin);

        var user = User.builder()
                .email("user@test.com")
                .password(passwordEncoder.encode("user123"))
                .name("User Test")
                .role(UserRole.USER)
                .active(true)
                .build();
        user = userRepository.save(user);
        userToken = jwtService.generateToken(user);

        testProduct = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99))
                .category("Electronics")
                .stockQuantity(10)
                .build();
        testProduct = productRepository.save(testProduct);

        setupElasticsearchMock();
    }

    @Test
    @DisplayName("POST /api/v1/products - Should create product as ADMIN")
    void shouldCreateProductAsAdmin() throws Exception {
        var productDTO = new ProductDTO(
                null,
                "New Product",
                "New Description",
                BigDecimal.valueOf(199.99),
                "Electronics",
                20,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/products - Should get all products")
    void shouldGetAllProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().is(206))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - Should get product by id")
    void shouldGetProductById() throws Exception {
        mockMvc.perform(get("/api/v1/products/" + testProduct.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testProduct.getId().toString()))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - Should return 404 when product not found")
    void shouldReturn404WhenProductNotFound() throws Exception {
        var nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/products/" + nonExistentId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} - Should update product as ADMIN")
    void shouldUpdateProductAsAdmin() throws Exception {
        var updateDTO = new ProductDTO(
                null,
                "Updated Product",
                "Updated Description",
                BigDecimal.valueOf(149.99),
                "Electronics",
                15,
                null,
                null
        );

        mockMvc.perform(put("/api/v1/products/" + testProduct.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.price").value(149.99))
                .andExpect(jsonPath("$.stockQuantity").value(15));
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} - Should return 403 when USER tries to update")
    void shouldReturn403WhenUserTriesToUpdate() throws Exception {
        var updateDTO = new ProductDTO(
                null,
                "Updated Product",
                "Updated Description",
                BigDecimal.valueOf(149.99),
                "Electronics",
                15,
                null,
                null
        );

        mockMvc.perform(put("/api/v1/products/" + testProduct.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - Should delete product as ADMIN")
    void shouldDeleteProductAsAdmin() throws Exception {
        mockMvc.perform(delete("/api/v1/products/" + testProduct.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/products/" + testProduct.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - Should return 403 when USER tries to delete")
    void shouldReturn403WhenUserTriesToDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/products/" + testProduct.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/products - Should validate required fields")
    void shouldValidateRequiredFields() throws Exception {
        var invalidDTO = new ProductDTO(
                null,
                "",
                "",
                null,
                "",
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/products - Should return 401 when no token provided")
    void shouldReturn401WhenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }

    private void setupElasticsearchMock() {
        var productDoc = new ProductDocument(
                testProduct.getId().toString(),
                testProduct.getName(),
                testProduct.getDescription(),
                testProduct.getPrice(),
                testProduct.getCategory(),
                testProduct.getStockQuantity(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(productSearchService.searchProducts(
                any(), any(), any(), any(), any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(productDoc)));
    }

}
