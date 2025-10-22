# PKI System - Backend

This repository contains the backend service for a **Public Key Infrastructure (PKI)** system, developed as a project for the "Security in E-business Systems" course. The application provides a comprehensive solution for managing digital certificates and includes a secure, shared password manager.

## Key Features

Based on the project specification, the backend implements the following features:

* **Role-Based Access Control:** Manages three distinct user roles: Administrator, CA User, and Regular User, each with specific permissions.
* **User Management:** Handles registration for regular users with email activation links, secure login with reCAPTCHA, and password recovery mechanisms.
* **Certificate Lifecycle Management:**
    * Issuance of all certificate types: Root (self-signed), Intermediate, and End-Entity.
    * Processing of Certificate Signing Requests (CSRs) uploaded by users.
    * Validation of the certificate chain, including expiration dates and revocation status, before issuing a new certificate.
    * Certificate revocation with support for CRL or OCSP checks.
* **Secure Key Storage:**
    * Stores CA certificates and their corresponding private keys in password-protected keystores.
    * Keystore passwords are encrypted using symmetric keys, which are managed per CA user.
* **Shared Password Manager:**
    * Allows users to save sensitive credentials (e.g., website passwords), which are encrypted using the user's public key.
    * Implements a secure sharing mechanism where a password is re-encrypted with the public key of the receiving user before being stored.
* **Security Features:**
    * **HTTPS:** Enforces secure communication between the client and server.
    * **JWT Session Management:** Allows users to view and revoke their active sessions from different devices.
    * **Audit Logging:** Records all security-significant events to ensure non-repudiation.
    * **Vulnerability Protection:** Implemented measures to protect against common web attacks like SQL Injection and Cross-Site Scripting (XSS).

## Technology Stack

* **Framework:** Java + Spring Boot / Node.js + Express (as per specification)
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
    * The API is documented following the OpenAPI specification and is available at `/swagger-ui.html` once the application is running.
