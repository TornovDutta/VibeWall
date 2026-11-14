# VibeWall — Backend

A production-ready REST API for an anonymous confession, interactive opinion poll, and Q&A platform built for students. The backend handles secure authentication, real-time content moderation using AI, encrypted data storage, and role-based access — all containerized with Docker.

---

## What I Built

This is a full-featured backend service designed and developed from scratch. It includes:

- A clean, layered architecture following MVC and SOLID principles
- AI-powered content moderation and companion chatbot integrated via LLM Inference APIs
- Real-time anonymous companion chatbot (`/api/v3/chat`)
- Server-side encryption at rest for user-generated content (AES-256-GCM)
- Stateless JWT authentication with refresh token rotation
- Redis caching for high-performance feed responses
- MongoDB with automatic document expiry (TTL indexes)
- Interactive Opinion Polls engine with live voting percentages and single-vote enforcement
- Anonymous Q&A Wall engine supporting community questions and targeted wall inboxes with public/private replies
- In-app notification service alerting users of received questions, answers, and interactions
- Swagger/OpenAPI documentation for all endpoints

---

## Core Features

| Feature | Details |
|---|---|
| **Anonymous Confessions** | Users post confessions without exposing real-world identity or trackable metadata |
| **Interactive Opinion Polls** | Create community polls with 2–6 options; live vote counting, percentages, and duplicate prevention |
| **Anonymous Q&A Walls** | Ask questions anonymously to the community or to specific user walls with public and private reply channels |
| **AI Content Moderation** | Every confession, poll, and question is screened by LLM before persistence — unsafe content is rejected with `422` |
| **JWT Auth & Silent Rotation** | Short-lived access token + 7-day rotating refresh token with single-active-session revocation |
| **AES-256-GCM Encryption** | All confession and feedback text is encrypted at rest using authenticated AES-GCM |
| **Redis Caching** | Feed read endpoints are cached in memory for sub-millisecond throughput |
| **Role-Based Access Control** | Public, Authenticated User, and Administrator roles with separate security filters |
| **Auto-Deletion (TTL)** | Confessions automatically expire and delete after 12 hours via MongoDB native TTL index |
| **In-App Notifications** | Real-time alerts for questions received, replies, reactions, and comments |
| **Reporting System** | Users report inappropriate content; admins review, approve, or reject from a dedicated panel |
| **Admin Dashboard API** | Full user management, content moderation audit, system error logs, and AI health telemetry |
| **System Error Logs** | Auto-captures user exceptions and client errors with 7-day TTL index for administrator auditing |
| **AI Health Monitoring** | Real-time tracking of AI provider uptime, latency, fallback heuristics, and on-demand ping tests |

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 17 LTS |
| **Framework** | Spring Boot 3.5 |
| **Database** | MongoDB Atlas (NoSQL, TTL indexes, atomic vote sets) |
| **Cache** | Redis |
| **Security** | Spring Security 6, JWT (jjwt), BCrypt |
| **Encryption** | AES/GCM/NoPadding (256-bit) with deterministic IV derivation |
| **AI Integration** | LLM Inference APIs (e.g. Meta Llama 3.1 / Qwen 2.5 via Router API) |
| **API Docs** | SpringDoc OpenAPI 3 (Swagger UI at `/api/v3/swagger-ui.html`) |
| **Containerization** | Docker + Docker Compose |
| **Build Tool** | Maven 3.8+ |

---

## Architecture Highlights

- **Layered Architecture** — Controller → Service → Repository strict separation
- **Aspect-Oriented Programming (AOP)** — Cross-cutting concerns handled cleanly via `LoggingAspect` and `ReportAspect`
- **Global Exception Handling** — Centralized error payloads using `@ControllerAdvice`
- **Annotation-based Validation** — Strong password validation and payload constraint checking
- **DTO Pattern** — Strict boundary between database documents and external API contracts
- **Refresh Token Rotation** — One active refresh token per user; rotation invalidates prior tokens to prevent replay attacks

---

## REST API Overview

Base URL: `/api/v3`

### Authentication (`/auth`)
- `POST /auth/register` — Register a new student account
- `POST /auth/login` — Login and receive JWT access token + refresh token
- `POST /auth/refresh` — Rotate refresh token and issue new access token
- `POST /auth/logout` — Revoke active refresh session

