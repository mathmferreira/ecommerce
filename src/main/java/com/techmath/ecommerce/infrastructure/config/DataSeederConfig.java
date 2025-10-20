package com.techmath.ecommerce.infrastructure.config;

import com.techmath.ecommerce.application.usecases.PayOrderUseCase;
import com.techmath.ecommerce.domain.entities.Order;
import com.techmath.ecommerce.domain.entities.Product;
import com.techmath.ecommerce.domain.entities.User;
import com.techmath.ecommerce.domain.repositories.OrderRepository;
import com.techmath.ecommerce.domain.repositories.ProductRepository;
import com.techmath.ecommerce.domain.repositories.UserRepository;
import com.techmath.ecommerce.infrastructure.search.services.ProductSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Slf4j
@Configuration
public class DataSeederConfig {

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
            productSearchService.ensureElasticsearchIndexExists();
            if (productRepository.count() > 0) {
                log.info("Database already contains products. Skipping data seeding.");
                return;
            }

            log.info("Starting data seeding...");

            var products = createProducts(productRepository, productSearchService);
            var clients = getAdditionalUsers(userRepository);
            createDistributedOrders(userRepository, orderRepository, products, clients, payOrderUseCase);

            log.info("Data seeding completed successfully!");
        };
    }

    private void createDistributedOrders(
            UserRepository userRepository,
            OrderRepository orderRepository,
            List<Product> products,
            List<User> clients,
            PayOrderUseCase payOrderUseCase
    ) {
        log.info("Creating distributed orders for testing...");

        var admin = userRepository.findByEmail("admin@ecommerce.com")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        var regularUser = userRepository.findByEmail("user@ecommerce.com")
                .orElseThrow(() -> new RuntimeException("Regular user not found"));

        var random = new Random();
        int totalOrders = 0;
        int totalPaidOrders = 0;

        // 1. Admin user - VIP Client (20 paid orders)
        log.info("Creating orders for admin user (VIP)...");
        for (int i = 0; i < 20; i++) {
            Order order = createRandomOrder(admin, products, random, 3, 5);
            order = orderRepository.save(order);
            payOrderUseCase.execute(order.getId());
            totalOrders++;
            totalPaidOrders++;
        }

        // 2. Regular user - Frequently client (12 paid orders)
        log.info("Creating orders for regular user...");
        for (int i = 0; i < 12; i++) {
            Order order = createRandomOrder(regularUser, products, random, 2, 4);
            order = orderRepository.save(order);
            payOrderUseCase.execute(order.getId());
            totalOrders++;
            totalPaidOrders++;
        }

        // 3. Other clients - Distributed orders
        log.info("Creating orders for new clients...");

        // Clients with a lot of buys (client1, client2, client3)
        for (int i = 0; i < 3; i++) {
            User client = clients.get(i);
            int orderCount = 10 + random.nextInt(6); // 10-15 orders
            log.info("Creating {} orders for {}", orderCount, client.getName());

            for (int j = 0; j < orderCount; j++) {
                Order order = createRandomOrder(client, products, random, 2, 4);
                order = orderRepository.save(order);
                payOrderUseCase.execute(order.getId());
                totalOrders++;
                totalPaidOrders++;
            }
        }

        // Clients with some buys (client4, client5, client6, client7)
        for (int i = 3; i < 7; i++) {
            User client = clients.get(i);
            int orderCount = 5 + random.nextInt(4); // 5-8 orders
            log.info("Creating {} orders for {}", orderCount, client.getName());

            for (int j = 0; j < orderCount; j++) {
                Order order = createRandomOrder(client, products, random, 1, 3);
                order = orderRepository.save(order);
                payOrderUseCase.execute(order.getId());
                totalOrders++;
                totalPaidOrders++;
            }
        }

        // Clients with a feel buys (client8, client9, client10)
        for (int i = 7; i < 10; i++) {
            User client = clients.get(i);
            int orderCount = 1 + random.nextInt(3); // 1-3 orders
            log.info("Creating {} orders for {}", orderCount, client.getName());

            for (int j = 0; j < orderCount; j++) {
                Order order = createRandomOrder(client, products, random, 1, 2);
                order = orderRepository.save(order);
                payOrderUseCase.execute(order.getId());
                totalOrders++;
                totalPaidOrders++;
            }
        }

        // 4. Create some pending orders (not paid) - Distributed among all users
        log.info("Creating pending orders...");
        List<User> allUsers = new ArrayList<>();
        allUsers.add(admin);
        allUsers.add(regularUser);
        allUsers.addAll(clients);

        for (int i = 0; i < 8; i++) {
            User randomUser = allUsers.get(random.nextInt(allUsers.size()));
            Order order = createRandomOrder(randomUser, products, random, 1, 3);
            orderRepository.save(order);
            totalOrders++;
        }

        SecurityContextHolder.clearContext();

        log.info("═══════════════════════════════════════════════════════");
        log.info("Data seeding completed!");
        log.info("Total users: {} (1 admin + 1 regular + {} clients)", allUsers.size(), clients.size());
        log.info("Total orders: {} ({} paid, {} pending)", totalOrders, totalPaidOrders, totalOrders - totalPaidOrders);
        log.info("═══════════════════════════════════════════════════════");
        log.info("You can now test the reports:");
        log.info("  → GET /api/v1/reports/top-buyers");
        log.info("  → GET /api/v1/reports/average-ticket");
        log.info("  → GET /api/v1/reports/current-month-revenue");
        log.info("  → GET /api/v1/reports/revenue?startDate=2024-07-01&endDate=2025-01-31");
        log.info("═══════════════════════════════════════════════════════");
    }

    private List<Product> createProducts(ProductRepository productRepository, ProductSearchService productSearchService) {
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

        savedProducts.forEach(product -> {
            try {
                productSearchService.syncProduct(product);
                log.debug("Synced product {} to Elasticsearch", product.getName());
            } catch (Exception e) {
                log.error("Failed to sync product {} to Elasticsearch: {}",
                        product.getName(), e.getMessage());
            }
        });

        log.info("{} products synced to Elasticsearch", savedProducts.size());
        return savedProducts;
    }

    private List<User> getAdditionalUsers(UserRepository userRepository) {
        log.info("Loading client users from database...");

        List<User> clients = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            String email = "client" + i + "@ecommerce.com";
            User client = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Client user not found: " + email));
            clients.add(client);
        }

        log.info("Loaded {} client users from database", clients.size());
        return clients;
    }

    private Order createRandomOrder(User user, List<Product> products, Random random, int minItems, int maxItems) {
        setAuthenticatedUser(user);

        Order order = new Order();
        int itemCount = minItems + random.nextInt(maxItems - minItems + 1);

        for (int i = 0; i < itemCount; i++) {
            Product randomProduct = products.get(random.nextInt(products.size()));
            int quantity = 1 + random.nextInt(3);
            order.addItem(randomProduct, quantity);
        }

        return order;
    }

    private void setAuthenticatedUser(User user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
