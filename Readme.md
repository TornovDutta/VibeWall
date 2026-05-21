# VibeWall — Backend

A production-ready REST API for an anonymous confession and feedback platform built for students. The backend handles secure authentication, real-time content moderation using AI, encrypted data storage, and role-based access — all containerized with Docker.

---

## What I Built

This is a full-featured backend service I designed and developed from scratch. It includes:

- A clean, layered architecture following MVC and SOLID principles
- AI-powered content moderation integrated via REST API (NVIDIA NIM / Llama 3.1)
- Server-side encryption at rest for all user-generated content (AES-256-GCM)
- Stateless JWT authentication with refresh token rotation
- Redis caching for high-performance feed responses
- MongoDB with automatic document expiry (TTL indexes)
- Swagger/OpenAPI documentation for all endpoints

---

## Core Features

| Feature | Details |
|---|---|
| Anonymous Posting | Users post confessions without exposing identity |
| AI Content Moderation | Every post is screened by LLM before being saved — unsafe content is rejected |
| JWT Auth | Short-lived access token + 7-day rotating refresh token |
| AES-256-GCM Encryption | All text content is encrypted before storage |
| Redis Caching | Feed and confession data cached for fast reads |
| Role-Based Access Control | Public, User, and Admin roles with separate route permissions |
| Auto-Deletion (TTL) | Confessions automatically expire and delete after 12 hours |
| Reporting System | Users report content; admins review from a dedicated dashboard |
| Admin Dashboard API | Full user and report management endpoints |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Database | MongoDB Atlas (NoSQL, TTL indexes) |
| Cache | Redis |
| Security | Spring Security, JWT (jjwt), BCrypt |
| Encryption | AES/GCM/NoPadding (256-bit) |
| AI Integration | NVIDIA NIM — Meta Llama 3.1 8B Instruct |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| Containerization | Docker + Docker Compose |
| Build Tool | Maven |

---

## Architecture Highlights

- **Layered Architecture** — Controller → Service → Repository separation
- **AOP Logging** — Cross-cutting concerns handled with Spring AOP
- **Global Exception Handling** — Centralized error responses using `@ControllerAdvice`
- **Custom Validation** — Annotation-based input validation
- **DTO Pattern** — Clean separation between API contracts and internal models
- **Refresh Token Rotation** — One active refresh token per user; issuing a new one invalidates the old one

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### 1. Clone the repository

```bash
git clone https://github.com/TornovDutta/VibeWall.git
cd VibeWall/backend
```

### 2. Set up environment variables

Copy the example file and fill in your values:

```bash
cp .env.example .env
```

| Variable | Description |
|---|---|
| `NVDIA_API_KEY` | NVIDIA NIM API key (`nvapi-...`) — get one at build.nvidia.com |
| `ENCRYPTION_KEY` | Base64-encoded 32-byte AES key — `openssl rand -base64 32` |
| `JWT_SECRET` | Random string (min 32 chars) — `openssl rand -base64 48` |
| `SPRING_DATA_MONGODB_URI` | MongoDB Atlas connection URI |
| `SPRING_DATA_REDIS_URL` | Redis connection URL |
| `ALLOWED_ORIGINS` | Frontend URL for CORS (e.g. `http://localhost:5173`) |
| `SERVER_PORT` | Port to run on (default `8080`) |

### 3. Build

```bash
mvn clean package -DskipTests
```

### 4. Run with Docker Compose

```bash
docker-compose up --build
```

API runs at: `http://localhost:8080/api/v3`

---

## Authentication Flow

Two-token strategy: a short-lived JWT **access token** for every request, and a rotating **refresh token** to keep sessions alive without asking the user to log in again.

```
POST /auth/login
  → 200 { "jwt": "<access-token>", "refresh": "<refresh-token>", "role": "USER" }

Authorization: Bearer <access-token>

POST /auth/refresh
  Body: { "token": "<refresh-token>" }
  → 200 { "jwt": "<new-access-token>", "refresh": "<new-refresh-token>" }

POST /auth/logout
  → 200 "Logged out successfully"
```

| Method | Path | Description |
|---|---|---|
| `POST` | `/auth/login` | Returns access + refresh tokens |
| `POST` | `/auth/refresh` | Rotates the refresh token and returns a new access token |
| `POST` | `/auth/logout` | Deletes the refresh token server-side |

---

## API Documentation

Swagger UI (interactive docs):
```
http://localhost:8080/api/v3/swagger-ui.html
```

OpenAPI JSON spec:
```
http://localhost:8080/api/v3/api-docs
```

---

## Access Control

| Role | Routes |
|---|---|
| Public | `/auth/**`, `/feed/**` |
| Authenticated User | `/users/**` |
| Admin | `/admin/**` |

All protected routes require a valid JWT `Bearer` token in the `Authorization` header.

---

## AI Content Moderation

Before any post is saved, it is sent to **Meta Llama 3.1 8B Instruct** via the NVIDIA NIM API. The model checks for:

- Hate speech, threats, and harassment
- Self-harm and violent content
- Explicit material and illegal activity

If the model flags the content, the request is rejected with `422 Unprocessable Entity`. Only clean content is saved.

---

## Security Practices

- Passwords hashed with **BCrypt**
- All stored content encrypted with **AES-256-GCM**
- JWT access tokens are short-lived; refresh tokens rotate on every use
- CORS restricted to configured origins only
- HSTS headers enabled when running behind HTTPS

---

## Project Structure

```
src/main/java/org/example/vibewall/
├── annotation/          Custom validation annotations
├── AOP/                 Logging aspects (Spring AOP)
├── config/              Redis and CORS configuration
├── controller/          REST controllers
├── DTO/                 Request and response objects
├── encryption/          AES-GCM encryption utility
├── exception/           Global exception handler
├── model/               MongoDB document models
├── repo/                MongoDB repositories
├── security/            JWT filter and Spring Security config
├── service/             Service interfaces
│   └── serviceImple/    Service implementations (including AI integration)
└── utility/             Mappers and helpers
```

---

## Contributing

Contributions are welcome. Fork the repo, create a feature branch, and open a pull request.
