# Lab 01 - Flyway + Testcontainers

## Overview

This repository demonstrates a complete integration testing setup using:

* Spring Boot 3.5
* PostgreSQL
* Testcontainers
* Flyway
* Spring Data JPA

The goal is to explore the evolution from a minimal Spring Boot application to a production-oriented database setup with migrations, integration testing, roles, ownership, and permission management.

---

## Learning Objectives

* Run PostgreSQL using Testcontainers
* Execute database migrations with Flyway
* Persist data using Spring Data JPA
* Validate integration tests against a real database
* Understand PostgreSQL roles and permissions
* Separate migration and application users

---

## Tech Stack

* Java 21
* Spring Boot 3.5.x
* PostgreSQL 17
* Flyway
* Testcontainers
* Maven
* JUnit 5
* AssertJ

---

## Project Evolution

### v0.1-bootstrap

* Spring Boot project created
* Maven Wrapper configured
* Context loading test

### v0.2-testcontainers

* PostgreSQL container configured
* ServiceConnection integration
* Database connectivity validation

### v0.3-flyway

* Flyway migrations enabled
* Customer table creation
* Migration validation tests

### v0.4-jpa

* Customer entity
* Customer repository
* Persistence integration tests

### v0.5-postgres-roles

* migration_user created
* app_user created
* Table ownership validation
* Permission grants validation
* DML access validation
* DDL restriction validation

---

## Running the Project

```bash
./mvnw test
```

All tests should pass successfully.

---

## Key Learnings

### Database Ownership

Tables created by Flyway are owned by the migration user.

### Role Separation

The application user should not own database objects.

### Principle of Least Privilege

The application user receives only:

* SELECT
* INSERT
* UPDATE
* DELETE

DDL operations remain restricted.

### Integration Testing

Tests run against a real PostgreSQL instance instead of an in-memory database.

---

## Future Labs

## Next Labs in the Series

* Lab 02 - PostgreSQL Row Level Security (RLS)
* Lab 03 - Spring Multitenancy
* Future Lab - Database observability
* Future Lab - Spring Boot multi-datasource configurations
