# 🚀 RD System Backend

## 📌 Project Overview

The **Recurring Deposit (RD) Management System Backend** is a secure and scalable REST API built using **Spring Boot**.
It enables admins to manage RD accounts, track deposits, and monitor system activity with advanced security features.

---

## ✨ Key Features

### 🔐 Authentication & Security

* JWT-based Authentication
* Role-based Access Control (**SUPER_ADMIN / ADMIN / USER**)
* Secure password encryption (BCrypt)
* Login & Logout tracking with email alerts

### 👑 Admin Functionalities

* Create and manage users
* Create new admins (only by SUPER_ADMIN)
* View all RD accounts
* Monitor deposits and passbooks

### 💰 RD & Deposit Management

* Add monthly deposits
* View passbook (transaction history)
* Calculate interest and maturity
* Late fine calculation system

### 📧 Email Notification System (🔥 Advanced)

* Admin login alert (with IP, location, device)
* Admin logout alert
* Security alert system

### 🌍 Tracking System (🔥 Unique Feature)

* IP Address tracking
* Location detection (via external API)
* Device & Browser detection

---

## 🛠️ Tech Stack

* **Backend:** Java, Spring Boot
* **Security:** Spring Security, JWT
* **Database:** MySQL
* **Build Tool:** Maven
* **Other:** REST APIs, Email (JavaMailSender)

---

## 📂 Project Structure

```
src/main/java/com/csrd/RDSystemcd/
│
├── controller      # API endpoints
├── service         # Business logic
├── repo            # Database operations
├── entity          # JPA entities
├── config          # Security & JWT config
```

---

## ⚙️ Setup Instructions

### 1️⃣ Clone Repository

```bash
git clone https://github.com/Rahulgupta7070/RD_System_Backend.git
cd RD_System_Backend
```

---

### 2️⃣ Configure Database

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rd_system
spring.datasource.username=root
spring.datasource.password=your_password
```

---

### 3️⃣ Configure Environment Variables (🔐 IMPORTANT)

Do NOT store credentials in code.

Set environment variables:

```bash
EMAIL=your_email@gmail.com
EMAIL_PASS=your_app_password
```

And in `application.properties`:

```properties
spring.mail.username=${EMAIL}
spring.mail.password=${EMAIL_PASS}
```

---

### 4️⃣ Run Application

```bash
mvn spring-boot:run
```

---

## 🔗 API Endpoints (Sample)

| Endpoint               | Method | Description                     |
| ---------------------- | ------ | ------------------------------- |
| `/auth/login`          | POST   | Admin/User Login                |
| `/auth/create-admin`   | POST   | Create Admin (SUPER_ADMIN only) |
| `/auth/logout`         | POST   | Logout with alert               |
| `/rdusers/saveUser`    | POST   | Add User                        |
| `/passbook/{rid}`      | GET    | Get Passbook                    |
| `/scheduler/calculate` | GET    | Interest Calculator             |

---

## 📸 Highlight Features

* 🔥 Real-time Email Alerts on Login/Logout
* 🔐 Role-Based Security System
* 🌍 IP + Location Tracking
* 💰 RD Interest Calculation System

---

## 👨‍💻 Author

**Rahul Kumar**

---

## 📢 Future Enhancements

* Login History Dashboard
* Customer Portal
* PDF Passbook Download
* Advanced Analytics Dashboard

---

## ⭐ Support

If you like this project, give it a ⭐ on GitHub!
