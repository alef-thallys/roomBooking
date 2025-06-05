# Room Booking Application

A robust Spring Boot project for seamless room reservations. This application exposes a suite of RESTful APIs for user, room, and reservation management, secured by JWT authentication and authorization. Data persistence is managed by MariaDB with Flyway migrations, and RabbitMQ powers asynchronous email notifications.

---

## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Building the Project](#building-the-project)
    - [Running with Docker Compose](#running-with-docker-compose)
    - [Running Natively](#running-natively)
- [API Documentation (Swagger UI)](#api-documentation-swagger-ui)
- [Database Schema](#database-schema)
- [Default Admin User](#default-admin-user)
- [Project Structure](#project-structure)
- [Error Handling](#error-handling)
- [Testing](#testing)
- [License](#license)

---

## Features

- **User Management**: Full CRUD operations with role-based access control (ADMIN, USER).
- **Room Management**: Manage rooms with capacity, description, and location.
- **Reservation Management**: Create, view, update, and delete reservations with conflict checking.
- **Authentication & Authorization**: Secure endpoints using JWT (with refresh token support) and RBAC.
- **Email Notifications**: Asynchronous confirmations using RabbitMQ.
- **Database Management**: MariaDB with Flyway migrations.
- **API Documentation**: Interactive docs via SpringDoc OpenAPI (Swagger UI).
- **Auditing**: Automatic creation/modification tracking for entities.
- **HATEOAS**: Discoverable APIs via hypermedia links.
- **Input Validation**: Robust request validation.
- **Global Exception Handling**: Consistent JSON error responses.

---

## Technologies Used

- **Spring Boot**: `3.4.5`
- **Java**: `17`
- **Maven**: Build automation
- **MariaDB**: Relational database
- **Flyway**: Database migrations
- **Spring Data JPA**: ORM
- **Spring Security**: Auth & RBAC
- **JWT**: Token-based auth
- **RabbitMQ**: Message broker
- **Spring AMQP**: RabbitMQ integration
- **Spring Mail**: Email
- **Lombok**: Reduces boilerplate
- **SpringDoc OpenAPI/Swagger UI**: API documentation
- **Spring HATEOAS**: Hypermedia links
- **Docker**: Containerization

---

## Getting Started

### Prerequisites

- **Java 17+**
- **Maven 3.x**
- **Docker & Docker Compose** (recommended)

---

### Building the Project

From the `roomBooking` directory:

```
mvn clean install
```

This compiles, tests, and packages the app as a JAR.

---

### Running with Docker Compose

Docker Compose sets up MariaDB, Flyway, RabbitMQ, and the application.

1. **Navigate to project root:**

```
cd roomBooking
```

2. **Configure Environment:**

   Update `MAIL_USERNAME` and `MAIL_PASSWORD` in `docker-compose.yml` for your SMTP service (e.g., with a Gmail App Password if needed).


3. **Build the Application:**

```
mvn clean package -DskipTests
```

4. **Start Services:**

```
docker-compose up --build
```

    - Builds the app Docker image.
    - Starts MariaDB, RabbitMQ, runs Flyway migrations, and launches the app.

Access: [http://localhost:8080](http://localhost:8080)

---

### Running Natively

If not using Docker Compose:

1. **Start MariaDB & RabbitMQ manually**
    - MariaDB on `3306`, with database `room_booking`.
    - RabbitMQ on `5672`, default `guest/guest`.

2. **Configure `application.yml`**

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
   ```

   Set JWT secrets as env vars or in `application.yml`:

   ```yaml
   jwt:
     secret: ${JWT_SECRET:...}
     refreshSecret: ${JWT_REFRESHSECRET:...}
   ```

3. **Apply Flyway Migrations** (runs with Maven build):

```
mvn clean install
```

4. **Run the App:**

```
mvn spring-boot:run
# or
java -jar target/roombooking-0.0.1-SNAPSHOT.jar
```

---

## API Documentation (Swagger UI)

Once running, access the interactive API docs at:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## Database Schema

Schema managed by Flyway. Main tables:

- **users**: User info (name, email, password, phone, role, auditing)
- **rooms**: Room details (name, description, capacity, location, auditing)
- **reservations**: Reservation info (start/end date, user, room, auditing)

Initial data loaded via:

- `V2__populate_user_table.sql`
- `V4__populate_room_table.sql`
- `V6__populate_reservation_table.sql`

---

## Default Admin User

On first startup, an admin is created if missing:

- **Email**: `admin@admin.com`
- **Password**: `admin123`
- **Role**: `ADMIN`

Use this for admin tasks.

---

## Project Structure

```
roomBooking/
├── src/main/java/com/github/alefthallys/roombooking/
│   ├── RoomBookingApplication.java      # Main entry
│   ├── annotations/                     # Custom annotations
│   ├── assemblers/                      # HATEOAS assemblers
│   ├── config/                          # Configurations
│   ├── controllers/                     # REST controllers
│   ├── dtos/                            # DTOs
│   ├── exceptions/                      # Custom/global exceptions
│   ├── mappers/                         # Entity-DTO mappers
│   ├── messaging/                       # RabbitMQ consumer
│   ├── models/                          # JPA entities
│   ├── repositories/                    # JPA repos
│   ├── security/                        # Security/JWT
│   ├── services/                        # Business logic
│   └── validadors/                      # Validators
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/                    # Flyway scripts
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

A global exception handler provides consistent JSON error responses for:

- **400 Bad Request**: Validation, malformed input
- **401 Unauthorized**: Auth failures
- **403 Forbidden**: Access denied
- **404 Not Found**: Entity missing
- **409 Conflict**: Duplicate/conflict
- **500 Internal Server Error**: Fallback

Example error response:

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

Unit and integration tests are under `src/test/java`. Run all tests:

```
mvn test
```

---

## License

MIT License. See the [LICENSE](LICENSE) file for details.