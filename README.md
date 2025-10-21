# PKI System - Backend

This repository contains the backend service for a **Public Key Infrastructure (PKI)** system, developed as a project for the "Security in E-business Systems" course. The application provides a comprehensive solution for managing digital certificates and includes a secure, shared password manager.

## Key Features

Based on the project specification, the backend implements the following features:

* [cite_start]**Role-Based Access Control:** Manages three distinct user roles: Administrator, CA User, and Regular User, each with specific permissions [cite: 13-15].
* [cite_start]**User Management:** Handles registration for regular users with email activation links, secure login with reCAPTCHA, and password recovery mechanisms[cite: 40, 43, 46, 50].
* **Certificate Lifecycle Management:**
    * [cite_start]Issuance of all certificate types: Root (self-signed), Intermediate, and End-Entity [cite: 22-24].
    * [cite_start]Processing of Certificate Signing Requests (CSRs) uploaded by users[cite: 89, 92].
    * [cite_start]Validation of the certificate chain, including expiration dates and revocation status, before issuing a new certificate [cite: 78-79].
    * [cite_start]Certificate revocation with support for CRL or OCSP checks[cite: 100, 102].
* **Secure Key Storage:**
    * [cite_start]Stores CA certificates and their corresponding private keys in password-protected keystores[cite: 81].
    * [cite_start]Keystore passwords are encrypted using symmetric keys, which are managed per CA user[cite: 82, 84].
* **Shared Password Manager:**
    * [cite_start]Allows users to save sensitive credentials (e.g., website passwords), which are encrypted using the user's public key[cite: 117, 120].
    * [cite_start]Implements a secure sharing mechanism where a password is re-encrypted with the public key of the receiving user before being stored [cite: 125-127].
* **Security Features:**
    * [cite_start]**HTTPS:** Enforces secure communication between the client and server[cite: 159].
    * [cite_start]**JWT Session Management:** Allows users to view and revoke their active sessions from different devices [cite: 53-56].
    * [cite_start]**Audit Logging:** Records all security-significant events to ensure non-repudiation[cite: 164, 166].
    * [cite_start]**Vulnerability Protection:** Implemented measures to protect against common web attacks like SQL Injection and Cross-Site Scripting (XSS)[cite: 170].

## Technology Stack

* [cite_start]**Framework:** Java + Spring Boot / Node.js + Express (as per specification [cite: 3252, 3255])
* **Authentication:** JWT (JSON Web Tokens)
* **Database:** Relational Database (e.g., PostgreSQL)
* **Security:** Bouncy Castle (for certificate generation), Spring Security, HTTPS

## Setup and Installation

1.  **Prerequisites:**
    * Java JDK (Version 17+) / Node.js
    * Maven or Gradle / npm
    * A running instance of a relational database.

2.  **Configuration:**
    * Update the `application.properties` (or `.env`) file with your database credentials, JWT secret, and other necessary configurations.

3.  **Build and Run:**
    ```bash
    # (Example for Spring Boot)
    ./mvnw spring-boot:run
    ```

4.  **API Documentation:**
    * [cite_start]The API is documented following the OpenAPI specification and is available at `/swagger-ui.html` once the application is running[cite: 3268].
