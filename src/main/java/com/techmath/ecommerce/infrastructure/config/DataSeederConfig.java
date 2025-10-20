package com.techmath.ecommerce.infrastructure.config;

import com.techmath.ecommerce.application.usecases.PayOrderUseCase;
import com.techmath.ecommerce.domain.entities.Order;
import com.techmath.ecommerce.domain.entities.Product;
import com.techmath.ecommerce.domain.entities.User;
import com.techmath.ecommerce.domain.repositories.OrderRepository;
import com.techmath.ecommerce.domain.repositories.ProductRepository;
import com.techmath.ecommerce.domain.repositories.UserRepository;
import com.techmath.ecommerce.infrastructure.search.services.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeederConfig {

    private final PayOrderUseCase payOrderUseCase;

    @Bean
    @Profile("!test")
    public CommandLineRunner dataSeeder(
            ProductRepository productRepository,
            ProductSearchService productSearchService,
            UserRepository userRepository,
            OrderRepository orderRepository,
            PayOrderUseCase payOrderUseCase
    ) {
        return args -> {
            if (productRepository.count() > 0) {
                log.info("Database already contains products. Skipping data seeding.");
                return;
            }

            log.info("Starting data seeding...");

            List<Product> products = Arrays.asList(
                    // Electronics
                    Product.builder()
                            .name("Samsung Galaxy S24 Ultra")
                            .description("Flagship smartphone with 200MP camera, S Pen, and AI features")
                            .price(BigDecimal.valueOf(1299.99))
                            .category("Electronics")
                            .stockQuantity(200)
                            .build(),

                    Product.builder()
                            .name("Apple MacBook Pro 16\"")
                            .description("M3 Max chip, 36GB RAM, 1TB SSD - Professional laptop")
                            .price(BigDecimal.valueOf(3499.99))
                            .category("Electronics")
                            .stockQuantity(200)
                            .build(),

                    Product.builder()
                            .name("Sony WH-1000XM5")
                            .description("Industry-leading noise canceling wireless headphones")
                            .price(BigDecimal.valueOf(399.99))
                            .category("Electronics")
                            .stockQuantity(200)
                            .build(),

                    Product.builder()
                            .name("iPad Pro 12.9\" M2")
                            .description("Ultimate iPad experience with M2 chip and Liquid Retina XDR display")
                            .price(BigDecimal.valueOf(1099.99))
                            .category("Electronics")
                            .stockQuantity(200)
                            .build(),

                    Product.builder()
                            .name("LG OLED C3 65\"")
                            .description("4K Smart TV with AI ThinQ and webOS")
                            .price(BigDecimal.valueOf(1799.99))
                            .category("Electronics")
                            .stockQuantity(200)
                            .build(),

                    // Home & Garden
                    Product.builder()
                            .name("Dyson V15 Detect")
                            .description("Cordless vacuum cleaner with laser dust detection")
                            .price(BigDecimal.valueOf(649.99))
                            .category("Home & Garden")
                            .stockQuantity(200)
                            .build(),

                    Product.builder()
                            .name("Nespresso Vertuo Next")
                            .description("Coffee and espresso machine with Centrifusion technology")
                            .price(BigDecimal.valueOf(179.99))
                            .category("Home & Garden")
                            .stockQuantity(200)
                            .build(),

                    Product.builder()
                            .name("iRobot Roomba j7+")
                            .description("Self-emptying robot vacuum with obstacle avoidance")
                            .price(BigDecimal.valueOf(799.99))
                            .category("Home & Garden")
                            .stockQuantity(200)
                            .build(),

                    // Sports & Outdoors
                    Product.builder()
                            .name("Peloton Bike+")
                            .description("Indoor cycling bike with rotating HD touchscreen")
                            .price(BigDecimal.valueOf(2495.00))
                            .category("Sports & Outdoors")
                            .stockQuantity(200)
                            .build(),

                    Product.builder()
                            .name("Hydro Flask 32oz")
                            .description("Insulated stainless steel water bottle - keeps drinks cold 24h")
                            .price(BigDecimal.valueOf(44.95))
                            .category("Sports & Outdoors")
                            .stockQuantity(200)
                            .build(),

                    Product.builder()
                            .name("Garmin Fenix 7X Solar")
                            .description("Premium multisport GPS watch with solar charging")
                            .price(BigDecimal.valueOf(899.99))
                            .category("Sports & Outdoors")
                            .stockQuantity(200)
                            .build(),

                    // Books
                    Product.builder()
                            .name("Atomic Habits by James Clear")
                            .description("Proven framework for improving every day")
                            .price(BigDecimal.valueOf(27.00))
                            .category("Books")
                            .stockQuantity(200)
                            .build(),

                    Product.builder()
                            .name("Clean Code by Robert Martin")
                            .description("A handbook of agile software craftsmanship")
                            .price(BigDecimal.valueOf(42.99))
                            .category("Books")
                            .stockQuantity(200)
                            .build(),

                    Product.builder()
                            .name("The Pragmatic Programmer")
                            .description("Your journey to mastery, 20th Anniversary Edition")
                            .price(BigDecimal.valueOf(49.99))
                            .category("Books")
                            .stockQuantity(200)
                            .build(),

                    // Fashion
                    Product.builder()
                            .name("Nike Air Max 270")
                            .description("Men's lifestyle shoes with Max Air cushioning")
                            .price(BigDecimal.valueOf(150.00))
                            .category("Fashion")
                            .stockQuantity(200)
                            .build(),

                    Product.builder()
                            .name("Levi's 501 Original Jeans")
                            .description("Classic straight fit jeans")
                            .price(BigDecimal.valueOf(98.00))
                            .category("Fashion")
                            .stockQuantity(200)
                            .build(),

                    Product.builder()
                            .name("Ray-Ban Aviator Classic")
                            .description("Iconic sunglasses with 100% UV protection")
                            .price(BigDecimal.valueOf(163.00))
                            .category("Fashion")
                            .stockQuantity(200)
                            .build(),

                    // Gaming
                    Product.builder()
                            .name("PlayStation 5")
                            .description("Next-gen gaming console with 4K gaming at 120fps")
                            .price(BigDecimal.valueOf(499.99))
                            .category("Gaming")
                            .stockQuantity(200)
                            .build(),

                    Product.builder()
                            .name("Xbox Series X")
                            .description("Most powerful Xbox ever with 12 teraflops GPU")
                            .price(BigDecimal.valueOf(499.99))
                            .category("Gaming")
                            .stockQuantity(200)
                            .build(),

                    Product.builder()
                            .name("Nintendo Switch OLED")
                            .description("Handheld gaming console with vibrant 7-inch OLED screen")
                            .price(BigDecimal.valueOf(349.99))
                            .category("Gaming")
                            .stockQuantity(200)
                            .build(),

                    Product.builder()
                            .name("Logitech G Pro X Superlight")
                            .description("Wireless gaming mouse under 63 grams")
                            .price(BigDecimal.valueOf(159.99))
                            .category("Gaming")
                            .stockQuantity(200)
                            .build()
            );

            List<Product> savedProducts = productRepository.saveAll(products);
            log.info("Saved {} products to database", savedProducts.size());

            // Sync with Elasticsearch
            savedProducts.forEach(product -> {
                try {
                    productSearchService.syncProduct(product);
                    log.debug("Synced product {} to Elasticsearch", product.getName());
                } catch (Exception e) {
                    log.error("Failed to sync product {} to Elasticsearch: {}",
                            product.getName(), e.getMessage());
                }
            });

            log.info("Data seeding completed successfully! {} products synced to Elasticsearch",
                    savedProducts.size());

            // Create sample orders for testing reports
            createSampleOrders(userRepository, orderRepository, savedProducts, payOrderUseCase);
        };
    }

    private void createSampleOrders(
            UserRepository userRepository,
            OrderRepository orderRepository,
            List<Product> products,
            PayOrderUseCase payOrderUseCase
    ) {
        log.info("Creating sample orders for testing...");

        User admin = userRepository.findByEmail("admin@ecommerce.com")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        User regularUser = userRepository.findByEmail("user@ecommerce.com")
                .orElseThrow(() -> new RuntimeException("Regular user not found"));

        Random random = new Random();
        int totalOrders = 0;

        // Orders for admin user - high-value customer (15 orders)
        for (int i = 0; i < 15; i++) {
            Order order = new Order();
            setAuthenticatedUser(admin);

            // Add 2-4 random products to each order
            int itemCount = 2 + random.nextInt(3);
            for (int j = 0; j < itemCount; j++) {
                Product randomProduct = products.get(random.nextInt(products.size()));
                int quantity = 1 + random.nextInt(3);
                order.addItem(randomProduct, quantity);
            }

            order = orderRepository.save(order);
            payOrderUseCase.execute(order.getId());
            totalOrders++;
        }

        // Orders for regular user - medium-value customer (8 orders)
        for (int i = 0; i < 8; i++) {
            Order order = new Order();
            setAuthenticatedUser(regularUser);

            // Add 1-3 random products to each order
            int itemCount = 1 + random.nextInt(3);
            for (int j = 0; j < itemCount; j++) {
                Product randomProduct = products.get(random.nextInt(products.size()));
                int quantity = 1 + random.nextInt(2);
                order.addItem(randomProduct, quantity);
            }

            order = orderRepository.save(order);
            payOrderUseCase.execute(order.getId());
            totalOrders++;
        }

        for (int i = 0; i < 5; i++) {
            User user = random.nextBoolean() ? admin : regularUser;

            Order order = new Order();
            setAuthenticatedUser(user);

            // Add products to order
            int itemCount = 1 + random.nextInt(4);
            for (int j = 0; j < itemCount; j++) {
                Product randomProduct = products.get(random.nextInt(products.size()));
                int quantity = 1 + random.nextInt(3);
                order.addItem(randomProduct, quantity);
            }

            order = orderRepository.save(order);
            payOrderUseCase.execute(order.getId());
            totalOrders++;
        }

        // Create some pending orders (not paid yet)
        for (int i = 0; i < 3; i++) {
            User user = random.nextBoolean() ? admin : regularUser;

            Order order = Order.builder()
                    .user(user)
                    .build();

            int itemCount = 1 + random.nextInt(3);
            for (int j = 0; j < itemCount; j++) {
                Product randomProduct = products.get(random.nextInt(products.size()));
                int quantity = 1 + random.nextInt(2);
                order.addItem(randomProduct, quantity);
            }

            orderRepository.save(order);
            totalOrders++;
        }

        SecurityContextHolder.clearContext();

        log.info("Created {} sample orders ({} paid, {} pending)",
                totalOrders, totalOrders - 3, 3);
        log.info("Sample data ready! You can now test:");
        log.info("  - GET /api/v1/reports/top-buyers");
        log.info("  - GET /api/v1/reports/average-ticket");
        log.info("  - GET /api/v1/reports/current-month-revenue");
        log.info("  - GET /api/v1/reports/revenue?startDate=2024-07-01&endDate=2025-01-31");
    }

    private void setAuthenticatedUser(User user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
