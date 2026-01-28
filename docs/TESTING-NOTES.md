# Testing Notes (Local Dependencies)

If your tests rely on SQS and Redis, you have two common approaches:

## Option A: Run tests against Docker Compose (recommended for integration tests)

1) Start dependencies:

```bash
docker compose up -d
```

2) Run tests:

```bash
mvn test
```

This works well when:
- you want behavior close to production
- you rely on LocalStack queues + Redis idempotency

## Option B: Mock external dependencies (unit tests)

Mock the SQS publisher/consumer boundaries and Redis access, so tests run without Docker.
This is best when you want fast feedback and are validating business logic only.

## Tips

- Keep unit tests Docker-free.
- Use integration tests to validate “end-to-end” messaging and idempotency.
