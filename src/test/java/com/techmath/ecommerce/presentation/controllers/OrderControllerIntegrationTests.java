package com.techmath.ecommerce.presentation.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmath.ecommerce.config.KafkaTestConfig;
import com.techmath.ecommerce.domain.entities.Order;
import com.techmath.ecommerce.domain.entities.Product;
import com.techmath.ecommerce.domain.entities.User;
import com.techmath.ecommerce.domain.enums.OrderStatus;
import com.techmath.ecommerce.domain.enums.UserRole;
import com.techmath.ecommerce.domain.repositories.OrderRepository;
import com.techmath.ecommerce.domain.repositories.ProductRepository;
import com.techmath.ecommerce.domain.repositories.UserRepository;
import com.techmath.ecommerce.infrastructure.search.repositories.ProductSearchRepository;
import com.techmath.ecommerce.infrastructure.search.services.ProductSearchService;
import com.techmath.ecommerce.infrastructure.security.JwtService;
import com.techmath.ecommerce.presentation.dto.request.OrderItemsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(KafkaTestConfig.class)
@DisplayName("OrderController - Integration Tests")
class OrderControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

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

    private String userToken;
    private Product testProduct;
    private User testUser;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .email("user@test.com")
                .password(passwordEncoder.encode("user123"))
                .name("User Test")
                .role(UserRole.USER)
                .active(true)
                .build();
        testUser = userRepository.saveAndFlush(testUser);
        userToken = jwtService.generateToken(testUser);

        testProduct = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(100.00))
                .category("Electronics")
                .stockQuantity(10)
                .build();
        testProduct = productRepository.saveAndFlush(testProduct);
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should create order successfully")
    void shouldCreateOrderSuccessfully() throws Exception {
        var itemRequest = new OrderItemsRequest(testProduct.getId(), 2);
        var items = List.of(itemRequest);

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(items)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should cancel order when insufficient stock")
    void shouldCancelOrderWhenInsufficientStock() throws Exception {
        var itemRequest = new OrderItemsRequest(testProduct.getId(), 20);
        var items = List.of(itemRequest);

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(items)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/orders/pay/{orderId} - Should pay order successfully")
    void shouldPayOrderSuccessfully() throws Exception {
        var order = Order.builder()
                .user(testUser)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();
        order = orderRepository.save(order);

        mockMvc.perform(post("/api/v1/orders/pay/" + order.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(order.getId().toString()))
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    @DisplayName("POST /api/v1/orders/pay/{orderId} - Should return 404 when order not found")
    void shouldReturn404WhenOrderNotFoundForPayment() throws Exception {
        mockMvc.perform(post("/api/v1/orders/pay/" + java.util.UUID.randomUUID())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should validate order items")
    void shouldValidateOrderItems() throws Exception {
        var invalidItem = new OrderItemsRequest(testProduct.getId(), -1);
        var items = List.of(invalidItem);

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(items)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should return 401 when no token provided")
    void shouldReturn401WhenNoTokenProvided() throws Exception {
        var itemRequest = new OrderItemsRequest(testProduct.getId(), 2);
        var items = List.of(itemRequest);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(items)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should calculate total amount correctly")
    void shouldCalculateTotalAmountCorrectly() throws Exception {
        var itemRequest = new OrderItemsRequest(testProduct.getId(), 3);
        var items = List.of(itemRequest);

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(items)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists());

        var createdOrder = orderRepository.findAll().get(0);
        assert createdOrder.getTotalAmount().compareTo(BigDecimal.valueOf(300.00)) == 0;
    }

}
