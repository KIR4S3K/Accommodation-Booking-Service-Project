# Accommodation Booking Service

An online platform to manage rental accommodations, bookings, user accounts, payments, and notifications.

## Features

- **Authentication & Authorization** via JWT
- **CRUD** for Accommodations, Users, Bookings, Payments
- **Role-based access** (MANAGER vs CUSTOMER)
- **Stripe integration** for payments
- **Telegram notifications** for key events
- **Scheduled tasks**:
    - Expire old bookings daily
    - Expire unpaid payment sessions every minute
- **Database migrations** with Liquibase
- **API documentation** via Swagger / OpenAPI
- **Health check** endpoint
- **Docker & Docker Compose** for easy setup

## Technology Stack

- Java 17, Spring Boot 3.1
- Spring Data JPA (Hibernate)
- Spring Security + JWT
- MapStruct, Lombok
- Liquibase
- Stripe Java SDK
- Telegram Bot API (OkHttp)
- Hibernate Validator
- Swagger / OpenAPI (springdoc)
- JaCoCo (test coverage)
- Checkstyle (Google Java Style)
- Docker, Docker Compose
- H2 (dev), PostgreSQL (prod)

## Getting Started

### Prerequisites

- Docker & Docker Compose
- Java 17
- Maven 3.8+

### Clone & Configure

```bash
git clone https://github.com/<your-org>/<your-repo>.git
cd <your-repo>
cp .env.sample .env
# Edit .env to provide your secrets:
# JWT_SECRET, JWT_EXPIRATION_MS, STRIPE_SECRET_KEY,
# TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID,
# SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
```

### Run with Docker

```bash
docker-compose up --build
```

- **App**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console** (dev profile): http://localhost:8080/h2-console

### Run Locally (without Docker)

```bash
mvn clean spring-boot:run
```

Application will pick up environment variables from `.env`.

## Tests & Coverage

Run all tests and generate a coverage report:

```bash
mvn clean verify
```

- Checkstyle and unit tests will run.
- Coverage report: `target/site/jacoco/index.html`

## CI

Every push or pull request against `main`/`master` triggers:

```bash
mvn clean verify
```

- Checkstyle and JaCoCo reports are generated.

## Contributing

1. **Branch**: `git checkout -b feature/your-feature`
2. **Commit**: write clear, descriptive commit messages
3. **Push**: `git push origin feature/your-feature`
4. **PR**: open a pull request against `main`
5. **Review & Merge**: after approval and green CI

> **Note:** Never commit real secret values—use only the `.env` file locally and keep **`.env.sample`** in the repo.  
