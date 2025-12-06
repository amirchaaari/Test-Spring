# School Management System

A Spring Boot application for managing students and school administration with JWT authentication.

## Features

- Student management
- Admin authentication with JWT
- RESTful API
- Swagger/OpenAPI documentation
- MySQL database

## Quick Start

You can run the entire application stack (MySQL database, Spring Boot backend, and Angular frontend) using Docker Compose:

```bash
docker-compose up
```

This will start:
- **MySQL Database** on port `3307`
- **Spring Boot Backend** on port `8089`
- **Angular Frontend** on port `4200`

## API Documentation

Once the backend is running, you can access the Swagger UI at:
- http://localhost:8089/swagger-ui/index.html

## Technology Stack

- **Backend**: Spring Boot 4.0.0
- **Database**: MySQL 8.0
- **Security**: Spring Security with JWT
- **Frontend**: Angular
- **Documentation**: Swagger/OpenAPI

