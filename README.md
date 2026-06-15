# AI Banking Assistant API

Enterprise-grade banking backend built with Java, Spring Boot, PostgreSQL, JWT Authentication, and OpenAI-ready architecture.

This project demonstrates secure banking operations, role-based access control, transaction management, account administration, and enterprise backend engineering practices.

---

## 🚀 Features

### 🔐 Authentication & Authorization

* User Registration
* Admin Registration
* User Login
* JWT Authentication
* Role-Based Access Control (USER / ADMIN)
* Secure Endpoint Protection
* Ownership Validation

---

### 🏦 Account Management

* Create Bank Account
* Account Validation
* Balance Inquiry
* View Current User Accounts
* Account Status Management

  * ACTIVE
  * FROZEN
  * CLOSED

---

### 💸 Transaction Management

* Deposit Funds
* Withdraw Funds
* Transfer Funds Between Accounts
* Transaction History
* Automatic Transaction Reference Generation
* Transaction Validation
* Sufficient Balance Validation

---

### 🛡️ Banking Security Controls

* Users can only access their own accounts
* Users cannot withdraw from another user's account
* Users cannot transfer funds from another user's account
* Users cannot view balances of other users
* Ownership validation enforced at service level

---

### 👨‍💼 Administrative Features

* Register Admin Users
* View All Users
* View All Accounts
* Freeze Accounts
* Unfreeze Accounts
* Close Accounts

---

### ⚙️ Enterprise Backend Features

* Layered Architecture
* DTO Pattern
* Repository Pattern
* Service Layer
* Global Exception Handling
* Centralized API Response Wrapper
* Transaction Management
* Swagger/OpenAPI Documentation
* PostgreSQL Persistence
* Spring Security Integration

---

## 🛠 Tech Stack

* Java 17
* Spring Boot
* Spring Security
* JWT Authentication
* PostgreSQL
* Spring Data JPA
* Maven
* Swagger / OpenAPI

---

## 📚 API Modules

### Authentication

* POST /api/v1/auth/register
* POST /api/v1/auth/login
* POST /api/v1/auth/admin/register

### Accounts

* POST /api/v1/accounts
* GET /api/v1/accounts/me
* GET /api/v1/accounts/{accountNumber}/balance
* GET /api/v1/accounts/validate/{accountNumber}

### Transactions

* POST /api/v1/transactions/deposit
* POST /api/v1/transactions/withdraw
* POST /api/v1/transactions/transfer
* GET /api/v1/transactions/account/{accountNumber}

### Admin

* GET /api/v1/admin/users
* GET /api/v1/admin/accounts
* PUT /api/v1/admin/accounts/{accountNumber}/freeze
* PUT /api/v1/admin/accounts/{accountNumber}/unfreeze
* PUT /api/v1/admin/accounts/{accountNumber}/close

---

## 📌 Current Status

### Completed

* JWT Authentication
* Role-Based Authorization
* Banking Account Management
* Deposit, Withdrawal & Transfer
* Ownership Security
* Account Status Management
* Administrative Controls
* Swagger Documentation
* PostgreSQL Integration

### In Progress

* AI Banking Assistant
* OpenAI Integration
* Conversation Management

### Planned

* Transaction Reversal
* Audit Logging
* Account Statements
* Beneficiary Management
* Daily Transfer Limits
* Fraud Detection & Monitoring
* AI-Powered Banking Assistant
* OpenAI Chat Integration

---

## 🎯 Project Goals

This project is designed to demonstrate:

* Enterprise Backend Engineering
* Secure Banking System Design
* API Security Best Practices
* Financial Transaction Processing
* Scalable Spring Boot Architecture
* AI Integration into Financial Systems

---

## ▶️ Run Locally

```bash
git clone https://github.com/Ridoh7/ai-banking-assistant-api.git

cd ai-banking-assistant-api

mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

---

## 👨‍💻 Author

Ridoh Lawal

Java Developer | Backend Engineer | IT Support Professional

---

## 🏗️ Project Status

Currently in active development.

Initial foundation setup completed:
- Spring Boot configuration
- PostgreSQL integration
- Environment-based configuration
- Multi-profile setup
- GitHub project initialization

---

## 📖 Goals

This project is designed to demonstrate:
- enterprise backend engineering,
- intelligent systems integration,
- scalable API architecture,
- and AI-powered enterprise workflow development.

---

## ⚙️ Run Locally

### Clone repository

```bash
git clone https://github.com/Ridoh7/ai-banking-assistant-api.git
