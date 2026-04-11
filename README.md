# RD System Backend

##  Project Description

This is a **Recurring Deposit (RD) Management System Backend** built using **Spring Boot**.
It allows admins to manage users and their deposits, and users to track their RD accounts.

---

##  Features

* Admin Login & Authentication (Spring Security + JWT)
* Create and Manage Users
* Add Monthly Deposits
* View Deposit History
* Role-based Access Control (Admin / User)
* REST APIs for frontend integration

---

##  Tech Stack

* Java
* Spring Boot
* Spring Security
* JWT Authentication
* MySQL Database
* Maven

---

##  Project Structure

* Controller → API endpoints
* Service → Business logic
* Repository → Database operations
* Entity → Database models
* Config → Security configurations

---

## ⚙️ Setup Instructions

### 1. Clone the repository

```bash
git clone https://github.com/Rahulgupta7070/RD_System_Backend.git
```

### 2. Configure Database

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rd_system
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Run the project

```bash
mvn spring-boot:run
```

---

##  API Endpoints (Example)

* `/auth/login` → Login
* `/auth/create-admin` → Create Admin
* `/rdusers/saveUser` → Add User
* `/deposit/add` → Add Deposit

---

##  Author

Rahul Kumar

---

## Note

This backend is connected with a React frontend (RD System Frontend).
