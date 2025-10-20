# 🛒 E-commerce API

A modern and scalable e-commerce REST API built with Spring Boot 3.5, featuring comprehensive product management, order processing with event-driven architecture, and advanced search capabilities.

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Project Structure](#-project-structure)
- [Contributing](#-contributing)

## ✨ Features

### Core Functionality
- **Product Management**: Full CRUD operations with stock control
- **Order Processing**: Complete order lifecycle with payment processing
- **User Authentication**: JWT-based authentication with role-based access control
- **Advanced Search**: Elasticsearch integration for fast product search with filters
- **Event-Driven Architecture**: Kafka messaging for asynchronous order processing
- **Stock Management**: Automated stock updates after payment confirmation
- **Business Reports**: Analytics for top buyers, average ticket, and revenue

### Technical Features
- RESTful API with Spring Boot 3.5
- Clean Architecture with DDD principles
- Event-driven architecture with Apache Kafka
- Full-text search with Elasticsearch
- Database migrations with Flyway
- Comprehensive test coverage (Unit + Integration)
- Docker Compose for easy local development
- JWT authentication and authorization
- Global exception handling
- Audit trails with JPA Auditing

## 🚀 Tech Stack

### Backend
- **Java 21** - Latest LTS version
- **Spring Boot 3.5.6** - Application framework
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Data persistence
- **Spring Data Elasticsearch** - Search functionality
- **Spring Kafka** - Event streaming

### Infrastructure
- **MySQL** - Primary database
- **Elasticsearch 8.11** - Search engine
- **Apache Kafka** - Message broker
- **Docker Compose** - Container orchestration
- **Flyway** - Database migrations

### Testing
- **JUnit 5** - Test framework
- **Mockito** - Mocking framework
- **Spring Test** - Integration testing
- **H2** - In-memory database for tests
- **Testcontainers** - Integration tests with real services

### Libraries
- **Lombok** - Boilerplate code reduction
- **JJWT** - JWT token handling
- **Apache Commons Lang** - Utility functions

## 🏗️ Architecture

The project follows **Clean Architecture** principles with clear separation of concerns:

```
├── presentation/       # Controllers, DTOs, API layer
├── application/        # Use cases, services, business logic
├── domain/             # Entities, repositories interfaces, domain logic
└── infrastructure/     # External services, configurations, implementations
```

### Key Design Patterns
- **Repository Pattern**: Data access abstraction
- **Service Layer**: Business logic encapsulation
- **DTO Pattern**: Data transfer objects for API responses
- **Event-Driven**: Asynchronous processing with Kafka
- **Dependency Injection**: Spring IoC container

### Event Flow
```
Order Payment → OrderPaidEvent → Kafka Topic → Consumer → Stock Update
```

## 🚀 Getting Started

### Prerequisites
- Java 21 or higher
- Docker and Docker Compose
- Gradle 8.14+ (wrapper included)

### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd ecommerce
```

2. **Start infrastructure services**
```bash
docker-compose up -d
```

This will start:
- MySQL (port 3306)
- Elasticsearch (port 9200)
- Kafka + Zookeeper (port 9092)

3. **Run the application**
```bash
./gradlew bootRun
```

The API will be available at `http://localhost:8080`

### Default Users

The application comes with pre-configured users:

| Email | Password | Role |
|-------|----------|------|
| admin@ecommerce.com | admin | ADMIN |
| user@ecommerce.com | user | USER |
| client1@ecommerce.com | client1 | USER |
| ... | ... | ... |
| client10@ecommerce.com | client10 | USER |

### Sample Data

The application automatically seeds the database with:
- 20 products across different categories
- Multiple test users
- Sample orders distributed among users

## 📚 API Documentation

### Authentication

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@ecommerce.com",
  "password": "admin"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "type": "Bearer",
  "email": "admin@ecommerce.com",
  "name": "Administrator",
  "role": "ADMIN"
}
```

### Products

#### Create Product (ADMIN only)
```http
POST /api/v1/products
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone with A17 Pro chip",
  "price": 999.99,
  "category": "Electronics",
  "stockQuantity": 50
}
```

#### List Products with Filters
```http
GET /api/v1/products?name=iPhone&category=Electronics&minPrice=500&maxPrice=1500&page=0&size=10&sort=name
Authorization: Bearer {token}
```

#### Get Product by ID
```http
GET /api/v1/products/{id}
Authorization: Bearer {token}
```

#### Update Product (ADMIN only)
```http
PUT /api/v1/products/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "iPhone 15 Pro Updated",
  "description": "Updated description",
  "price": 899.99,
  "category": "Electronics",
  "stockQuantity": 45
}
```

#### Delete Product (ADMIN only)
```http
DELETE /api/v1/products/{id}
Authorization: Bearer {token}
```

### Orders

#### Create Order
```http
POST /api/v1/orders
Authorization: Bearer {token}
Content-Type: application/json

