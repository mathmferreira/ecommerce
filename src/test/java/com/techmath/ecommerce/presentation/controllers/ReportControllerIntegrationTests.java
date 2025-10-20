package com.techmath.ecommerce.presentation.controllers;

import com.techmath.ecommerce.domain.entities.Order;
import com.techmath.ecommerce.domain.entities.User;
import com.techmath.ecommerce.domain.enums.OrderStatus;
import com.techmath.ecommerce.domain.enums.UserRole;
import com.techmath.ecommerce.domain.repositories.OrderRepository;
import com.techmath.ecommerce.domain.repositories.UserRepository;
import com.techmath.ecommerce.infrastructure.search.repositories.ProductSearchRepository;
import com.techmath.ecommerce.infrastructure.search.services.ProductSearchService;
import com.techmath.ecommerce.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ReportController - Integration Tests")
public class ReportControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

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

    @MockitoBean
    public AuditorAware<User> auditorAware;

    private String adminToken;
    private String userToken;
    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userRepository.deleteAll();

        User admin = User.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("admin123"))
                .name("Admin Test")
                .role(UserRole.ADMIN)
                .active(true)
                .build();
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of(admin));
        admin = userRepository.save(admin);
        adminToken = jwtService.generateToken(admin);

        User user = User.builder()
                .email("user@test.com")
                .password(passwordEncoder.encode("user123"))
                .name("User Test")
                .role(UserRole.USER)
                .active(true)
                .build();
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of(user));
        user = userRepository.save(user);
        userToken = jwtService.generateToken(user);

        testUser1 = User.builder()
                .email("buyer1@test.com")
                .password(passwordEncoder.encode("pass"))
                .name("Buyer One")
                .role(UserRole.USER)
                .active(true)
                .build();
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of(testUser1));
        testUser1 = userRepository.save(testUser1);

        testUser2 = User.builder()
                .email("buyer2@test.com")
                .password(passwordEncoder.encode("pass"))
                .name("Buyer Two")
                .role(UserRole.USER)
                .active(true)
                .build();
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of(testUser2));
        testUser2 = userRepository.save(testUser2);

        createPaidOrder(testUser1, BigDecimal.valueOf(100.00));
        createPaidOrder(testUser1, BigDecimal.valueOf(200.00));
        createPaidOrder(testUser2, BigDecimal.valueOf(150.00));
    }

    private void createPaidOrder(User user, BigDecimal amount) {
        var order = Order.builder()
                .user(user)
                .status(OrderStatus.PAID)
                .totalAmount(amount)
                .build();
        orderRepository.save(order);
    }

    @Test
    @DisplayName("GET /api/v1/reports/top-buyers - Should return top buyers")
    void shouldReturnTopBuyers() throws Exception {
        for (int i = 0; i < 10; i++) {
            User user = User.builder()
                    .email("buyer" + i + "@testtopbuyers.com")
                    .password(passwordEncoder.encode("pass"))
                    .name("Buyer " + i)
                    .role(UserRole.USER)
                    .active(true)
                    .build();
            when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of(user));
            user = userRepository.save(user);
            createPaidOrder(user, BigDecimal.valueOf(100.00));
        }

        mockMvc.perform(get("/api/v1/reports/top-buyers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].userName").exists())
                .andExpect(jsonPath("$[0].totalOrders").exists())
                .andExpect(jsonPath("$[0].totalSpent").exists());
    }

    @Test
    @DisplayName("GET /api/v1/reports/top-buyers - Should limit to 5 results")
    void shouldLimitTop5Buyers() throws Exception {
        for (int i = 0; i < 10; i++) {
            User user = User.builder()
                    .email("buyer" + i + "@testtopbuyers.com")
                    .password(passwordEncoder.encode("pass"))
                    .name("Buyer " + i)
                    .role(UserRole.USER)
                    .active(true)
                    .build();
            when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of(user));
            user = userRepository.save(user);
            createPaidOrder(user, BigDecimal.valueOf(100.00));
        }

        mockMvc.perform(get("/api/v1/reports/top-buyers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(5))));
    }

    @Test
    @DisplayName("GET /api/v1/reports/top-buyers - Should filter by date range")
    void shouldFilterTopBuyersByDateRange() throws Exception {
        mockMvc.perform(get("/api/v1/reports/top-buyers")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/reports/average-ticket - Should return average ticket")
    void shouldReturnAverageTicket() throws Exception {
        for (int i = 0; i < 10; i++) {
            User user = User.builder()
                    .email("buyer" + i + "@testtopbuyers.com")
                    .password(passwordEncoder.encode("pass"))
                    .name("Buyer " + i)
                    .role(UserRole.USER)
                    .active(true)
                    .build();
            when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of(user));
            user = userRepository.save(user);
            createPaidOrder(user, BigDecimal.valueOf(100.00));
        }

        mockMvc.perform(get("/api/v1/reports/average-ticket")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].userName").exists())
                .andExpect(jsonPath("$[0].averageTicket").exists());
    }

    @Test
    @DisplayName("GET /api/v1/reports/current-month-revenue - Should return current month revenue")
    void shouldReturnCurrentMonthRevenue() throws Exception {
        mockMvc.perform(get("/api/v1/reports/current-month-revenue")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").exists())
                .andExpect(jsonPath("$.year").exists())
                .andExpect(jsonPath("$.totalRevenue").exists())
                .andExpect(jsonPath("$.totalRevenue").isNumber());
    }

    @Test
    @DisplayName("GET /api/v1/reports/revenue - Should return revenue for custom period")
    void shouldReturnRevenueForCustomPeriod() throws Exception {
        mockMvc.perform(get("/api/v1/reports/revenue")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("CUSTOM_PERIOD"))
                .andExpect(jsonPath("$.totalRevenue").exists());
    }

    @Test
    @DisplayName("GET /api/v1/reports/revenue - Should require date parameters")
    void shouldRequireDateParameters() throws Exception {
        mockMvc.perform(get("/api/v1/reports/revenue")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/reports/top-buyers - Should return 403 when USER tries to access")
    void shouldReturn403WhenUserTriesToAccessReports() throws Exception {
        mockMvc.perform(get("/api/v1/reports/top-buyers")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/reports/average-ticket - Should return 403 for USER")
    void shouldReturn403ForUserAccessingAverageTicket() throws Exception {
        mockMvc.perform(get("/api/v1/reports/average-ticket")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/reports/current-month-revenue - Should return 403 for USER")
    void shouldReturn403ForUserAccessingRevenue() throws Exception {
        mockMvc.perform(get("/api/v1/reports/current-month-revenue")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/reports/top-buyers - Should return 401 when no token")
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/reports/top-buyers"))
                .andExpect(status().isUnauthorized());
    }

}
