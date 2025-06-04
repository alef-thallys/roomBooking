# Room Booking Application

The Room Booking Application is a Spring Boot project designed to manage room reservations. It provides RESTful APIs for user management, room management, and reservation handling. The application includes authentication and authorization features using JWT, data persistence with MariaDB, database migrations with Flyway, and asynchronous email notifications via RabbitMQ.

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

- **User Management:** Register, view, update, and delete users. Includes roles (ADMIN, USER).
- **Room Management:** Create, view, update, and delete rooms with details like capacity, description, and location.
- **Reservation Management:** Create, view, update, and delete room reservations. Includes conflict checking for overlapping reservations.
- **Authentication & Authorization:** Secure API endpoints using JSON Web Tokens (JWT) with refresh token functionality. Role-based access control (RBAC) implemented.
- **Email Notifications:** Asynchronous email notifications for reservation confirmations using RabbitMQ.
- **Database Management:** MariaDB for data storage with Flyway for database migrations.
- **API Documentation:** Interactive API documentation using SpringDoc OpenAPI (Swagger UI).
- **Auditing:** Automatic recording of creation and last modification details for entities.
- **HATEOAS:** Hypermedia as the Engine of Application State for discoverable APIs.
- **Input Validation:** Robust validation for API request DTOs.
- **Global Exception Handling:** Centralized exception handling for consistent error responses.

---

## Technologies Used

- **Spring Boot:** 3.4.5
- **Java:** 17
- **Maven:** For dependency management and build automation
- **MariaDB:** Relational database
- **Flyway:** Database migration tool
- **Spring Data JPA:** For database interaction
- **Spring Security:** For authentication and authorization
- **JWT:** JSON Web Token library for Java
- **RabbitMQ:** Message broker for asynchronous communication
- **Spring AMQP:** Spring integration for RabbitMQ
- **Spring Mail:** For sending email notifications
- **Lombok:** To reduce boilerplate code
- **SpringDoc OpenAPI (Swagger UI):** For API documentation
- **Spring HATEOAS:** For building hypermedia-rich RESTful services
- **Docker:** For containerization of services

---

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.x.x
- Docker and Docker Compose (recommended for easy setup)

---

### Building the Project

To build the project using Maven, navigate to the `roomBooking` directory and run:

```
mvn clean install
```

This command will compile the code, run tests, and package the application into a JAR file.

---

### Running with Docker Compose

The easiest way to get the application running is using Docker Compose, which will set up MariaDB, Flyway, RabbitMQ, and the Spring Boot application.

1. **Navigate to the project root:**

```
cd roomBooking
```

2. **Update Environment Variables:**

   Before running, update the `MAIL_USERNAME` and `MAIL_PASSWORD` environment variables in `docker-compose.yml` with your Gmail credentials (or any other SMTP server details). You might need to generate an App Password for Gmail if you have 2-Factor Authentication enabled.

    ```yaml
    # roomBooking/docker-compose.yml
    ...
      app:
        ...
        environment:
          ...
          MAIL_USERNAME: your_mail@gmail.com
          MAIL_PASSWORD: your_mail_password
          ...
    ```

   Similarly, you can update `JWT_SECRET` and `JWT_REFRESHSECRET` values, ensuring they are Base64 encoded.

3. **Run Docker Compose:**

