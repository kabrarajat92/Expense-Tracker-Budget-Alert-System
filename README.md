# Expense Tracker & Budget Alerts System

A full-stack, microservices-based Expense Tracker built with Java, Spring Boot, Spring Security, Angular, MySQL, MongoDB, Apache Kafka, Redis, Docker, and AI integration using Groq.

The application helps users track expenses, manage category-based budgets, monitor spending, receive automatic budget alerts (in-app and email), and get AI-powered financial insights — all behind a single API Gateway with JWT-secured, independently deployable services.

🎯 **Goal:** Build a production-style microservices application demonstrating secure REST APIs, event-driven communication, asynchronous processing, caching, fault tolerance, containerization, database integration, and AI-powered features.

---

## 📌 Project Overview

Managing personal expenses manually makes it difficult to identify overspending early.

This application provides a centralized platform where users can:

- Track daily expenses
- Organize expenses by category
- Create and manage budgets
- Monitor budget utilization
- Receive automatic alerts (in-app + email) when spending crosses a configured threshold
- Analyze spending patterns using AI
- Get AI-generated budget suggestions
- Forecast potential budget overruns
- Ask natural-language questions about their spending via an AI assistant

The system uses **Kafka** for asynchronous budget-alert communication, **MongoDB** for notification storage, **Redis** for caching AI responses, and **Groq AI** for intelligent spending analysis — protected by a **circuit breaker** against upstream AI failures.

---

## 🏗️ Architecture

The application follows a microservices architecture consisting of:

- Angular frontend
- Spring Cloud API Gateway
- User Service
- Expense Service
- Notification Service
- MySQL
- MongoDB
- Redis
- Apache Kafka
- Zookeeper
- Groq AI API

```
                         ┌──────────────────────┐
                         │      Angular 17      │
                         │      Frontend        │
                         └──────────┬───────────┘
                                    │
                                    │ HTTP
                                    ▼
                         ┌──────────────────────┐
                         │    API Gateway       │
                         │ Spring Cloud Gateway │
                         │       :8081          │
                         └──────────┬───────────┘
                                    │
                 ┌──────────────────┼──────────────────┐
                 │                  │                  │
                 ▼                  ▼                  ▼
        ┌────────────────┐ ┌────────────────┐ ┌────────────────┐
        │  User Service  │ │ Expense Service│ │ Notification   │
        │     :8090      │ │     :8091      │ │ Service :8092  │
        └───────┬────────┘ └───────┬────────┘ └───────┬────────┘
                │                  │                  │
                ▼                  ▼                  ▼
             ┌──────┐     ┌───────────────┐      ┌─────────┐
             │MySQL │     │ MySQL + Redis │      │ MongoDB │
             └──────┘     └───────┬───────┘      └─────────┘
                                  │
                                  │ Budget Alert Event
                                  ▼
                            ┌───────────┐
                            │   Kafka   │
                            │  Topic:   │
                            │budget-alert│
                            └─────┬─────┘
                                  │
                                  ▼
                         Notification Service
                                  │
                        ┌─────────┴─────────┐
                        ▼                   ▼
                     MongoDB              Email

                           ┌────────────────┐
                           │    Groq AI     │
                           │  (behind a     │
                           │ circuit breaker)│
                           └───────▲────────┘
                                   │
                                   │ REST API
                                   │
                           Expense Service
```

---

## 🔄 Application Flow

### Authentication Flow

```
User
 │
 │ Login / Register
 ▼
Angular
 │
 ▼
API Gateway
 │
 ▼
User Service
 │
 └──► JWT Token (includes username, role, email)
        │
        ▼
     Angular
        │
        │ Authorization: Bearer <JWT>
        ▼
   API Gateway
        │
        ▼
Protected Microservices
```

### Expense & Budget Alert Flow

```
User adds expense
       │
       ▼
Angular
       │
       ▼
API Gateway
       │
       ▼
Expense Service
       │
       ├──► Save expense in MySQL
       │
       ├──► Evict cached AI analysis for this user
       │
       ├──► Calculate budget utilization
       │
       └──► Threshold exceeded (≥80%)?
                │
               YES
                │
                ▼
          Publish Kafka Event (BudgetAlertEvent)
                │
                ▼
              Kafka
                │
                ▼
       Notification Service
                │
                ├──► Save notification → MongoDB
                │
                └──► Send email alert (best-effort, non-blocking)
```

---

## 🤖 AI Integration

The application integrates the **Groq API** (model: `openai/gpt-oss-120b`) to provide AI-powered financial assistance. All AI functionality is implemented inside the Expense Service, guarded by a **Resilience4j circuit breaker** and a **Redis cache**.