[
  {
    "productId": "uuid-here",
    "quantity": 2
  },
  {
    "productId": "another-uuid",
    "quantity": 1
  }
]

Response:
{
  "orderId": "order-uuid",
  "status": "PENDING"
}
```

#### Pay Order
```http
POST /api/v1/orders/pay/{orderId}
Authorization: Bearer {token}

Response:
{
  "orderId": "order-uuid",
  "status": "PAID"
}
```

### Reports (ADMIN only)

#### Top 5 Buyers
```http
GET /api/v1/reports/top-buyers?startDate=2025-01-01&endDate=2025-12-31
Authorization: Bearer {token}

Response:
[
  {
    "userId": "uuid",
    "userName": "John Doe",
    "userEmail": "john@example.com",
    "totalOrders": 15,
    "totalSpent": 2500.00
  }
]
```

#### Average Ticket by User
```http
GET /api/v1/reports/average-ticket?startDate=2025-01-01&endDate=2025-12-31
Authorization: Bearer {token}
```

#### Current Month Revenue
```http
GET /api/v1/reports/current-month-revenue
Authorization: Bearer {token}

Response:
{
  "month": "OCTOBER",
  "year": 2025,
  "totalRevenue": 15750.50
}
```

#### Custom Period Revenue
```http
GET /api/v1/reports/revenue?startDate=2025-07-01&endDate=2025-09-30
Authorization: Bearer {token}
```

## 🧪 Testing

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests ProductServiceTests
```

### Test Coverage
The project includes:
- **Unit Tests**: Service layer logic testing
- **Integration Tests**: End-to-end API testing
- **Test Coverage**: ~80% code coverage

### Test Structure
```
src/test/java/
├── application/
│   ├── services/          # Service layer unit tests
│   └── usecases/          # Use case unit tests
└── presentation/
    └── controllers/       # Controller integration tests
```

## 📁 Project Structure

```
ecommerce/
├── src/
│   ├── main/
│   │   ├── java/com/techmath/ecommerce/
│   │   │   ├── application/           # Business logic layer
│   │   │   │   ├── converters/       # DTO ↔ Entity converters
│   │   │   │   ├── services/         # Business services
│   │   │   │   └── usecases/         # Application use cases
│   │   │   ├── domain/                # Domain layer
│   │   │   │   ├── entities/         # Domain entities
│   │   │   │   ├── enums/            # Domain enums
│   │   │   │   ├── events/           # Domain events
│   │   │   │   ├── exceptions/       # Domain exceptions
│   │   │   │   └── repositories/     # Repository interfaces
│   │   │   ├── infrastructure/        # Infrastructure layer
│   │   │   │   ├── config/           # Configurations
│   │   │   │   ├── exceptions/       # Global exception handler
│   │   │   │   ├── messaging/        # Kafka producers/consumers
│   │   │   │   ├── search/           # Elasticsearch
│   │   │   │   ├── security/         # Security configs
│   │   │   │   └── utils/            # Utilities
│   │   │   └── presentation/          # Presentation layer
│   │   │       ├── controllers/      # REST controllers
│   │   │       └── dto/              # Data transfer objects
│   │   └── resources/
│   │       ├── application.yml       # Application config
│   │       └── db/migration/         # Flyway migrations
│   └── test/                          # Test classes
├── build.gradle.kts                   # Build configuration
├── compose.yaml                       # Docker Compose
└── README.md                          # This file
```

## 🔧 Configuration

### Application Properties

Key configurations in `application.yml`:

```yaml
spring:
  datasource:
    # Configured via Docker Compose
  
  kafka:
    bootstrap-servers: localhost:9092
  
jwt:
  secret: ${JWT_SECRET}  # Set via environment variable
  expiration: 86400000   # 24 hours
```

### Environment Variables

Create a `.env` file for sensitive data:
```env
JWT_SECRET=your-secret-key-here
```

## 🛡️ Security

- JWT-based authentication
- BCrypt password hashing
- Role-based access control (RBAC)
- CORS configuration
- Stateless session management

## 📊 Monitoring & Logging

The application uses SLF4J with Logback for logging:
- Request/Response logging
- Business operation tracking
- Error tracking and stack traces
- Kafka event monitoring

---

**Built with ❤️ using Spring Boot 3.5 and Java 21**