### Live Wall & Feed (`/feed`)
- `GET /feed/` — Get cached public feed of confessions

### Opinion Polls (`/polls`)
- `GET /polls` — List active opinion polls with live option vote counts and percentages (Public)
- `POST /polls` — Create a new opinion poll with 2–6 custom options (Authenticated)
- `GET /polls/{id}` — Get single poll details (Public)
- `POST /polls/{id}/vote` — Cast an anonymous vote for a specific option (Authenticated)
- `DELETE /polls/{id}` — Delete own poll or admin deletion (Authenticated)

### Anonymous Q&A (`/qa`)
- `GET /qa` — Get all public community questions for the live feed (Public)
- `POST /qa/ask` — Ask a question to the community (Public / Anonymous)
- `POST /qa/ask/{targetUserId}` — Ask a question to a specific user wall inbox (Public / Anonymous)
- `GET /qa/wall/{targetUserId}` — Get answered public questions on a user's wall (Public)
- `GET /qa/inbox` — Get authenticated user's Q&A inbox (Authenticated)
- `POST /qa/{questionId}/reply` — Reply to a question publicly or privately (Authenticated)
- `DELETE /qa/{questionId}` — Delete a question (Authenticated)
- `GET /qa/my-asked` — List questions asked by the current user (Authenticated)
- `GET /qa/pending-count` — Count of pending unanswered inbox questions (Authenticated)

### Confessions & Feedback (`/users`, `/feedback`, `/vibes`)
- `POST /users/confession` — Submit encrypted confession (AI moderated)
- `PUT /users/confession/{id}` — Edit own confession within 12h window
- `DELETE /users/confession/{id}` — Delete own confession
- `POST /feedback/{confessionId}` — Add feedback comment to confession
- `POST /vibes/{confessionId}/react` — React with an emoji vibe

### Notifications (`/notifications`)
- `GET /notifications` — List in-app notifications
- `GET /notifications/unread-count` — Unread notification badge count
- `PUT /notifications/{id}/read` — Mark notification as read
- `PUT /notifications/read-all` — Mark all notifications as read

### Admin & Moderation (`/admin`, `/reports`)
- `GET /admin/users` — List platform users
- `DELETE /admin/users/{id}` — Ban / delete user
- `GET /admin/report` — View all content reports
- `PATCH /admin/report/Reviewed/{id}` — Approve or reject flagged content
- `GET /admin/logs` — List recent system and user error logs (Admin only)
- `DELETE /admin/logs` — Clear all error logs (Admin only)
- `POST /admin/logs/client` — Ingest client-side error telemetry (Public / Client)
- `GET /admin/ai-status` — Get real-time AI provider health, latency, and fallback state (Admin only)
- `POST /admin/ai-test` — Trigger live connectivity ping test for AI provider (Admin only)
- `POST /reports` — Submit abuse report for moderation

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (optional, for containerized run)

### 1. Set up environment variables

Copy `.env.example` to `.env` and fill in credentials:

```bash
cp .env.example .env
```

| Variable | Description |
|---|---|
| `HF_TOKEN` | Hugging Face user access token (`hf_...`) with Inference API access |
| `HF_MODEL` | Hugging Face model identifier (e.g. `Qwen/Qwen2.5-72B-Instruct`) |
| `ENCRYPTION_KEY` | Base64-encoded 32-byte AES key — generate via `openssl rand -base64 32` |
| `JWT_SECRET` | Base64-encoded 48-byte secret — generate via `openssl rand -base64 48` |
| `SPRING_DATA_MONGODB_URI` | MongoDB Atlas connection URI |
| `SPRING_DATA_REDIS_URL` | Redis connection URL |
| `ALLOWED_ORIGINS` | Frontend URL for CORS (e.g. `http://localhost:5173,https://vibewall.me`) |
| `SERVER_PORT` | Port to run on (default `8080`) |

### 2. Run with Maven

```bash
./mvnw clean spring-boot:run
```

The server starts on `http://localhost:8080/api/v3`.  
Interactive Swagger documentation is available at `http://localhost:8080/api/v3/swagger-ui.html`.

### 3. Run with Docker Compose

```bash
docker-compose up --build -d
```