### AI Spending Analysis

```
User
 │
 │ "Analyze my spending"
 ▼
Angular
 │
 ▼
Expense Service
 │
 ├──► Check Redis cache (per-user key)
 │       │
 │      HIT ──► Return cached result instantly
 │       │
 │      MISS
 │       ▼
 ├──► Fetch expense data (MySQL)
 ├──► Fetch budget information (MySQL)
 ├──► Build AI prompt (explicitly instructed to use ₹/INR)
 │
 ▼
Groq API (via circuit breaker + 10s timeout)
 │
 ▼
AI-generated analysis → cached in Redis (10 min TTL)
 │
 ▼
Angular Dashboard
```

### AI Features

**📊 Spending Analysis** — analyzes recent expenses and highlights spending patterns, budget utilization, and personalized recommendations.
```
GET /api/ai/analyze
```

**💡 Smart Budget Suggestions** — uses historical spending and a stated monthly income to suggest sensible category-level budgets.
```
POST /api/ai/suggest-budget
```

**🔮 Expense Forecasting** — analyzes current spending velocity and predicts likely month-end budget overruns per category.
```
GET /api/ai/forecast
```

**💬 Natural Language Financial Assistant** — users can ask free-form questions about their own expense data.
```
POST /api/ai/chat
```
Example: *"How much did I spend on Food last week?"*

### ⚡ Circuit Breaker (Resilience4j)

The Groq AI integration is wrapped in a circuit breaker to prevent a slow or failing external API from degrading the rest of the application:

- **CLOSED** — normal operation, calls flow through to Groq
- **OPEN** — after ≥50% of the last 10 calls fail (minimum 5 calls sampled), the circuit opens and immediately returns a fallback message for 30 seconds, without attempting to call Groq at all
- **HALF_OPEN** — after the wait period, 3 test calls are allowed through; ≥2 successes closes the circuit again, otherwise it reopens
- A `TimeLimiter` treats any Groq call exceeding 10 seconds as a failure

State is inspectable live via Spring Boot Actuator:
```
GET /actuator/circuitbreakers
```

### 🗄️ Redis Caching

AI spending analysis is cached per user (`aiAnalysis::<username>`) with a 10-minute TTL, and is **explicitly evicted** whenever that user adds a new expense — so cached results never go stale after new data arrives. Cache hits, misses, and evictions are logged explicitly for observability.

---

## 🔐 Security

Security is implemented using **Spring Security** and **JWT-based authentication**.

### Security Flow

```
Login
  │
  ▼
User Service
  │
  └──► JWT generated (subject=username, claims: role, email)
          │
          ▼
       Frontend
          │
          │ Bearer Token
          ▼
     API Gateway
          │
          ▼
    Protected Services
```

Each service validates the JWT **independently** using a shared signing secret — there's no shared session store or callback to User Service on every request. User Service additionally performs a database lookup (via `UserDetailsService`) since it owns the user data; Expense Service and Notification Service build the authenticated principal directly from the token's claims, since the signature already proves authenticity.

Security features include:

- JWT authentication (HS256, 10-hour expiry)
- Spring Security stateless filter chains
- BCrypt password hashing
- Protected REST endpoints
- Role-based claims (`USER` / `ADMIN`) issued in the token — present but not yet enforced with endpoint-level `@PreAuthorize` restrictions
- Custom JWT validation filters per service
- Authentication propagation between services via shared secret, not a shared session

---

## ⚠️ Error Handling

Each service (User, Expense, Notification) implements a centralized `@RestControllerAdvice` (`GlobalExceptionHandler`) that returns a consistent JSON error shape instead of raw stack traces:

```json
{
  "timestamp": "2026-08-09T12:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Username already taken",
  "path": "/api/auth/register"
}
```

Handled cases include:
- `ResourceNotFoundException` → 404
- `DuplicateResourceException` → 409
- `InvalidCredentialsException` (User Service) → 401
- Bean validation failures (`@Valid` on request DTOs) → 400, with a field-level error map
- Any unclassified exception → 500 with a generic, non-leaking message

---

## ⚡ Event-Driven Communication

The application uses **Apache Kafka** for asynchronous communication between the Expense Service and Notification Service.

### Budget Alert Event

When spending crosses the configured budget threshold (≥80%):

```
Expense Service
      │
      │ BudgetAlertEvent
      │ (username, email, category, budgetLimit, totalSpent, percentageUsed)
      ▼
    Kafka
      │
      │ topic: budget-alert
      ▼
Notification Service
      │
      ├──► Save notification → MongoDB
      │
      └──► Send email alert (SMTP, best-effort — failures are logged, not retried,
           so a mail failure never blocks or duplicates the in-app notification)
```

