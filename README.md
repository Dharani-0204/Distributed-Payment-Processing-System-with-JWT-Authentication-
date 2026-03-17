# 🚀 Distributed Payment Processing System

A full-stack **secure digital wallet and payment processing system** built using Spring Boot, React, and PostgreSQL.
This project simulates real-world payment platforms like UPI/Stripe with authentication, transaction handling, and wallet management.

---

## 🧠 Overview

This system allows users to:

* Register and securely log in using JWT authentication
* Deposit money into their wallet
* Transfer money between users
* Track transaction history in real-time

The application follows a **layered architecture** and implements **secure backend practices**.

---

## 🛠️ Tech Stack

### 🔹 Backend

* Java Spring Boot
* Spring Security
* JWT Authentication
* Hibernate / JPA
* Maven

### 🔹 Frontend

* React (Vite)
* Axios
* Context API
* CSS (Glassmorphism UI)

### 🔹 Database

* PostgreSQL

---

## 🔐 Key Features

* ✅ JWT-based secure authentication
* ✅ Password hashing using BCrypt
* ✅ Wallet system (Deposit & Transfer)
* ✅ Transaction tracking system
* ✅ Protected REST APIs
* ✅ Error handling & validation
* ✅ Full-stack integration

---

## ⚙️ System Architecture

```
React Frontend
      ↓
Axios API Calls (JWT Token)
      ↓
Spring Boot Backend
      ↓
Service Layer (Business Logic)
      ↓
Repository Layer (JPA)
      ↓
PostgreSQL Database
```

---

## 📦 Project Structure

```
payment-backend/
  ├── controller/
  ├── service/
  ├── repository/
  ├── dto/
  ├── entity/
  ├── security/

payment-frontend/
  ├── pages/
  ├── components/
  ├── context/
  ├── services/
```

---

## 🚀 How to Run the Project

### 🔹 Backend

```bash
cd payment-backend
./mvnw spring-boot:run
```

Runs on:

```
http://localhost:8080
```

---

### 🔹 Frontend

```bash
cd payment-frontend
npm install
npm run dev
```

Runs on:

```
http://localhost:5173 (or 5174)
```

---

## 🔑 Authentication Flow

1. User logs in with email & password
2. Backend generates JWT token
3. Token stored in frontend (localStorage)
4. Every API request includes:

```
Authorization: Bearer <token>
```

5. Backend validates token using JWT filter

---

## 💰 Core Functionalities

### 🔹 Deposit

* Adds money to user wallet
* Updates balance
* Stores transaction

### 🔹 Transfer

* Validates sender balance
* Transfers amount to receiver
* Stores transaction

### 🔹 Transactions

* Fetch user transaction history
* Display in dashboard

---

## ⚠️ Challenges Solved

* Fixed CORS issues between frontend & backend
* Handled 403 errors due to missing JWT
* Implemented secure authentication flow
* Resolved database connection issues
* Managed port conflicts

---

## 🔮 Future Improvements

* Fraud detection system
* Rate limiting
* Microservices architecture
* Real payment gateway integration (Stripe/Razorpay)
* Admin dashboard

---

## 💼 Use Case

This project demonstrates:

* Backend system design
* Secure API development
* Full-stack integration
* Real-world financial application logic

---

## 👨‍💻 Author

**Dharani Karla**
B.Tech Student | Aspiring Software Engineer

---

## ⭐ If you like this project

Give it a ⭐ on GitHub!
