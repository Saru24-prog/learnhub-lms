# LearnHub – Learning Management System

LearnHub is a full-stack Learning Management System (LMS) built with **Spring Boot** and **MySQL**, inspired by platforms like Moodle. Lecturers create courses, share weekly learning materials and set assignments. Students enroll with a key, download materials, submit their work and receive grades and feedback.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.2-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8-blue)
![JWT](https://img.shields.io/badge/Auth-JWT-black)

---

## Features

### Authentication & roles
- Register and login with **JWT** (stateless authentication)
- Passwords hashed with **BCrypt**
- Three roles: **Admin**, **Lecturer**, **Student**, each with its own portal
- Role-based access control on every API endpoint

### Admin
- Dashboard with users, courses and enrollment statistics
- Manage users and change roles (e.g. promote a student to lecturer)
- Create and manage all courses (admin can also act as a lecturer)

### Lecturer
- Create, edit and delete courses with an **enrollment key**
- View enrolled students and remove students from a course
- Add weekly sections (Week 1, Week 2…) with **file uploads** (PDF, slides, Word…) and **links** (YouTube, Google Drive…)
- Create **assignments** with a deadline and maximum marks
- View submissions (on time / late), download student files, give **marks and feedback**

### Student
- Browse courses and enroll using the enrollment key
- Open a course to view weekly content and download materials
- Submit assignments (re-submit allowed until graded); late submissions are flagged automatically
- See grades and lecturer feedback

---

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4.2 (Web MVC, Data JPA, Validation, Security) |
| Security | Spring Security, JWT (jjwt 0.12), BCrypt |
| Database | MySQL 8, Hibernate |
| Frontend | HTML, CSS, vanilla JavaScript (served from Spring Boot) |
| Build | Maven |
| Other | Lombok |

---

## Architecture

```
Browser (HTML/CSS/JS)
        │  REST API + JSON  (Authorization: Bearer <JWT>)
        ▼
Controller  →  Service  →  Repository  →  MySQL
   (REST)      (business      (Spring
                 rules)       Data JPA)
        │
        └── FileStorageService → /uploads (course materials & submissions)
```

- **Controllers** handle HTTP requests and validation
- **Services** contain business rules (ownership checks, deadlines, grading limits)
- **Repositories** use Spring Data JPA
- **DTOs** keep entities (and passwords) out of API responses
- A **global exception handler** returns clean JSON errors (400, 401, 403, 404, 409, 413)

---

## Project structure

```
src/main/java/com/example/learnhub
├── config/        SecurityConfig, DataSeeder (creates default admin)
├── controller/    Auth, User, Admin, Lecturer, Student, Content, Assignment
├── dto/           Request / response objects
├── entity/        User, Course, Enrollment, Section, Material, Assignment, Submission
├── exception/     Custom exceptions + GlobalExceptionHandler
├── repository/    Spring Data JPA repositories
├── security/      JwtService, JwtAuthFilter, CustomUserDetailsService
├── service/       Business logic
└── storage/       FileStorageService (file uploads)

src/main/resources
├── application.properties
└── static/        login.html, admin.html, lecturer.html, student.html, css/, js/
```

---

## Getting started

### Prerequisites
- Java 21
- Maven (or use the included `mvnw`)
- MySQL 8

### 1. Create the database
```sql
CREATE DATABASE learnhub_db;
```

### 2. Configure `src/main/resources/application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/learnhub_db
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

jwt.secret=YOUR_BASE64_SECRET_KEY
```
> Use your own `jwt.secret` (a Base64 string of at least 32 bytes) and never commit real passwords.

### 3. Run
```bash
./mvnw spring-boot:run
```
On Windows: `mvnw.cmd spring-boot:run`

Open **http://localhost:8082**

### Default admin account
| Email | Password |
|---|---|
| admin@learnhub.com | admin123 |

> Change this password before deploying anywhere public.

---

## API overview

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register (as student) |
| POST | `/api/auth/login` | Public | Login, returns JWT |
| GET | `/api/users/me` | Logged in | Current user |
| GET | `/api/admin/users` | Admin | All users |
| PUT | `/api/admin/users/{id}/role?role=LECTURER` | Admin | Change role |
| GET | `/api/admin/courses` | Admin | All courses |
| GET / POST | `/api/lecturer/courses` | Lecturer, Admin | List / create courses |
| PUT / DELETE | `/api/lecturer/courses/{id}` | Lecturer, Admin | Update / delete course |
| GET | `/api/lecturer/courses/{id}/students` | Lecturer, Admin | Enrolled students |
| GET | `/api/student/courses` | Student | Browse courses |
| GET | `/api/student/courses/my` | Student | My courses |
| POST / DELETE | `/api/student/courses/{id}/enroll` | Student | Enroll (with key) / unenroll |
| GET | `/api/courses/{id}/content` | Enrolled / owner | Weekly content |
| POST | `/api/courses/{id}/sections` | Owner | Add section |
| POST | `/api/sections/{id}/files` | Owner | Upload file (multipart) |
| POST | `/api/sections/{id}/links` | Owner | Add link |
| GET | `/api/materials/{id}/download` | Enrolled / owner | Download material |
| GET / POST | `/api/courses/{id}/assignments` | Enrolled / owner | List / create assignments |
| GET | `/api/assignments/{id}/submissions` | Owner | All submissions |
| POST | `/api/assignments/{id}/submit` | Enrolled student | Submit file (multipart) |
| PUT | `/api/submissions/{id}/grade` | Owner | Give marks + feedback |
| GET | `/api/submissions/{id}/download` | Owner / submitting student | Download submission |

*"Owner" = the course lecturer or an admin.*

---

## Screenshots

> Add your screenshots to a `screenshots/` folder and link them here.

| Login | Admin dashboard |
|---|---|
| ![Login](screenshots/login.png) | ![Admin](screenshots/admin-dashboard.png) |

| Course content | Assignments |
|---|---|
| ![Content](screenshots/course-content.png) | ![Assignments](screenshots/assignments.png) |

---

## Roadmap
- [x] JWT authentication and role-based access
- [x] Courses and enrollment keys
- [x] Weekly content with file uploads and links
- [x] Assignments with deadlines, submissions and grading
- [ ] Announcements
- [ ] Quizzes (MCQ with timer and auto-marking)
- [ ] Email notifications
- [ ] Docker + cloud deployment

---

## Author

**Your Name**
- GitHub: [your-username](https://github.com/your-username)
- LinkedIn: [your-profile](https://linkedin.com/in/your-profile)
