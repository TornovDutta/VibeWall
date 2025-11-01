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


## REST API Endpoints

| Endpoint | HTTP Method | Description | Access | Example |
|----------|------------|-------------|--------|---------|
| `/api/admin/add` | POST | Add a new admin | Admin only | `{ "username": "admin1", "password": "pass123" }` |
| `/api/admin/update` | PUT | Update admin details | Admin only | `{ "id": "64f1a3", "username": "admin2" }` |
| `/api/admin/deleteAdmin/{id}` | DELETE | Delete an admin by ID | Admin only | `/api/admin/deleteAdmin/64f1a3` |
| `/api/user/add` | POST | Add a new user | Admin only | `{ "username": "user1", "email": "user@example.com" }` |
| `/api/user/update` | PUT | Update user details | Admin only | `{ "id": "64f1a4", "email": "newmail@example.com" }` |
| `/api/user/delete/{id}` | DELETE | Delete a user by ID | Admin only | `/api/user/delete/64f1a4` |
| `/api/confession/create` | POST | Create a new confession | User/Admin | `{ "content": "Feeling stressed today" }` |
| `/api/confession/update/{id}` | PUT | Update a confession by ID | Admin only | `{ "content": "Updated confession" }` |
| `/api/confession/delete/{id}` | DELETE | Delete a confession by ID | Admin only | `/api/confession/delete/64f1a5` |
| `/api/confession/` | GET | Get all confessions | User/Admin | `/api/confession/` |
| `/api/confession/{id}` | GET | Get confession by ID | User/Admin | `/api/confession/64f1a5` |
| `/api/feedback/{id}` | POST | Add feedback to confession | User/Admin | `{ "message": "Stay strong!" }` |
| `/api/feedback/confession/{id}` | GET | Get all feedback for a confession | User/Admin | `/api/feedback/confession/64f1a5` |
| `/api/feedback/{id}` | GET | Get feedback by ID | User/Admin | `/api/feedback/64f1a6` |
| `/api/feedback/update/{id}` | PUT | Update feedback by ID | User/Admin | `{ "message": "Updated feedback" }` |
| `/api/feedback/{id}` | DELETE | Delete feedback by ID | Admin only | `/api/feedback/64f1a6` |

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


