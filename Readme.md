# VibeWall

**VibeWall** is an anonymous and secure emotion-sharing platform built using **Spring Boot** and **MongoDB**.  
It allows students to freely share their thoughts and emotions while ensuring complete privacy and emotional safety.  
With advanced encryption, AI-powered moderation, reporting features, and admin management, VibeWall promotes open communication in a safe and judgment-free environment.

---

## Features

- **Anonymous Confessions:** Users can share emotions or thoughts without revealing their identity.
- **End-to-End Encryption:** Usernames, passwords, confessions, and feedback are stored in encrypted form to protect privacy—even admins cannot view the raw emotions.
- **AI-Powered Moderation:** Integrated **OpenAI** API automatically detects and blocks harmful or unsafe posts and feedback to maintain a positive space.
- **Report System:** Users can report confessions, feedback, or activities they find unsafe or inappropriate, ensuring continuous community safety.
- **Admin Management:** Admins can manage users, confessions, and reports while maintaining platform integrity.
- **User Management:** Admins can add, update, or delete user accounts securely.
- **Feedback System:** Users can share and view supportive feedback on confessions.
- **Optional Login:** Offers flexibility to engage anonymously or through optional authentication.
- **Swagger UI Integration:** Enables interactive API documentation and testing for developers.
- **Simple UI:** Intuitive and minimal interface for a distraction-free experience.

---

## Technologies Used

- **Backend:** Spring Boot
- **Database:** MongoDB
- **Language:** Java
- **Build Tool:** Maven
- **AI Moderation:** OpenAI API
- **Documentation:** Swagger UI




---

## 🧩 REST API Endpoints

| Endpoint | HTTP Method | Description | Access | Example |
|----------|--------------|-------------|--------|----------|
| `/auth` | **POST** | Register a new user | Public | `{ "username": "user1", "password": "pass123", "email": "user@example.com" }` |
| `/admin/users` | **POST** | Add a new admin | Admin only | `{ "username": "admin1", "password": "pass123" }` |
| `/admin/users/{id}` | **PUT** | Update admin details by ID | Admin only | `{ "username": "admin2" }` |
| `/admin/users/{id}` | **DELETE** | Delete an admin by ID | Admin only | `/admin/users/64f1a3` |
| `/admin/reports` | **GET** | Get all reports | Admin only | `/admin/reports` |
| `/admin/reports/{id}` | **GET** | Get report by ID | Admin only | `/admin/reports/64f1a9` |
| `/admin/reports/pending` | **GET** | Get all pending reports | Admin only | `/admin/reports/pending` |
| `/admin/reports/pending/{id}` | **GET** | Get pending report by ID | Admin only | `/admin/reports/pending/64f1a9` |
| `/admin/reports/{id}/status/{status}` | **PATCH** | Update report status (e.g., Reviewed/Resolved) | Admin only | `/admin/reports/64f1a9/status/resolved` |
| `/users/{id}` | **PUT** | Update user details by ID | Admin/User | `{ "email": "newmail@example.com" }` |
| `/users/{id}` | **DELETE** | Delete a user by ID | Admin only | `/users/64f1a4` |
| `/users/confessions` | **POST** | Create a new confession | User/Admin | `{ "content": "Feeling stressed today" }` |
| `/users/confessions/{id}` | **PUT** | Update a confession by ID | Confession Owner/Admin | `{ "content": "Updated confession" }` |
| `/users/confessions/{id}` | **DELETE** | Delete a confession by ID | Confession Owner/Admin | `/users/confessions/64f1a5` |
| `/users/confessions` | **GET** | Get all confessions | User/Admin | `/users/confessions` |
| `/users/confessions/{id}` | **GET** | Get confession by ID | User/Admin | `/users/confessions/64f1a5` |
| `/api/v2/feedbacks/confession/{confessionId}` | **POST** | Add feedback to a confession | User/Admin | `{ "feedback": "Stay strong!" }` |
| `/api/v2/feedbacks/confession/{confessionId}` | **GET** | Get all feedback for a confession | User/Admin | `/api/v2/feedbacks/confession/64f1a5` |
| `/api/v2/feedbacks/{feedbackId}` | **GET** | Get feedback by ID | User/Admin | `/api/v2/feedbacks/64f1a6` |
| `/api/v2/feedbacks/{feedbackId}` | **PUT** | Update feedback by ID | Feedback Owner/Admin | `{ "feedback": "Updated feedback" }` |
| `/api/v2/feedbacks/{feedbackId}` | **DELETE** | Delete feedback by ID | Admin only | `/api/v2/feedbacks/64f1a6` |
| `/users/reports` | **POST** | Create a new report | User/Admin | `{ "reason": "Abusive confession" }` |
| `/users/reports/{id}` | **PUT** | Update a report by ID | User/Admin | `{ "reason": "Updated report reason" }` |
| `/users/reports/{id}` | **DELETE** | Withdraw a report | User/Admin | `/users/reports/64f1a9` |
---

## Project Structure

```
org.example.vibewall
├── controller
│   ├── AdminController.java
│   ├── UserController.java
│   ├── ConfessionController.java
│   └── FeedbackController.java
├── model
│   ├── Users.java
│   ├── Confession.java
│   └── Feedback.java
├── service
│   ├── UsersService.java
│   ├── ConfessionService.java
│   └── FeedbackService.java
├── repository
└── VibeWallApplication.java
```

---

## Usage

1. Clone the repository:
   ```bash
   git clone https://github.com/TornovDutta/VibeWall.git
   ```
2. Open the project in your IDE (IntelliJ/Eclipse).
3. Configure **MongoDB** connection in `application.properties`.
4. Run the Spring Boot application.
5. Use Postman or any REST client to interact with the APIs.

---

## Access Control

- **Admin Only:** Admin management, user management, and deleting confessions/feedback.
- **User/Admin:** Creating confessions, adding feedback, and viewing confessions/feedback.
- **Anonymous Access:** Confessions and feedback reading can be optionally opened.

---

## Contribution

Contributions are welcome! Please fork the repository, make changes, and create a pull requestUpdate.


