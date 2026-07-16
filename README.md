# Trader Workbook Backend

Java/Spring Boot backend for the Trader Diary application.

## Stack

- Java 21
- Spring Boot 4.1.0
- PostgreSQL 15
- JWT authentication
- OpenAPI (Swagger UI)

## Quick start (Docker)

```bash
docker compose up --build
```

API: http://localhost:8000  
Swagger UI: http://localhost:8000/docs

Default admin credentials (from env):
- username: `admin`
- password: `admin`

## Local development

1. Start PostgreSQL (or use Docker for db only):

```bash
docker compose up db -d
```

Database is exposed on host port **5433** (5432 may already be in use by other projects).

2. Copy environment variables:

```bash
cp .env.example .env
```

3. Run the application:

```bash
./gradlew bootRun
```

## Environment variables

| Variable | Default | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/trader` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `trader` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `trader` | Database password |
| `JWT_SECRET` | (dev default) | Secret key for JWT signing |
| `JWT_ACCESS_TTL` | `30m` | Access token lifetime |
| `JWT_REFRESH_TTL` | `7d` | Refresh token lifetime |
| `ADMIN_USERNAME` | `admin` | Initial admin username |
| `ADMIN_PASSWORD` | `admin` | Initial admin password |
| `SERVER_PORT` | `8000` | HTTP port |

## API overview

### Auth (`/auth`)
- `POST /auth/register` — register user
- `POST /auth/login` — get access + refresh tokens
- `POST /auth/refresh` — refresh access token
- `GET /auth/me` — current user info

### Trades (`/api/v1/trades`, auth required)
- `GET /` — list trades (filters: `status`, `security_id`, `date_from`, `date_to`, pagination: `page`, `size`; admin: `?all=true`)
- `POST /` — create trade
- `GET /{id}` — get trade
- `PATCH /{id}` — update trade
- `DELETE /{id}` — delete trade

### Admin (`/api/v1/admin`, admin role required)
- CRUD for `/security-types`, `/issuers`, `/securities`
- `GET /users`, `PATCH /users/{id}` — user management

## Build

```bash
./gradlew build
```

## Tests

```bash
./gradlew test
```
