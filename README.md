# Room Booking Application

A **robust, production-grade Spring Boot application** for seamless room reservations. This project exposes a comprehensive set of RESTful APIs for managing users, rooms, and reservations, all protected with JWT authentication and authorization. Data persistence is handled by MariaDB with Flyway-managed migrations, while RabbitMQ powers asynchronous email notifications for key reservation events.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Clone & Setup](#clone--setup)
    - [Environment Configuration](#environment-configuration)
    - [Build & Run with Docker Compose](#build--run-with-docker-compose)
    - [Exporting Environment Variables with Bash](#exporting-environment-variables-with-bash)
    - [Run Natively (Local Development)](#run-natively-local-development)
- [API Documentation (Swagger UI)](#api-documentation-swagger-ui)
- [Database Schema](#database-schema)
- [Default Admin User](#default-admin-user)
- [Project Structure](#project-structure)
- [Error Handling](#error-handling)
- [Testing](#testing)
- [License](#license)

---

## Features

- **User Management**: Full CRUD with role-based access (ADMIN, USER).
- **Room Management**: Create, update, and delete rooms, specifying capacity, description, and location.
- **Reservation Management**: Make, view, update, and cancel reservations with automatic conflict prevention.
- **Authentication & Authorization**: Secure endpoints via JWT (access & refresh tokens) and RBAC.
- **Email Notifications**: Asynchronous reservation confirmations and updates via RabbitMQ.
- **Database Versioning**: Automatic schema evolution and seed data using Flyway migrations.
- **Interactive API Docs**: Swagger UI via SpringDoc OpenAPI.
- **Auditing**: Automatic tracking of entity creation and updates.
- **HATEOAS**: Hypermedia-driven REST APIs.
- **Input Validation**: Extensive request validation with detailed error messages.
- **Global Error Handling**: Uniform JSON error responses.

---

## Tech Stack

- **Spring Boot** `3.4.5`
- **Java** `17`
- **Maven**
- **MariaDB**
- **Flyway** (DB migrations)
- **Spring Data JPA**
- **Spring Security** (JWT)
- **RabbitMQ** (Async messaging)
- **Spring AMQP**, **Spring Mail**
- **Lombok**
- **SpringDoc OpenAPI / Swagger UI**
- **Spring HATEOAS**
- **Docker & Docker Compose**

---

## Getting Started

### Prerequisites

- **Java 17+**
- **Maven 3.x**
- **Docker & Docker Compose** (recommended for easy setup)

---

### Clone & Setup

```bash
git clone https://github.com/alef-thallys/roomBooking.git
cd roomBooking
```

---

### Environment Configuration

All required environment variables are listed in `.env.example`.

Copy the example file and fill in your credentials:

```bash
cp .env.example .env
```

Update `.env` with your values:

- Database credentials
- Mail credentials (`MAIL_USERNAME`, `MAIL_PASSWORD` — e.g., Gmail App Password)
- Strong secrets for `JWT_SECRET` and `JWT_REFRESHSECRET`

**Never commit real credentials or secrets to source control.**

---

### Build & Run with Docker Compose

Docker Compose will provision MariaDB, RabbitMQ, run Flyway migrations, and start the application.

1. **Build the Application**

    ```bash
    mvn clean package -DskipTests
    ```

2. **Start All Services**

    ```bash
    docker-compose up --build
    ```

- The app will be available at [http://localhost:8080](http://localhost:8080)
- API documentation: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

### Exporting Environment Variables with Bash (optional)

This step is **just for users who want to run the application natively** (without Docker). By exporting environment variables from your `.env` file, you ensure that commands like `mvn spring-boot:run` or `java -jar ...` will use your configuration.

```bash
source .env
```

> **Tip:**
> - Do not add spaces around the `=`, and wrap values with spaces or special characters in quotes.
> - Alternatively, use `export $(grep -v '^#' .env | xargs)`, but `source .env` is more robust for quoted values.
> - Never use this command in production or share your `.env` file with sensitive data!

---

### Run Natively (Local Development)

1. **Start MariaDB & RabbitMQ Manually**
    - MariaDB on `3306`, database `room_booking`
    - RabbitMQ on `5672` (user: `guest`/`guest`)

2. **Configure `src/main/resources/application.yml`** (or use environment variables):

   ```yaml
   spring:
     datasource:
       url: jdbc:mariadb://localhost:3306/room_booking
       username: your_db_user
       password: your_db_password
     rabbitmq:
       host: localhost
       port: 5672
       username: guest
       password: guest
     mail:
       username: your_mail@gmail.com
       password: your_mail_password

   jwt:
     secret: ${JWT_SECRET:your_jwt_secret}
     refreshSecret: ${JWT_REFRESHSECRET:your_jwt_refresh_secret}
   ```

3. **Apply Flyway Migrations** (runs on Maven build):

    ```bash
    mvn clean install
    ```

4. **Run the App:**

    ```bash
    mvn spring-boot:run
    # or
    java -jar target/roombooking-0.0.1-SNAPSHOT.jar
    ```

---

## API Documentation (Swagger UI)

- [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## Database Schema

Schema managed by Flyway. Main tables:

- **users**: name, email, password, phone, role, auditing fields
- **rooms**: name, description, capacity, location, auditing fields
- **reservations**: start/end date, user, room, auditing fields

Initial data loaded via:

- `V2__populate_user_table.sql`
- `V4__populate_room_table.sql`
- `V6__populate_reservation_table.sql`

---

## Default Admin User

On first startup, an admin account is created if missing:

- **Email:** `admin@admin.com`
- **Password:** `admin123`
- **Role:** `ADMIN`

> **Change this password immediately in production!**

---

## Project Structure

```
roomBooking/
├── src/main/java/com/github/alefthallys/roombooking/
│   ├── RoomBookingApplication.java      # Main entry point
│   ├── annotations/                     # Custom annotations
│   ├── assemblers/                      # HATEOAS assemblers
│   ├── config/                          # Configurations
│   ├── controllers/                     # REST controllers
│   ├── dtos/                            # Data Transfer Objects
│   ├── exceptions/                      # Custom/global exceptions
│   ├── mappers/                         # Entity-DTO mappers
│   ├── messaging/                       # RabbitMQ consumers/producers
│   ├── models/                          # JPA entities
│   ├── repositories/                    # JPA repositories
│   ├── security/                        # Security/JWT logic
│   ├── services/                        # Business logic
│   └── validadors/                      # Validators
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/                    # Flyway migration scripts
├── .env.example                         # Example environment file
└── src/test/java/com/github/alefthallys/roombooking/
    ├── RoomBookingApplicationTests.java
    ├── controllers/
    ├── security/jwt/
    ├── services/
    ├── testBuilders/
    └── testUtils/
```

---

## Error Handling

A global exception handler ensures consistent JSON error responses:

- **400 Bad Request**: Validation/malformed input
- **401 Unauthorized**: Authentication failures
- **403 Forbidden**: Access denied
- **404 Not Found**: Entity missing
- **409 Conflict**: Duplicates/conflicts
- **500 Internal Server Error**: Unhandled errors

Example response:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid request body format or missing content",
  "path": "/api/v1/users",
  "timestamp": "2023-10-27T10:30:00.000Z",
  "fieldErrors": [
    {
      "field": "email",
      "message": "Invalid email format",
      "rejectedValue": "invalid"
    }
  ]
}
```

---

## Testing

Unit and integration tests are under `src/test/java`. To run:

```bash
mvn test
```

---

## License

Licensed under the MIT License. See the [LICENSE](LICENSE) file for details.