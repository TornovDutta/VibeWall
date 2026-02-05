# VibeWall

VibeWall is a social platform designed for sharing anonymous confessions, providing feedback, and managing user interactions through a feed system. The application focuses on user privacy and community engagement, offering a secure environment for expression.

## Features

*   **User Authentication**: Secure registration and login using JWT and Spring Security.
*   **Access & Refresh Tokens**: Implements **short-lived access tokens** and **long-lived refresh tokens** for 
   secure and seamless session management.
*   **Anonymous Confessions**: Users can post confessions without revealing their identity.
*   **Feed System**: A dynamic feed to view confessions from other users.
*   **Feedback Mechanism**: Users can provide feedback on confessions.
*   **Reporting System**: Users can report inappropriate content, which is managed by admins.
*   **Admin Dashboard**: comprehensive capabilities for administrators to manage users and reports.
*   **Auto-Deletion (TTL)**: Confessions are automatically removed after 12 hours.
*   **AI Content Moderation**: Integrates **OpenAI** and **Google Gemini** to analyze confessions and feedback for hurtful or inappropriate content before posting.
*   **AI Integration**: Utilizes Spring AI for enhanced content processing.

## Tech Stack

*   **Language**: Java 17
*   **Framework**: Spring Boot 3.5.6
*   **Database**: MongoDB
*   **Caching**: Redis
*   **Security**: Spring Security, JWT (jjwt)
*   **Containerization**: Docker

## Setup Instructions

### Prerequisites
*   Java Development Kit (JDK) 17
*   Maven
*   Docker & Docker Compose

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/TornovDutta/VibeWall.git
    cd vibewall
    ```

2.  **Configure Environment Variables:**
    Create a `.env` file in the root directory. It should **only** contain the following keys:
    ```properties
    OPENAI_API_KEY=your_openai_api_key
    GEMINI_API_KEY=your_gemini_api_key
    ```

3.  **Build the project:**
    This step is required before building the Docker image.
    ```bash
    mvn clean package -DskipTests
    ```

4.  **Run the application (Docker Only):**
    ```bash
    docker-compose up --build
    ```

## API Documentation
## 🔐 API Endpoints & Access Control

### Authentication (`/auth`) — **Public**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/auth/register` | Register a new user |
| `POST` | `/auth/login` | Login and receive access & refresh tokens |
| `POST` | `/auth/refresh` | Refresh the access token using refresh token |
| `POST` | `/auth/logout` | Logout and invalidate refresh token |

---

### Feed (`/feed`) — **Public**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/feed` | Get the feed of confessions |

---

### Confessions (`/users/confession`) — **USER**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/users/confession` | Create a new confession |
| `PUT` | `/users/confession/{id}` | Update an existing confession |
| `DELETE` | `/users/confession/{id}` | Delete a confession |

---

### Users (`/users`) — **USER**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `PUT` | `/users/me` | Update current user's profile |
| `DELETE` | `/users/me` | Delete current user's account |

---

### Reports (`/users/report`) — **USER**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/users/report` | Create a new report |
| `PUT` | `/users/report/{reportId}` | Update a report |
| `DELETE` | `/users/report/{reportId}` | Delete a report |

---

### Feedback (`/users/feedback`) — **USER**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/users/feedback/{confessionId}` | Give feedback on a confession |
| `PUT` | `/users/feedback/{confessionId}/{feedbackId}` | Update feedback |
| `DELETE` | `/users/feedback/{confessionId}/{feedbackId}` | Delete feedback |

---

### Admin (`/admin`) — **ADMIN**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/admin` | Get all users |
| `POST` | `/admin` | Add a new admin |
| `PUT` | `/admin/me` | Update admin profile |
| `DELETE` | `/admin/me` | Delete admin account |
| `GET` | `/admin/report` | Get all reports |
| `GET` | `/admin/report/{id}` | Get report by ID |
| `GET` | `/admin/report/pending` | Get all pending reports |
| `GET` | `/admin/report/pending/{id}` | Get pending report by ID |
| `PATCH` | `/admin/report/reviewed/{id}` | Review or resolve a report (status parameter required) |

---

## 🔒 Access Rules Summary

- **Public**: `/auth/**`, `/feed/**`
- **Authenticated USER**: `/users/**`
- **Authenticated ADMIN**: `/admin/**`
- **JWT Authentication** required for USER and ADMIN routes

---

## Access Control

- **Admin Only:** Admin management, user management, and deleting confessions/feedback.
- **User/Admin:** Creating confessions, adding feedback, and viewing confessions/feedback.
- **Anonymous Access:** Confessions and feedback reading can be optionally opened.

---

## Contribution

Contributions are welcome! Please fork the repository, make changes, and create a pull requestUpdate.


