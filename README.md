# 🏦 AI Banking Assistant API

<p align="center">

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.x-brightgreen)
![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-success)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue)
![JWT](https://img.shields.io/badge/JWT-Authentication-red)
![Flyway](https://img.shields.io/badge/Flyway-Migrations-brown)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger-green)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

</p>

Enterprise-grade Banking Backend built with **Java 17**, **Spring Boot 3.5**, **Spring Security**, **JWT Authentication**, **Refresh Token Rotation**, **Device-aware Session Management**, **PostgreSQL**, and an **AI-ready architecture**.

This project demonstrates enterprise backend engineering practices by implementing secure banking operations, financial transaction processing, concurrency control, idempotent transactions, refresh token rotation, session lifecycle management, and scalable architecture that will power an AI Banking Assistant.

---

# 🌟 Highlights

✔ Enterprise-grade Authentication

✔ Refresh Token Rotation

✔ Device-aware Session Management

✔ Optimistic Locking

✔ Transaction Idempotency

✔ Ownership-based Authorization

✔ Flyway Database Versioning

✔ Global Exception Handling

✔ AI-ready Architecture

---

# 🚀 Features

## 🔐 Authentication & Authorization

- User Registration
- Admin Registration
- JWT Authentication
- Refresh Token Authentication
- Refresh Token Rotation
- Persistent Refresh Token Storage
- Secure Refresh Endpoint
- Device-aware Login
- Session-based JWT Validation
- Role-Based Access Control (USER / ADMIN)
- BCrypt Password Encryption
- Secure Endpoint Protection
- Ownership Validation

---

## 💻 Enterprise Session Management

- Device-aware Login Sessions
- Browser Detection
- Operating System Detection
- Device Class Detection
- Device Name Detection
- IP Address Tracking
- Active Session Tracking
- View Active Sessions
- Logout Current Session
- Logout Specific Session
- Logout Other Sessions
- Logout All Sessions
- Refresh Token Revocation
- Session Lifecycle Management

---

## 🏦 Account Management

- Create Bank Account
- View Current User Accounts
- Account Validation
- Balance Inquiry

### Account Status

- ACTIVE
- FROZEN
- CLOSED

Additional Features

- Ownership Validation
- Optimistic Locking

---

## 💸 Transaction Management

- Deposit Funds
- Withdraw Funds
- Transfer Funds
- Transaction History
- Automatic Transaction Reference Generation
- Sufficient Balance Validation
- Duplicate Request Detection
- Transaction Idempotency
- Safe Retry Support

---

## 👨‍💼 Administrative Features

- Register Admin Users
- View All Users
- View All Accounts
- Freeze Accounts
- Unfreeze Accounts
- Close Accounts

---

## ⚙ Enterprise Backend Features

- Layered Architecture
- SOLID Principles
- DTO Pattern
- Repository Pattern
- Service Layer
- Global Exception Handling
- Validation
- Centralized API Response Wrapper
- Transaction Management
- Optimistic Locking
- Request Hashing
- Cached Response Replay
- Flyway Database Versioning
- PostgreSQL Persistence
- Swagger Documentation
- Environment-based Configuration
- Spring Security Integration

---

# 🏛 Architecture Overview

```
                        Client
                           │
                           │
                JWT Authentication
                           │
                           ▼
                 Spring Security Filter
                           │
                           ▼
                JwtAuthenticationFilter
                           │
             ┌─────────────┴─────────────┐
             │                           │
             ▼                           ▼
      Validate JWT              Validate Session
             │                           │
             └─────────────┬─────────────┘
                           ▼
                  Business Services
                           │
                           ▼
                   Spring Data JPA
                           │
                           ▼
                     PostgreSQL
```

---

# 🔐 Authentication Flow

```
User Login
    │
    ▼
AuthenticationManager
    │
    ▼
Generate Refresh Token
    │
    ▼
Persist Refresh Token
    │
    ▼
Generate JWT
(Session ID Embedded)
    │
    ▼
Return Tokens
```

---

# 🔄 Refresh Token Rotation

```
Client
   │
Refresh Token
   │
   ▼
Verify Refresh Token
   │
   ▼
Revoke Old Refresh Token
   │
   ▼
Issue New Refresh Token
   │
   ▼
Generate New JWT
   │
   ▼
Return New Tokens
```

---

# 💻 Session Management Flow

```
Login
   │
   ▼
Create Session
   │
   ▼
Store Device Information
   │
   ▼
Generate JWT
(Session ID Included)
   │
   ▼
Every Request
   │
   ▼
Validate JWT
   │
   ▼
Validate Session
   │
   ▼
Continue Request
```

---

# 🔒 Banking Security Model

```
                User
                  │
                  ▼
         JWT Authentication
                  │
                  ▼
      Ownership Validation
                  │
                  ▼
     Account Status Validation
                  │
                  ▼
     Idempotency Validation
                  │
                  ▼
      Optimistic Locking
                  │
                  ▼
        Banking Operation
```

---

# 📂 Project Structure

```text
src
└── main
    ├── java
    │   └── com.ridoh.aibankingassistant
    │       ├── auth
    │       ├── account
    │       ├── transaction
    │       ├── security
    │       ├── common
    │       ├── config
    │       ├── user
    │       ├── idempotency
    │       └── ai
    │
    └── resources
        ├── db
        │   └── migration
        ├── application.yml
        └── application-dev.yml
```

---

# ⭐ Design Principles

- Clean Architecture
- SOLID Principles
- Separation of Concerns
- Stateless Authentication
- Enterprise Security
- Scalable REST APIs
- Maintainable Codebase
- AI-ready Backend Design

---

# 🛠 Technology Stack

```
Backend
--------
Java 17
Spring Boot 3.5.x
Spring Security
Spring Data JPA
Hibernate

Database
--------
PostgreSQL
Flyway

Authentication
--------------
JWT
Refresh Tokens

Documentation
-------------
Swagger / OpenAPI

Build Tool
----------
Maven

Utilities
---------
Lombok
```

---

# 📚 API Modules

## 🔐 Authentication

| Method | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/v1/auth/register` | Register a new user |
| POST | `/api/v1/auth/admin/register` | Register an administrator |
| POST | `/api/v1/auth/login` | Authenticate user |
| POST | `/api/v1/auth/refresh` | Refresh access token |

---

## 💻 Session Management

| Method | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/auth/sessions` | View active sessions |
| DELETE | `/api/v1/auth/sessions/{sessionId}` | Logout a specific session |
| DELETE | `/api/v1/auth/sessions/others` | Logout all other sessions |
| DELETE | `/api/v1/auth/sessions` | Logout all sessions |

---

## 🏦 Accounts

| Method | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/v1/accounts` | Create bank account |
| GET | `/api/v1/accounts/me` | View current user's accounts |
| GET | `/api/v1/accounts/{accountNumber}/balance` | Retrieve account balance |
| GET | `/api/v1/accounts/validate/{accountNumber}` | Validate account |

---

## 💸 Transactions

| Method | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/v1/transactions/deposit` | Deposit funds |
| POST | `/api/v1/transactions/withdraw` | Withdraw funds |
| POST | `/api/v1/transactions/transfer` | Transfer funds |
| GET | `/api/v1/transactions/account/{accountNumber}` | Transaction history |

---

## 👨‍💼 Administration

| Method | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/admin/users` | View all users |
| GET | `/api/v1/admin/accounts` | View all accounts |
| PUT | `/api/v1/admin/accounts/{accountNumber}/freeze` | Freeze account |
| PUT | `/api/v1/admin/accounts/{accountNumber}/unfreeze` | Unfreeze account |
| PUT | `/api/v1/admin/accounts/{accountNumber}/close` | Close account |

---

# 📌 Current Project Status

## ✅ Completed Milestones

### 🔐 Authentication

- JWT Authentication
- Stateless Authentication
- Refresh Token Authentication
- Refresh Token Rotation
- Persistent Refresh Token Storage
- Secure Refresh Endpoint
- BCrypt Password Encryption
- Role-Based Authorization
- Ownership Validation

---

### 💻 Session Management

- Device-aware Sessions
- Browser Detection
- Operating System Detection
- Device Class Detection
- Device Name Detection
- IP Address Tracking
- Session Identification
- Active Session Listing
- Logout Current Session
- Logout Specific Session
- Logout Other Sessions
- Logout All Sessions
- Session Revocation
- Session-based JWT Validation

---

### 🏦 Banking

- Account Creation
- Account Validation
- Balance Inquiry
- Deposit Funds
- Withdraw Funds
- Transfer Funds
- Transaction History
- Account Status Management
- Ownership Validation
- Optimistic Locking

---

### 💸 Financial Processing

- Transaction Idempotency
- Duplicate Request Detection
- Safe Retry Support
- Automatic Transaction Reference Generation

---

### ⚙ Enterprise Backend

- Flyway Database Versioning
- PostgreSQL Integration
- Swagger Documentation
- Global Exception Handling
- Validation Framework
- Environment-based Configuration
- Layered Architecture
- SOLID Principles
- Repository Pattern
- DTO Pattern

---

# 🚧 Currently In Progress

## 🤖 Artificial Intelligence

- AI Banking Assistant
- OpenAI Integration
- AI Conversation Management

---

# 📅 Planned Features

## 🔐 Authentication & Security

- Password Change Session Revocation
- Scheduled Session Cleanup
- Multi-Factor Authentication (MFA)
- Login Notifications
- Suspicious Device Detection
- Session Expiration Policies

---

## 🏦 Banking

- Audit Logging
- Beneficiary Management
- Transaction Reversal
- Daily Transfer Limits
- Scheduled Transfers
- Standing Orders
- Account Statements
- Bank Notifications
- Fraud Detection & Monitoring

---

## 🤖 Artificial Intelligence

- AI Banking Assistant
- OpenAI Chat Integration
- Spending Analysis
- Intelligent Financial Insights
- AI Customer Support
- Intelligent Transaction Explanation
- AI Fraud Detection
- Personalized Banking Recommendations

---

# 🗺 Project Roadmap

## Phase 1 — Foundation ✅

- [x] Spring Boot Setup
- [x] PostgreSQL Configuration
- [x] Spring Security
- [x] JWT Authentication
- [x] Swagger Documentation
- [x] Flyway Migration
- [x] Environment Configuration

---

## Phase 2 — Banking Core ✅

- [x] User Registration
- [x] Admin Registration
- [x] Account Creation
- [x] Balance Inquiry
- [x] Deposit
- [x] Withdrawal
- [x] Transfer
- [x] Transaction History
- [x] Account Status Management

---

## Phase 3 — Enterprise Security ✅

- [x] Refresh Tokens
- [x] Refresh Token Rotation
- [x] Ownership Validation
- [x] Optimistic Locking
- [x] Transaction Idempotency
- [x] Device-aware Session Management
- [x] Active Sessions
- [x] Logout Current Session
- [x] Logout Other Sessions
- [x] Logout All Sessions
- [x] Session-based JWT Validation

---

## Phase 4 — Enterprise Banking 🚧

- [ ] Audit Logging
- [ ] Beneficiary Management
- [ ] Daily Transfer Limits
- [ ] Account Statements
- [ ] Scheduled Transfers
- [ ] Standing Orders
- [ ] Fraud Detection

---

## Phase 5 — Artificial Intelligence 🚧

- [ ] OpenAI Integration
- [ ] AI Banking Assistant
- [ ] AI Chat
- [ ] Financial Insights
- [ ] Spending Analysis
- [ ] AI Customer Support
- [ ] AI Fraud Detection

---

# 🎯 Project Goals

This project demonstrates real-world enterprise backend engineering practices, including:

- Enterprise Java Development
- Secure Banking System Design
- Enterprise Authentication & Authorization
- Device-aware Session Management
- Financial Transaction Processing
- Transaction Idempotency
- Concurrency Handling
- Optimistic Locking
- Spring Security Best Practices
- REST API Design
- Database Versioning with Flyway
- Clean Architecture
- SOLID Principles
- Scalable Enterprise Backend Development
- AI-ready Banking Architecture

---

# 📈 Project Progress

| Module | Status |
|----------|--------|
| Authentication | ✅ Complete |
| Refresh Tokens | ✅ Complete |
| Session Management | ✅ Complete |
| Account Management | ✅ Complete |
| Transactions | ✅ Complete |
| Administration | ✅ Complete |
| Enterprise Security | ✅ Complete |
| Database Migration | ✅ Complete |
| AI Integration | 🚧 In Progress |
| Fraud Detection | 📅 Planned |
| AI Banking Assistant | 📅 Planned |

---

# ▶️ Getting Started

## Prerequisites

Before running the project, ensure you have the following installed:

- Java 17+
- Maven 3.9+
- PostgreSQL 15+
- Git

---

# 📥 Clone the Repository

```bash
git clone https://github.com/Ridoh7/ai-banking-assistant-api.git

cd ai-banking-assistant-api
```

---

# ⚙️ Environment Configuration

Create a `.env` file (or configure your preferred environment variables).

Example:

```env
DB_URL=jdbc:postgresql://localhost:5432/ai_banking_assistant
DB_USERNAME=postgres
DB_PASSWORD=your_password

JWT_SECRET=your_super_secret_key
JWT_EXPIRATION=900000

ADMIN_SECRET=your_admin_secret
```

---

# 🗄 Database Migration

This project uses **Flyway** for database versioning.

Run all migrations:

```bash
mvn flyway:migrate
```

---

# ▶️ Run the Application

```bash
mvn spring-boot:run
```

The application starts on

```
http://localhost:8080
```

---

# 📖 API Documentation

Swagger UI

```
http://localhost:8080/swagger-ui.html
```

or

```
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON

```
http://localhost:8080/v3/api-docs
```

---

# 🧪 Testing

The API can be tested using:

- Swagger UI
- Postman
- REST Client
- cURL

Example login request:

```http
POST /api/v1/auth/login
```

```json
{
  "email": "user@example.com",
  "password": "password"
}
```

---

# 📸 Sample Features

The following enterprise features are fully implemented.

### Authentication

- JWT Login
- Refresh Token Rotation
- Session-based Authentication

### Session Management

- Device Tracking
- Browser Detection
- Operating System Detection
- Device Class Detection
- Active Sessions
- Logout Current Session
- Logout Other Sessions
- Logout All Sessions

### Banking

- Deposit
- Withdrawal
- Transfer
- Account Management
- Transaction History

### Administration

- Account Freeze
- Account Unfreeze
- Account Closure
- User Management

---

# 📂 Database

The project currently includes database support for:

- Users
- Accounts
- Transactions
- Refresh Tokens
- Idempotency Records

Managed using **Flyway** migrations.

---

# 🔐 Security Features

The application implements multiple enterprise security layers.

Authentication

- JWT Access Tokens
- Refresh Tokens
- Refresh Token Rotation
- Persistent Sessions

Authorization

- Role-based Access Control
- Ownership Validation
- Endpoint Authorization

Session Security

- Device-aware Sessions
- Session Revocation
- Active Session Validation
- Device Tracking

Transaction Security

- Transaction Idempotency
- Duplicate Request Detection
- Optimistic Locking

Infrastructure

- BCrypt Password Hashing
- Global Exception Handling
- Validation
- Environment-based Configuration

---

# 📦 Project Structure

```
src
├── auth
├── account
├── transaction
├── security
├── user
├── config
├── common
├── idempotency
├── ai
└── resources
```

---

# 🚀 Future Enhancements

The next milestones include:

## Banking

- Audit Logging
- Beneficiary Management
- Scheduled Transfers
- Standing Orders
- Daily Transfer Limits
- Fraud Detection
- Transaction Reversal

## Security

- Multi-factor Authentication (MFA)
- Password Reset
- Login Notification Emails
- Scheduled Session Cleanup
- Admin Force Logout

## Artificial Intelligence

- OpenAI Integration
- AI Banking Assistant
- Spending Analysis
- Financial Insights
- Intelligent Customer Support
- AI Fraud Detection
- Personalized Financial Recommendations

---

# 🤝 Contributing

Contributions are welcome.

To contribute:

1. Fork the repository.

2. Create a feature branch.

```bash
git checkout -b feature/my-feature
```

3. Commit your changes.

```bash
git commit -m "feat: add awesome feature"
```

4. Push to your branch.

```bash
git push origin feature/my-feature
```

5. Open a Pull Request.

---

# 👨‍💻 Author

## Ridoh Lawal

Backend Java Developer • Software Engineer • AI Engineer

**LinkedIn**

https://www.linkedin.com/in/ridoh-lawal-31b0a3156

**GitHub**

https://github.com/Ridoh7

---

# 💡 Why This Project?

This project goes beyond a typical CRUD banking application.

It demonstrates enterprise-grade backend engineering practices including:

- Clean Architecture
- SOLID Principles
- REST API Design
- Spring Security
- JWT Authentication
- Refresh Token Rotation
- Device-aware Session Management
- Financial Transaction Processing
- Transaction Idempotency
- Optimistic Locking
- Flyway Database Versioning
- PostgreSQL Integration
- Enterprise Exception Handling
- AI-ready Architecture

The long-term vision is to evolve this platform into an intelligent AI-powered banking backend capable of delivering conversational banking, fraud detection, financial insights, and intelligent customer support using Large Language Models (LLMs).

---

# ⭐ Support

If you found this project useful, consider giving it a ⭐ on GitHub.

It helps others discover the project and supports its continued development.

---

# 📄 License

This project is released under the MIT License.

It is intended for educational purposes, portfolio demonstration, and enterprise backend engineering practice.

Feel free to learn from it, extend it, and build upon it.