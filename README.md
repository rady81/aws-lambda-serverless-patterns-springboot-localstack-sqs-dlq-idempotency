# Student Card Platform - AWS Lambda (Local SQS + DLQ + Idempotency)

This is a single Spring Boot Maven project that demonstrates a real-world flow:
- REST API receives a student credit card application
- Application is persisted in H2
- An async event is published to SQS (LocalStack)
- A worker poller processes SQS messages like a Lambda consumer
- Idempotency is enforced via Redis (SETNX + TTL)
- Retries happen automatically if processing fails
- Poison messages are routed to DLQ after maxReceiveCount

## Tech
- Java 17
- Spring Boot 3
- Maven
- LocalStack (SQS + DLQ)
- Redis (idempotency)
- H2 (local persistence)
- Testcontainers (integration tests)

---

## 1) Prerequisites
- Java 17 installed
- Maven installed (or use IntelliJ Maven integration)
- Docker installed and running

---

## 2) Create project in IntelliJ
1. File -> New -> Project
2. Choose Maven
3. GroupId: `com.northstar`
4. ArtifactId: `student-card-platform`
5. Name: `student-card-platform`
6. JDK: 17
7. Finish
   
8. REST endpoint (API)
9. “Lambda-like” async worker (SQS consumer) running inside the same Spring Boot app
10. Idempotency/dedup using Redis (local via Docker)
11. Retries + DLQ using SQS redrive policy (local via LocalStack)
12. Integration tests using Testcontainers (runs fully on your machine)

Then replace/add files exactly as in this repository.

---

## 3) Start local infrastructure (LocalStack + Redis)
From project root:

```bash
docker compose up -d
