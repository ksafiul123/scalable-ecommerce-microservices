# Scalable Microservices-Based E-Commerce System

A full-stack e-commerce platform built as a university DBMS course project, demonstrating scalable microservices architecture, secure payment integration, and optimized relational database design.

---

## Architecture Overview

```
Frontend (React)
      │
      ▼
API Gateway :8080  ──── Eureka Server :8761
      │                       │
      ├── Auth Service    :8081 (registers)
      ├── User Service    :8082 (registers)
      ├── Product Service :8083 (registers)
      ├── Order Service   :8084 (registers)
      └── Payment Service :8085 (registers)
                │
          PostgreSQL :5432
```

## Tech Stack

### Backend
| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.x |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| Service Discovery | Netflix Eureka |
| Security | Spring Security + JWT (HS256) |
| ORM | Spring Data JPA (Hibernate) |
| Database | PostgreSQL 15 |
| Inter-service calls | WebClient + Eureka load balancing |
| Payment | Stripe Checkout Sessions |
| Build | Maven |

### Frontend
| Layer | Technology |
|---|---|
| Framework | React 18 |
| Styling | Tailwind CSS v3 |
| HTTP | Axios |
| Routing | React Router v6 |

### Infrastructure
| Tool | Purpose |
|---|---|
| Docker | PostgreSQL + pgAdmin |
| docker-compose | Local environment orchestration |
| Stripe CLI | Local webhook testing |

---

## Microservices

| Service | Port | Responsibility |
|---|---|---|
| `eureka-server` | 8761 | Service registry and discovery |
| `api-gateway` | 8080 | Single entry point, JWT pre-validation, routing |
| `auth-service` | 8081 | Register, login, JWT issuance |
| `user-service` | 8082 | User profile management |
| `product-service` | 8083 | Product and category CRUD, stock management |
| `order-service` | 8084 | Order placement, status tracking, stock decrement |
| `payment-service` | 8085 | Stripe Checkout Session creation, webhook handling |

---

## Database Schema

All services share a single PostgreSQL database (`ecommerce_db`) with schema separation by table ownership. No physical foreign keys across service boundaries — logical relationships only.

```
roles         ──< users ──< user_profiles
categories    ──< products
orders        ──< order_items
payments      ──  orders  (logical FK, no constraint)
```

### Key Design Decisions
- `ddl-auto: none` — all schemas managed via explicit `schema.sql` files
- ENUM types replaced with `VARCHAR(20) + CHECK constraints` for Spring Boot compatibility
- Stock decrement and order save in a single `@Transactional` block (prevents oversell)
- No physical FK from `order_items.product_id` → `products.id` (microservice boundary)

---

## Features

### User-Facing
- Register and login with JWT authentication
- Browse products with search and category filter
- Product detail page with stock status
- Add to cart (React state), quantity management
- Place orders with shipping address
- Stripe hosted checkout payment flow
- View order history with real-time payment status
- Cancel pending/confirmed orders

### Admin
- Add and delete products and categories
- View orders by status (PENDING / CONFIRMED / SHIPPED / DELIVERED / CANCELLED)
- Advance order status through the pipeline

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+
- Docker Desktop

### 1. Start the database

```bash
docker-compose up -d
```

PostgreSQL available at `localhost:5432`
pgAdmin available at `http://localhost:5050` (admin@ecommerce.com / admin123)

### 2. Configure Stripe

In `payment-service/src/main/resources/application.yml`:
```yaml
stripe:
  secret-key: sk_test_YOUR_STRIPE_SECRET_KEY
  webhook-secret: whsec_YOUR_WEBHOOK_SECRET
```

Start the Stripe webhook listener:
```bash
stripe listen --forward-to localhost:8085/api/payments/webhook
```

### 3. Start all services (in order)

```bash
# 1. Service registry
cd eureka-server  && mvn spring-boot:run &

# 2. Gateway + downstream services
cd api-gateway    && mvn spring-boot:run &
cd auth-service   && mvn spring-boot:run &
cd user-service   && mvn spring-boot:run &
cd product-service && mvn spring-boot:run &
cd order-service  && mvn spring-boot:run &
cd payment-service && mvn spring-boot:run &
```

### 4. Start the frontend

```bash
cd frontend
npm install
npm start
```

App available at `http://localhost:3000`

### 5. Seed the database

Run `seed.sql` against `ecommerce_db` using pgAdmin Query Tool or psql:

```bash
docker exec -i postgres psql -U ecommerce_user -d ecommerce_db < seed.sql
```

**Default accounts:**
| Email | Password | Role |
|---|---|---|
| admin@ecommerce.com | password123 | ROLE_ADMIN |
| alice@example.com | password123 | ROLE_USER |
| bob@example.com | password123 | ROLE_USER |
| charlie@example.com | password123 | ROLE_USER |

---

## API Reference

All requests go through the gateway at `http://localhost:8080`.

### Auth
| Method | Endpoint | Auth | Body |
|---|---|---|---|
| POST | `/api/auth/register` | None | `{username, email, password}` |
| POST | `/api/auth/login` | None | `{email, password}` |

### Products
| Method | Endpoint | Auth |
|---|---|---|
| GET | `/api/products` | None |
| GET | `/api/products/{id}` | None |
| GET | `/api/products/search?name=` | None |
| GET | `/api/products/category/{id}` | None |
| POST | `/api/products` | ROLE_ADMIN |
| DELETE | `/api/products/{id}` | ROLE_ADMIN |

### Orders
| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/orders` | Any user |
| GET | `/api/orders/user/{userId}` | Any user |
| PUT | `/api/orders/{id}/cancel` | Any user |
| GET | `/api/orders/status/{status}` | ROLE_ADMIN |
| PUT | `/api/orders/{id}/status?status=` | ROLE_ADMIN |

### Payments
| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/payments/checkout` | Any user |
| GET | `/api/payments/order/{orderId}` | Any user |
| POST | `/api/payments/webhook` | Stripe (signature verified) |

---

## Security Model

```
Client → Gateway (validates JWT presence)
              → Downstream Service (validates JWT fully, checks role)
```

- JWT signed with HS256, 24-hour expiry
- Each service independently validates the token (no shared auth service call)
- Gateway checks token presence only — downstream services enforce role-based rules
- Stripe webhook endpoint whitelisted in payment-service (verified by Stripe signature)
- Stock endpoints accessible to any authenticated user (internal service-to-service calls)

---

## DBMS Concepts Demonstrated

- **Normalization** — 1NF → 2NF → 3NF across all tables
- **Transactions** — `@Transactional` on order placement (stock decrement + order save atomically)
- **ACID** — PostgreSQL guarantees on concurrent order placement prevent oversell
- **Indexing** — Indexes on `email`, `user_id`, `category_id`, `status`, `stripe_session_id`
- **Constraints** — CHECK constraints on status columns, NOT NULL, UNIQUE
- **Aggregate queries** — Revenue by category, orders by status, top products by sales

---

## Project Structure

```
ecommerce-system/
├── docker-compose.yml
├── seed.sql
├── eureka-server/
├── api-gateway/
├── auth-service/
├── user-service/
├── product-service/
├── order-service/
├── payment-service/
└── frontend/
    ├── src/
    │   ├── api/
    │   ├── components/
    │   ├── context/
    │   └── pages/
    └── public/
```
