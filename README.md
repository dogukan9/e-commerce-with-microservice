# Example of E-commerce backend like shopwise.with Microservice

A production-ready microservice infrastructure template built with Spring Boot. Demonstrates core microservice architecture patterns through an e-commerce scenario.

## Architecture

```
                        ┌─────────────────┐
                        │   API Gateway   │
                        │   :8080         │
                        └────────┬────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                  │
    ┌─────────▼──────┐  ┌───────▼────────┐  ┌──────▼─────────┐
    │  User Service  │  │Product Service │  │ Order Service  │
    │  :8081         │  │  :8082         │  │  :8083         │
    └─────────┬──────┘  └───────┬────────┘  └──────┬─────────┘
              │                  │                  │
    ┌─────────▼──────┐  ┌───────▼────────┐  ┌──────▼─────────┐
    │  PostgreSQL    │  │  PostgreSQL    │  │  PostgreSQL    │
    │  :5432         │  │  :5433         │  │  :5434         │
    └────────────────┘  └────────────────┘  └────────────────┘
                                 │
                        ┌────────▼────────┐
                        │     Kafka       │
                        │     :9092       │
                        └─────────────────┘
```

## Tech Stack

- **Java 17**
- **Spring Boot 3.5.1**
- **Spring Cloud Gateway** — API Gateway, JWT validation, routing
- **Spring Security** — JWT-based authentication
- **Spring Data JPA** — database operations
- **Apache Kafka** — asynchronous inter-service communication
- **PostgreSQL** — separate database per service
- **Redis** — JWT whitelist
- **OpenFeign** — synchronous inter-service communication
- **Lombok** — boilerplate reduction

## Features

### Security
- JWT-based authentication
- Token management with Redis whitelist
- Centralized authentication at API Gateway
- Independent JWT validation in each service

### Kafka Patterns
- **Outbox Pattern** — prevents event loss
- **Dead Letter Queue** — failed event management
- **Idempotency** — prevents duplicate event processing
- **Dead Letter Retry Service** — automatic retry of failed events

### Architecture Patterns
- **Repository Pattern** — database abstraction
- **Domain Model** — business rules in the domain layer
- **JPA Auditing** — automatic management of createdBy, updatedBy, createdAt, updatedAt

## Services

### User Service (:8081)
User management and authentication.
- User registration and login
- JWT token generation
- Redis whitelist management

### Product Service (:8082)
Product and inventory management.
- Product CRUD operations
- Stock reservation management (Saga Pattern)
- Kafka event consumer (OrderCreated, OrderCancelled, OrderConfirmed)

### Order Service (:8083)
Order management.
- Order creation, confirmation, and cancellation
- Event publishing with Kafka Outbox Pattern
- Saga orchestration

### API Gateway (:8080)
Single entry point.
- JWT validation
- Redis whitelist check
- Route management
- X-User-Id, X-User-Role header injection


## Getting Started

### Prerequisites
- Java 17
- Docker & Docker Compose
- Maven




## Project Structure

```
shopwise-microservice/
├── shopwise-user-service/
├── shopwise-product-service/
├── shopwise-order-service/
├── shopwise-api-gateway/
└── docker-compose.yml
```

Each service follows Hexagonal Architecture:

```
src/main/java/com/shopwise/{service}/
├── domain/
│   ├── model/        # Domain models
│   └── port/         # Repository ports
├── application/
│   ├── dto/          # Request/Response DTOs
│   ├── port/         # Use case ports
│   └── service/      # Business logic
├── infrastructure/
│   ├── config/       # Configurations
│   ├── exception/    # Error handling
│   ├── kafka/        # Kafka consumers/producers
│   ├── persistence/  # JPA entities and repositories
│   └── client/       # Feign clients
└── api/              # REST controllers
```
