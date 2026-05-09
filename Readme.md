# VibeWall — Backend

VibeWall is an anonymous confession and feedback platform built for students. Users can share confessions, give feedback, and interact with a live feed — all in a safe, moderated environment. Content is checked by two independent AI models before it is ever saved.

---

## Features

- **Anonymous Confessions** — Post confessions without revealing your identity
- **Feedback System** — Reply to any confession with feedback
- **Live Feed** — Browse the latest confessions from all users
- **Dual AI Content Moderation** — Every confession and feedback is screened by both Google Gemini and OpenRouter (Mistral) before being saved; content flagged by either model is rejected
- **JWT Authentication** — Short-lived access tokens + long-lived refresh tokens for secure, seamless sessions
- **Auto-Deletion (TTL)** — Confessions are automatically removed from the database after 12 hours
- **Reporting System** — Users can report content; admins resolve reports from a dedicated dashboard
- **AES-GCM Encryption** — All confession and feedback content is encrypted at rest
- **Redis Caching** — Feed and confession responses are cached for fast reads
- **Admin Dashboard** — Full user and report management for administrators
- **Role-Based Access Control** — Separate permission levels for public visitors, authenticated users, and admins

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.6 |
| Database | MongoDB (Atlas) |
| Cache | Redis |
| Security | Spring Security, JWT (jjwt 0.11.5) |
| Encryption | AES/GCM/NoPadding (256-bit) |
| AI Moderation | Google Gemini 2.0 Flash + OpenRouter (Mistral 7B) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Containerization | Docker + Docker Compose |

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

### 2. Configure environment variables

Copy the example file and fill in your own values:

```bash
cp .env.example .env
```

| Variable | Description |
|---|---|
| `GEMINI_API_KEY` | Google Gemini API key |
| `OPENROUTER_API_KEY` | OpenRouter API key (`sk-or-v1-...`) |
| `ENCRYPTION_KEY` | Base64-encoded 32-byte AES key — generate with `openssl rand -base64 32` |
| `JWT_SECRET` | Long random string (min 32 chars) — generate with `openssl rand -base64 48` |
| `SPRING_DATA_MONGODB_URI` | MongoDB Atlas connection URI |
| `SPRING_DATA_REDIS_URL` | Redis connection URL |
| `ALLOWED_ORIGINS` | Frontend origin for CORS (e.g. `http://localhost:5173`) |
| `SERVER_PORT` | Server port (default `8080`) |

### 3. Build the project

```bash
mvn clean package -DskipTests
```

### 4. Run with Docker Compose

```bash
docker-compose up --build
```

The API will be available at `http://localhost:8080/api/v3`.

---

## API Documentation

Interactive API documentation is available via **Swagger UI** once the application is running:

```
http://localhost:8080/api/v3/swagger-ui.html
```

The OpenAPI JSON spec is at:

```
http://localhost:8080/api/v3/api-docs
```

---

## Access Control

| Role | Accessible Routes |
|---|---|
| Public (no token) | `/auth/**`, `/feed/**` |
| Authenticated User | `/users/**` |
| Admin | `/admin/**` |

All protected routes require a valid JWT `Bearer` token in the `Authorization` header.

---

## AI Content Moderation

Before any confession or feedback is saved, it passes through two independent AI models:

1. **Google Gemini 2.0 Flash** — Primary check with strict safety filters
2. **OpenRouter / Mistral 7B** — Secondary check for content that may pass Gemini

If **either** model flags the content as harmful, violent, hateful, sexually explicit, or otherwise unsafe, the request is rejected with a `422 Unprocessable Entity` response. Both models must clear the content before it is accepted.

---

## Security

- Passwords are hashed with BCrypt
- All confession and feedback text is encrypted with AES-GCM (256-bit) before storage
- JWT access tokens are short-lived; refresh tokens are long-lived and stored server-side
- HSTS headers are sent when running behind HTTPS (e.g. Render)
- CORS is restricted to the configured `ALLOWED_ORIGINS`

---

## Project Structure

```
src/main/java/org/example/vibewall/
├── annotation/          Custom validation annotations
├── AOP/                 Logging aspects
├── config/              Redis and CORS configuration
├── controller/          REST controllers
├── DTO/                 Request and response objects
├── encryption/          AES-GCM encryption utility
├── exception/           Custom exceptions and global handler
├── model/               MongoDB document models
├── repo/                MongoDB repositories
├── security/            JWT filter and Spring Security config
├── service/             Service interfaces
│   └── serviceImple/    Service implementations (including AI services)
└── utility/             Mappers and helpers
```

---

## Contributing

Contributions are welcome. Fork the repository, create a feature branch, and open a pull request.