This decouples the notification/email workflow from the expense operation — the Expense Service does not wait for notification processing or email delivery to complete before responding to the user.

---

## 🧩 Microservices

### 🔐 User Service — `:8090`

Responsible for:
- User registration
- User login
- JWT generation (embeds username, role, and email as claims)
- Centralized exception handling

**Main APIs**
```
POST /api/auth/register
POST /api/auth/login
GET  /api/users/profile
GET  /api/users/hello
```

**Database:** MySQL (`expense_tracker_user_db`)

---

### 💰 Expense Service — `:8091`

The core business service.

Responsible for:
- Expense management
- Budget management (upsert per category)
- Spending calculations and budget threshold monitoring
- Kafka event publishing
- AI integration (analyze / suggest-budget / forecast / chat)
- Redis-backed AI response caching
- Circuit breaker around the Groq API call

**Main APIs**
```
POST /api/expenses
GET  /api/expenses

POST /api/budgets
GET  /api/budgets

GET  /api/ai/analyze
POST /api/ai/suggest-budget
GET  /api/ai/forecast
POST /api/ai/chat

GET  /actuator/circuitbreakers
```

**Database:** MySQL (`expense_tracker_expense_db`)
**Cache:** Redis
**Messaging:** Apache Kafka (producer)
**AI:** Groq API

---

### 🔔 Notification Service — `:8092`

Responsible for consuming budget-alert events and managing user notifications.

**Responsibilities**
- Consume Kafka budget alerts
- Store notifications
- Retrieve notifications (all / unread)
- Track read/unread status
- Send email notifications via SMTP

**Main APIs**
```
GET  /api/notifications
GET  /api/notifications/unread
PUT  /api/notifications/{id}/read
```

**Database:** MongoDB (`expense_tracker_notification_db`, collection `alerts`)
**Message Consumer:** Apache Kafka (consumer group: `notification-service-group`)

---

### 🔀 API Gateway — `:8081`

The API Gateway acts as the single entry point for frontend requests. All Angular API requests are routed through the gateway.

**Routing**
```
/api/auth/**           → User Service
/api/users/**          → User Service

/api/expenses/**       → Expense Service
/api/budgets/**        → Expense Service
/api/ai/**             → Expense Service

/api/notifications/**  → Notification Service
```

Frontend applications do not need to directly communicate with individual backend services. CORS is configured at the gateway to allow requests from the Angular dev server (`http://localhost:4200`).

**Note:** the gateway currently performs routing only — it does not itself validate JWTs. Each downstream service validates independently. Centralized gateway-level JWT validation is listed under Future Improvements.

---

## 🛠️ Technology Stack

| Category | Technology |
|---|---|
| Language | Java 17 |
| Backend | Spring Boot 3.3 |
| Security | Spring Security + JWT |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| ORM | Hibernate / JPA |
| Frontend | Angular 17 |
| Relational Database | MySQL |
| NoSQL Database | MongoDB |
| Cache | Redis |
| Fault Tolerance | Resilience4j (Circuit Breaker + Time Limiter) |
| Messaging | Apache Kafka |
| Kafka Coordination | Zookeeper |
| Email | Spring Mail (SMTP) |
| AI | Groq API |
| Build Tool | Maven |
| Containerization | Docker |
| Orchestration | Docker Compose |
| Version Control | Git / GitHub |

---

## 📂 Project Structure

```
expense-tracker/
│
├── api-gateway/
│   ├── src/
│   ├── Dockerfile
│   ├── pom.xml
│   └── ...
│
├── expense-service/
│   ├── src/
│   ├── Dockerfile
│   ├── pom.xml
│   └── ...
│
├── expense-tracker-frontend/
│   ├── src/
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   ├── angular.json
│   └── ...
│
├── notification-service/
│   ├── src/
│   ├── Dockerfile
│   ├── pom.xml
│   └── ...
│
├── user-service/
│   ├── src/
│   ├── Dockerfile
│   ├── pom.xml
│   └── ...
│
├── infra/
│   ├── docker-compose.yml
│   ├── .env                  (not committed — see Configuration below)
│   └── mysql-init/
│       └── init.sql
│
├── test-all-endpoints.ps1
├── .gitignore
└── README.md
```

---

## 🐳 Running the Application

### Prerequisites

Install the following:
- Java 17+
- Docker Desktop
- Git
- Node.js + npm
- Angular CLI (only needed if running the frontend outside Docker)

Verify Docker installation:
```
docker --version
docker compose version
```

### 1. Clone the Repository

