# Expense Tracker API

A REST API built with Spring Boot for managing personal expenses.

Users can register, log in, receive a JWT, and manage only their own expenses.

## Tech Stack

- Java 26
- Spring Boot 4
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- JWT (JJWT)
- Maven
- H2 for testing

## Features

- User registration
- User login
- BCrypt password hashing
- JWT authentication
- Stateless security
- Expense CRUD operations
- Expense ownership per user
- Pagination
- Sorting
- Filtering by category
- Filtering by date range
- Expense statistics
- Request validation
- Global exception handling
- Flyway database migrations
- Unit tests
- Controller tests
- Repository tests
- Integration tests

## Configuration

The application uses environment variables for sensitive configuration.

### Required Environment Variables

```text
DB_PASSWORD
JWT_SECRET
```

### Optional Environment Variables

```text
DB_URL
DB_USERNAME
JWT_EXPIRATION
```

Default values:

```text
DB_URL=jdbc:postgresql://localhost:5432/expense_tracker
DB_USERNAME=postgres
JWT_EXPIRATION=3600000
```

The application configuration uses:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/expense_tracker}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}

jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:3600000}
```

Do not store database passwords or JWT secrets directly in the source code.

## Database

The application uses PostgreSQL.

Database schema changes are managed with Flyway.

Migration files are located in:

```text
src/main/resources/db/migration
```

Current migrations:

```text
V1__baseline_schema.sql
V2__assign_legacy_expenses_and_require_user.sql
```

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Hibernate validates the database schema but does not modify it automatically.

Flyway is responsible for database schema migrations.

## Authentication

Authentication is based on JWT.

### Register

```http
POST /auth/register
```

Example request:

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

A successful registration returns:

```text
201 Created
```

### Login

```http
POST /auth/login
```

Example request:

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

A successful login returns a JWT.

Example:

```json
{
  "id": 1,
  "email": "user@example.com",
  "token": "eyJ..."
}
```

Protected endpoints require the JWT in the Authorization header:

```text
Authorization: Bearer <token>
```

## Expense Endpoints

All expense endpoints require authentication.

### Create Expense

```http
POST /expenses
```

Example request:

```json
{
  "description": "Coffee",
  "amount": 4.50,
  "category": "FOOD",
  "date": "2026-09-24"
}
```

Successful response:

```text
201 Created
```

### Get Expenses

```http
GET /expenses
```

Supported query parameters:

```text
category
from
to
page
size
sortBy
direction
```

Example:

```http
GET /expenses?category=FOOD&page=0&size=5&sortBy=date&direction=desc
```

### Get Expense by ID

```http
GET /expenses/{id}
```

### Update Expense

```http
PUT /expenses/{id}
```

Example request:

```json
{
  "description": "Updated Coffee",
  "amount": 5.20,
  "category": "FOOD",
  "date": "2026-09-24"
}
```

### Delete Expense

```http
DELETE /expenses/{id}
```

Successful response:

```text
204 No Content
```

## Statistics

### Total Expense Amount

```http
GET /expenses/stats/total
```

Optional date range:

```http
GET /expenses/stats/total?from=2026-09-01&to=2026-09-30
```

### Totals by Category

```http
GET /expenses/stats/by-category
```

Optional date range:

```http
GET /expenses/stats/by-category?from=2026-09-01&to=2026-09-30
```

## Expense Categories

Supported categories:

```text
FOOD
TRANSPORT
HOUSING
ENTERTAINMENT
HEALTH
SHOPPING
OTHER
```

## Security

All `/expenses/**` endpoints require authentication.

The application uses stateless JWT authentication.

Each expense belongs to a specific user.

Users can only access, update, or delete their own expenses.

If a user tries to access an expense belonging to another user, the API returns:

```text
404 Not Found
```

Passwords are stored using BCrypt hashing.

## Validation

Expense requests are validated before they reach the service layer.

Examples of invalid requests include:

- Empty description
- Negative or zero amount
- Missing category
- Missing date
- Invalid pagination parameters
- Invalid sorting parameters
- Invalid date ranges

Validation and business errors are handled through a global exception handler.

## Testing

Tests use an H2 in-memory database.

Test configuration is located in:

```text
src/test/resources/application-test.properties
```

Flyway is disabled for the test profile.

Hibernate creates and removes the H2 schema during tests.

Run all tests with:

```bash
mvn test
```

## Run the Application

Make sure PostgreSQL is running.

The expected local database is:

```text
expense_tracker
```

Configure the required environment variables:

```text
DB_PASSWORD
JWT_SECRET
```

Then run the application with:

```bash
mvn spring-boot:run
```

Alternatively, run:

```text
ExpenseTrackerApplication
```

directly from IntelliJ IDEA.

The API runs by default at:

```text
http://localhost:8080
```

## Build

Create the application package with:

```bash
mvn clean package
```

A successful build should end with:

```text
BUILD SUCCESS
```