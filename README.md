# Multi-Chain Wallet Management System

A comprehensive backend application for managing multi-chain cryptocurrency wallets supporting Ethereum Sepolia, Tron, and TON networks.

## Features

- User authentication with JWT
- Multi-chain wallet creation and management
- Deposit and withdrawal operations
- Real-time balance updates
- Transaction history tracking
- Automated transaction confirmation monitoring

## Tech Stack

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.2.0** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence
- **PostgreSQL 15** - Primary database
- **Hibernate** - ORM framework

### Blockchain Integration
- **Web3j 4.10.3** - Ethereum interaction
- **Tron Trident** - Tron blockchain integration
- **TON SDK** - TON blockchain integration

### Security & Authentication
- **JWT (JJWT 0.12.3)** - Token-based authentication
- **BCrypt** - Password hashing
- **Bouncy Castle** - Cryptographic operations

### DevOps & Tools
- **Docker & Docker Compose** - Containerization
- **Maven** - Build automation
- **Swagger/OpenAPI** - API documentation
- **Lombok** - Reduce boilerplate code
- **H2 Database** - Testing

---

## Quick Start

## Prerequisites

Before you begin, ensure you have the following installed:

### Required
- **Java 17** or higher ([Download](https://adoptium.net/temurin/releases/?version=17))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **Docker** ([Download](https://www.docker.com/get-started))
- **Docker Compose** (usually comes with Docker Desktop)

### Optional
- **Git** ([Download](https://git-scm.com/downloads))
- **PostgreSQL 15** (if running without Docker)
- **IntelliJ IDEA** or **Eclipse** (for development)
- **Postman** (for API testing)

### Verify Installation

```bash
# Check Java version
java -version
# Expected: openjdk version "17.x.x"

# Check Maven version
mvn -version
# Expected: Apache Maven 3.8.x or higher

# Check Docker version
docker --version
# Expected: Docker version 20.x.x or higher

# Check Docker Compose version
docker compose version
# Expected: Docker Compose version v2.x.x
```

### Running with Docker

```bash
docker-compose up -d --build
```

## API Endpoints

### Authentication
POST /api/user/register - Register new user \
POST /api/user/login - User login \
POST /api/user/logout - User logout \
GET /api/user/profile - Get current user info \

### Wallets
POST /api/wallets/create - Create new wallet \
GET /api/wallets/list - Get current user wallets \
GET /api/wallets/{network} - Get user wallet by network \
PUT /api/wallets/deposit-address/{network} - Return deposit address of specific wallet \

### Transactions
POST /api/transactions/withdraw - Init withdrawal request \
POST /api/transactions/withdraw/approve - Approve withdrawal request \
GET /api/transactions/list/user/{userId} - Fetch user's transactions
GET /api/transactions/list/{walletId} - Fetch specific wallet's transactions \
GET /api/transactions/list/{walletId}/paged - Fetch specific wallet's paged transactions \
GET /api/transactions/info/{transactionId} - Get transaction details

---