```
docker-compose up --build
```

   This command will:
    - Build the app service Docker image.
    - Start MariaDB, RabbitMQ, and Flyway (which will apply database migrations).
    - Start the Spring Boot application.

   The application will be accessible at [http://localhost:8080](http://localhost:8080). The database will be pre-populated with initial user, room, and reservation data.

---

### Running Natively

If you prefer to run the application natively without Docker Compose for the application itself, you will need to manually set up MariaDB and RabbitMQ.

1. **Start MariaDB and RabbitMQ:**
    - Ensure MariaDB is running on port 3306 with a database named `room_booking`.
    - Ensure to change the database user and password to match your MariaDB setup.
    - Ensure RabbitMQ is running on port 5672 with default credentials (guest/guest).

2. **Configure `application.yml`:**

   Ensure `src/main/resources/application.yml` and `target/classes/application.yml` (after building) are correctly configured with your database and RabbitMQ details. The `docker-compose.yml` environment variables are typically picked up by the Spring application. If running natively, you'll need to set these as environment variables in your shell or directly in `application.yml` if not using the Docker environment.

    ```yaml
    # roomBooking/src/main/resources/application.yml
    spring:
      datasource:
        url: jdbc:mariadb://localhost:3306/room_booking # Adjust if MariaDB is not on localhost
        username: your_db_user # Adjust to your MariaDB user
        password: your_db_password # Adjust to your MariaDB password
      rabbitmq:
        host: localhost # Adjust if RabbitMQ is not on localhost
        port: 5672 
        username: guest
        password: guest
      mail:
        username: your_mail@gmail.com
        password: your_mail_password
    ```

3. **Run Flyway Migrations:**

   Before running the application, ensure Flyway migrations are applied. You can run them manually using the Flyway command-line tool or by setting `spring.flyway.baseline-on-migrate=true` and `spring.jpa.hibernate.ddl-auto=none` in `application.yml` and letting Spring Boot run it on startup.

   With the provided `pom.xml`, Flyway is configured to run during the Maven build process.

4. **Run the Spring Boot Application:**

```
mvn spring-boot:run
```

   Alternatively, you can run the JAR file:

```
java -jar target/roombooking-0.0.1-SNAPSHOT.jar
```

---

## API Documentation (Swagger UI)

Once the application is running, you can access the API documentation via Swagger UI at:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

This interface allows you to explore all available endpoints, view their request/response models, and even test them directly.

---

## Database Schema

The database schema is managed by Flyway. Here's a brief overview of the tables created:

- **users:** Stores user information including name, email (unique), password, phone, and role. Also includes created_by, created_date, last_modified_by, and last_modified_date for auditing purposes.
- **rooms:** Stores room details such as name (unique), description, capacity, available status, and location. Also includes auditing columns.
- **reservations:** Stores reservation details including start_date, end_date, user_id (foreign key to users), and room_id (foreign key to rooms). Auditing columns are also present.

Initial data for users, rooms, and reservations is populated via Flyway migration scripts:
- `V2__populate_user_table.sql`
- `V4__populate_room_table.sql`
- `V6__populate_reservation_table.sql`

---

## Default Admin User

Upon the first successful startup, an administrative user is created if it does not already exist:

- **Email:** `admin@admin.com`
- **Password:** `admin123`
- **Role:** `ADMIN`

This user can be used to access restricted endpoints and manage other users, rooms, and reservations.

---

## Project Structure

The project follows a standard Spring Boot application structure:

```
roomBooking/
├── src/main/java/com/github/alefthallys/roombooking/
│   ├── RoomBookingApplication.java             # Main Spring Boot application entry point
│   ├── annotations/                          # Custom annotations (e.g., @ValidReservationDates)
│   ├── assemblers/                           # HATEOAS model assemblers
│   ├── config/                               # Spring configurations (Auditing, OpenAPI, RabbitMQ)
│   ├── controllers/                          # REST controllers for API endpoints
│   ├── dtos/                                 # Data Transfer Objects for requests and responses
│   ├── exceptions/                           # Custom exceptions and global exception handler
│   ├── mappers/                              # Mappers between DTOs and entities
│   ├── messaging/                            # RabbitMQ message consumer
│   ├── models/                               # JPA Entities (User, Room, Reservation, Auditable)
│   ├── repositories/                         # Spring Data JPA Repositories
│   ├── security/                             # Spring Security configurations, JWT utilities, CustomUserDetailsService, Admin Initializer
│   ├── services/                             # Business logic services
│   └── validadors/                           # Custom validators (e.g., for reservation dates)
├── src/main/resources/
│   ├── application.yml                       # Spring Boot application properties
│   └── db/migration/                         # Flyway migration scripts
└── src/test/java/com/github/alefthallys/roombooking/
    ├── RoomBookingApplicationTests.java      # Main test class
    ├── controllers/                         # Unit/Integration tests for controllers
    ├── security/jwt/                        # Tests for JWT components
    ├── services/                            # Unit tests for services
    ├── testBuilders/                        # Utility classes for building test data
    └── testUtils/                           # Test constants
```

---

## Error Handling

The application uses a GlobalExceptionHandler to provide consistent error responses in JSON format. Common exceptions handled include:

- `MethodArgumentNotValidException`: For validation errors in request bodies (HTTP 400 Bad Request).
- `EntityUserNotFoundException`, `EntityRoomNotFoundException`, `EntityReservationNotFoundException`: When an entity is not found (HTTP 404 Not Found).
- `EntityUserAlreadyExistsException`, `EntityRoomAlreadyExistsException`: When attempting to create a duplicate entity (HTTP 409 Conflict).
- `EntityReservationConflictException`: When a reservation conflicts with an existing one (HTTP 409 Conflict).
- `BadCredentialsException`, `InvalidJwtException`, `ResponseStatusException`: For authentication and authorization issues (HTTP 401 Unauthorized).
- `ForbiddenException`, `AuthorizationDeniedException`: For access denied issues (HTTP 403 Forbidden).
- `IllegalArgumentException`, `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException`: For invalid request arguments or malformed JSON (HTTP 400 Bad Request).
- `Exception`, `RuntimeException`: Generic fallback for unexpected errors (HTTP 500 Internal Server Error).

Error responses typically follow this structure:

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

The project includes unit and integration tests to ensure code quality and functionality. Tests are located in `src/test/java`.

To run all tests:

```
mvn test
```

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