```
git clone https://github.com/kabrarajat92/Expense-Tracker-Budget-Alert-System.git
cd expense-tracker
```

### 2. Configure Environment Variables

Sensitive values are never committed to GitHub. Create `infra/.env` (this file is gitignored) with:

```env
MYSQL_ROOT_PASSWORD=your_mysql_root_password
JWT_SECRET=your_base64_jwt_secret
GROQ_API_KEY=your_groq_api_key
MAIL_USERNAME=your_gmail_address
MAIL_PASSWORD=your_gmail_app_password
```

⚠️ Never commit API keys, database passwords, JWT secrets, or email credentials to the repository.

### 3. Running with Docker Compose

From the `infra` folder:
```
cd infra
docker-compose up --build
```

This builds and starts all 4 backend services, the Angular frontend (served via nginx), MySQL, MongoDB, Kafka, Zookeeper, and Redis — fully networked together, no manual per-service startup required.

Check running containers:
```
docker ps
```

View logs for a specific service:
```
docker-compose logs -f expense-service
```

**Stop the application**
```
docker-compose down
```

To stop containers **and remove volumes** (deletes persisted DB data):
```
docker-compose down -v
```

### Running individual services locally (without Docker)

Each service also runs standalone via your IDE (e.g., Spring Tool Suite) against `localhost` defaults — useful during development. Start the infra containers only (`docker-compose up -d`) and run each Spring Boot service normally; all config falls back to `localhost` when no environment variables are set.

---

## 🧪 API Testing

The repository contains a PowerShell script that exercises every endpoint across all four services through the gateway — including a full end-to-end trigger of the Kafka → notification → email pipeline.

```
.\test-all-endpoints.ps1
```

It covers: registration, login, protected routes, expense/budget CRUD, all four AI endpoints, notification retrieval, an end-to-end budget-threshold-crossing test that confirms the async Kafka pipeline actually produced a notification, and a negative test confirming unauthenticated requests are rejected.

---

## 📊 Example User Journey

```
1. Register
      ↓
2. Login → receive JWT (with username, role, email claims)
      ↓
3. Create monthly budgets
      ↓
4. Add expenses
      ↓
5. Monitor spending on the dashboard
      ↓
6. Cross budget threshold (≥80%)
      ↓
7. Kafka publishes BudgetAlertEvent
      ↓
8. Notification Service consumes event
      ↓
9. Notification stored in MongoDB + email sent
      ↓
10. User sees in-app alert and receives an email
      ↓
11. User requests AI spending analysis (served from Redis cache if unchanged)
      ↓
12. Groq generates personalized insights
```

---

## 🧠 Key Technical Concepts Demonstrated

- Microservices architecture with database-per-service
- RESTful API development
- Spring Boot, Spring Security, JWT authentication
- Spring Cloud Gateway (single entry point, path-based routing, CORS)
- Event-driven architecture (Kafka producer/consumer across services)
- Redis caching with explicit invalidation on data change
- Circuit breaker and time-limiting around an external AI dependency (Resilience4j)
- Centralized exception handling (`@RestControllerAdvice`)
- MongoDB and MySQL integration (polyglot persistence)
- Email notifications (Spring Mail, best-effort/non-blocking)
- Docker multi-stage builds and full-stack Docker Compose orchestration
- AI API integration with prompt construction from live application data
- DTO-based API design, layered architecture, JPA/Hibernate
- Automated endpoint testing (PowerShell smoke test suite)

---

## 🔮 Future Improvements

Potential future enhancements include:

- Gateway-level JWT validation (currently validated independently per service)
- Role-based endpoint authorization (`@PreAuthorize` — the JWT already carries a role claim, not yet enforced)
- Service discovery using Eureka
- Client-side load balancing
- API rate limiting at the gateway
- Centralized configuration using Spring Cloud Config
- Distributed tracing across services
- Kafka dead-letter topic and consumer idempotency (currently at-least-once with no dedupe)
- Automated unit/integration test suite (currently manual + smoke-tested)
- Recurring expenses and scheduled monthly reports
- Kubernetes deployment
- CI/CD pipeline
- Live cloud deployment

---

## 👨‍💻 Author

**Rajat Kabra**
Java Full Stack Developer
GitHub: [kabrarajat92](https://github.com/kabrarajat92)

---

## ⭐ Project Highlights

**Microservices + Event-Driven Architecture + JWT Security + Kafka + Redis + Circuit Breaker + Docker + AI**

This project was designed to demonstrate how a real-world expense management platform can be decomposed into independently deployable services while maintaining secure communication, asynchronous event processing, fault-tolerant external API integration, persistent polyglot storage, and AI-powered functionality — containerized end-to-end for one-command reproducibility.
