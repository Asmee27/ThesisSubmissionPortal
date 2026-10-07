# ThesisFlow – Thesis Submission Portal with CI/CD

ThesisFlow is a web-based Thesis Submission and Review System developed using Spring Boot and MySQL. The project demonstrates a complete DevOps workflow using Git/GitHub, Jenkins, Maven, Selenium, Docker, Docker Hub, and Ansible.

The system allows students to submit thesis documents and track their status, while reviewers can review submissions, provide feedback, and approve, reject, or request changes.

## Tech Stack

### Application
- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA / Hibernate
- Thymeleaf
- MySQL
- Maven

### DevOps & Testing
- Git & GitHub
- Jenkins
- Selenium WebDriver
- Docker
- Docker Hub
- Ansible
- WSL / Ubuntu

## Features

### Student
- Secure student login
- Submit thesis details and PDF documents
- Input and document validation
- View submitted thesis
- Track thesis status
- View reviewer feedback

### Reviewer
- Secure reviewer login
- View student submissions
- Access submitted PDF documents
- Add review feedback
- Approve thesis
- Reject thesis
- Request changes

## DevOps Workflow

The project implements an automated CI/CD workflow:

```text
Developer
    |
    v
Git / GitHub
    |
    v
Jenkins Pipeline
    |
    +--> Checkout Source Code
    |
    +--> Maven Build & Automated Tests
    |
    +--> Selenium Tests
    |
    +--> Package Spring Boot JAR
    |
    +--> Build Versioned Docker Image
    |
    +--> Push Image to Docker Hub
    |
    +--> Deploy Docker Container
    |
    v
Health Check
```

If automated tests fail, the Jenkins pipeline stops and the application is not deployed.

## Jenkins CI/CD Pipeline

The Jenkins pipeline automates:

1. Source-code checkout from GitHub
2. Maven build and automated testing
3. Application packaging
4. Versioned Docker image creation
5. Docker Hub authentication
6. Docker image push
7. Deployment of a fresh application container
8. Application health verification

Docker images are versioned using the Jenkins build number, for example:

```text
goldy12/thesisflow:build-15
goldy12/thesisflow:build-16
```

This allows individual application releases to be identified and supports rollback to a previous stable version.

## Automated Testing

Selenium WebDriver tests are integrated with Maven and Jenkins to validate critical user workflows.

The CI/CD pipeline is configured so that:

```text
Tests Pass  -> Package -> Docker Build -> Deployment
Tests Fail  -> Pipeline Stops -> Deployment Blocked
```

This behavior was validated by deliberately introducing a failing test and verifying that Jenkins blocked the deployment.

## Docker Deployment

The Spring Boot application is packaged as a Docker image using Eclipse Temurin Java 21.

The container exposes the application on port:

```text
8083
```

The application container connects to the MySQL database running on the host environment.

The project demonstrates the complete Docker container lifecycle including:

- Image build
- Image tagging
- Container creation
- Port mapping
- Log inspection
- Container stop/restart
- Container removal
- Versioned image deployment

## Configuration Management with Ansible

Ansible is used to configure the target environment.

The Ansible configuration defines:

- Required packages
- Application service user
- Application directory
- Deployment configuration
- Application port
- Docker deployment requirements

The playbook can be executed repeatedly while maintaining the desired server configuration, demonstrating idempotent configuration management.

## Reliability and Rollback

The deployment process includes health checking and rollback validation.

A rollback was demonstrated from:

```text
build-16
   |
   v
build-15 (Previous Stable Release)
```

After rollback, the Docker container was verified as running and the application health check returned:

```text
HTTP 200
```

This demonstrates recovery to a previous stable application release.

## Security

Sensitive configuration such as database passwords and Docker Hub credentials is not stored directly in the source code.

Environment variables and Jenkins Credentials are used for secret management.

Example:

```properties
spring.datasource.password=${DB_PASSWORD}
```

## Running the Application

Set the required database password as an environment variable and run:

```bash
mvn spring-boot:run
```

The application is available at:

```text
http://localhost:8083
```

## Running Tests

```bash
mvn test
```

## Building the Application

```bash
mvn clean package
```

The generated Spring Boot JAR is stored inside the `target` directory.

## Docker Build

```bash
docker build -t thesisflow:v1 .
```

## Docker Container

Example:

```bash
docker run -d -p 8083:8083 --name thesisflow-app thesisflow:v1
```

## Project Highlights

- End-to-end Jenkins CI/CD pipeline
- Automated Maven and Selenium testing
- Deployment blocked automatically on test failure
- Dockerized Spring Boot application
- Versioned Docker images
- Docker Hub integration
- Automated container deployment
- Ansible configuration management
- Idempotent provisioning
- Deployment health checks
- Rollback to a previous stable release

## Author

**Asmee Jadhav**

Computer Engineering