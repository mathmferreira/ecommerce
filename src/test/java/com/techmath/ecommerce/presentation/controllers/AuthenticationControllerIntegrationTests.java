package com.techmath.ecommerce.presentation.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmath.ecommerce.domain.entities.User;
import com.techmath.ecommerce.domain.enums.UserRole;
import com.techmath.ecommerce.domain.repositories.UserRepository;
import com.techmath.ecommerce.infrastructure.messaging.producers.OrderEventProducer;
import com.techmath.ecommerce.infrastructure.search.repositories.ProductSearchRepository;
import com.techmath.ecommerce.infrastructure.search.services.ProductSearchService;
import com.techmath.ecommerce.presentation.dto.request.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthenticationController - Integration Tests")
class AuthenticationControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private OrderEventProducer orderEventProducer;

    @MockitoBean
    private ProductSearchService productSearchService;

    @MockitoBean
    private ProductSearchRepository productSearchRepository;

    @MockitoBean
    private ElasticsearchOperations elasticsearchOperations;

    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        adminUser = User.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("admin123"))
                .name("Admin Test")
                .role(UserRole.ADMIN)
                .active(true)
                .build();
        adminUser = userRepository.save(adminUser);

        regularUser = User.builder()
                .email("user@test.com")
                .password(passwordEncoder.encode("user123"))
                .name("User Test")
                .role(UserRole.USER)
                .active(true)
                .build();
        regularUser = userRepository.save(regularUser);
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should authenticate admin successfully")
    void shouldAuthenticateAdminSuccessfully() throws Exception {
        var loginRequest = LoginRequest.builder()
                .email("admin@test.com")
                .password("admin123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("admin@test.com"))
                .andExpect(jsonPath("$.name").value("Admin Test"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should authenticate regular user successfully")
    void shouldAuthenticateRegularUserSuccessfully() throws Exception {
        var loginRequest = LoginRequest.builder()
                .email("user@test.com")
                .password("user123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.name").value("User Test"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should fail with invalid credentials")
    void shouldFailWithInvalidCredentials() throws Exception {
        var loginRequest = LoginRequest.builder()
                .email("admin@test.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should fail with non-existent user")
    void shouldFailWithNonExistentUser() throws Exception {
        var loginRequest = LoginRequest.builder()
                .email("nonexistent@test.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should fail with invalid email format")
    void shouldFailWithInvalidEmailFormat() throws Exception {
        var loginRequest = LoginRequest.builder()
                .email("invalid-email")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should fail with empty email")
    void shouldFailWithEmptyEmail() throws Exception {
        var loginRequest = LoginRequest.builder()
                .email("")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should fail with empty password")
    void shouldFailWithEmptyPassword() throws Exception {
        var loginRequest = LoginRequest.builder()
                .email("admin@test.com")
                .password("")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should fail with null credentials")
    void shouldFailWithNullCredentials() throws Exception {
        var loginRequest = LoginRequest.builder()
                .email(null)
                .password(null)
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should fail with inactive user")
    void shouldFailWithInactiveUser() throws Exception {
        var inactiveUser = User.builder()
                .email("inactive@test.com")
                .password(passwordEncoder.encode("password123"))
                .name("Inactive User")
                .role(UserRole.USER)
                .active(false)
                .build();
        userRepository.save(inactiveUser);

        var loginRequest = LoginRequest.builder()
                .email("inactive@test.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

}
