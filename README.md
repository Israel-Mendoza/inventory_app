# High-Concurrency Inventory Reservation System

A simple CRUD application built with **Kotlin**, **Spring Boot**, and **Coroutines** to demonstrate advanced concurrency patterns in a real-world scenario: a Flash Sale reservation system.

## Core Concurrency Patterns
- **Striped Locking**: Uses a fixed-size `Array<Mutex>` (64 stripes) to map product IDs to locks, ensuring thread safety per product while avoiding memory leaks associated with unbounded maps.
- **Optimistic Locking**: Leverages JPA `@Version` as a secondary safety net at the database level.
- **Structured Concurrency**: Utilizes Kotlin Coroutines (`suspend` functions, `withLock`, `Dispatchers.IO`) for efficient asynchronous processing.
- **Virtual Threads**: Enabled (Java 25) to handle high-throughput blocking I/O.
- **Atomic Operations**: Business logic is wrapped in Spring `TransactionTemplate` to ensure ACID properties during stock updates and reservation creation.

## Prerequisites
- **Java 25**
- **Docker & Docker Compose**
- **Gradle 9+** (included via `./gradlew`)

## Getting Started

### 1. Start the Database
The project uses PostgreSQL 17. Run the following command to start the database container:
```bash
docker-compose up -d
```
The database will be initialized automatically using the schema in `init-db/schema.sql`.

### 2. Run the Application
Start the Spring Boot application:
```bash
./gradlew bootRun
```
The API will be available at `http://localhost:8080`.

### 3. Run the Concurrency Stress Test
To verify the system's robustness under high load, run the provided stress test. It launches 100 concurrent reservation requests against a single product with limited stock using Testcontainers:
```bash
./gradlew test --info --tests dev.artisra.simplecrud.ConcurrencyTest
```

## API Usage

### Create a Product
```bash
curl -X POST http://localhost:8080/api/products \
-H "Content-Type: application/json" \
-d '{"name": "Limited Edition Sneakers", "stock": 10}'
```

### Get Product Details
```bash
# Replace {id} with the UUID from the previous response
curl http://localhost:8080/api/products/{id}
```

### Create a Reservation
```bash
curl -X POST http://localhost:8080/api/reservations \
-H "Content-Type: application/json" \
-d '{
  "productId": "{product-uuid}",
  "userId": "{user-uuid}",
  "quantity": 1
}'
```

### Confirm a Reservation
```bash
curl -X POST http://localhost:8080/api/reservations/{reservation-uuid}/confirm
```

### Cancel a Reservation
```bash
curl -X POST http://localhost:8080/api/reservations/{reservation-uuid}/cancel
```

### Get Reservation Details
```bash
curl http://localhost:8080/api/reservations/{reservation-uuid}
```

## Database Schema
The schema (located in `init-db/schema.sql`) includes a `CHECK (stock >= 0)` constraint as a final fail-safe to prevent overselling at the storage layer.
