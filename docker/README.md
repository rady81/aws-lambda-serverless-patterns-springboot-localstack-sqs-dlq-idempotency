# Docker (LocalStack + Redis)

This folder contains Docker-related assets used for local development.

## Services

### LocalStack
- Emulates AWS services locally
- We use it to emulate **SQS** (including DLQ behavior)
- Exposes an AWS-like API endpoint on `http://localhost:4566`

### Redis
- Used for idempotency / deduplication
- Exposes Redis on `localhost:6379`

## LocalStack init scripts

We mount:

- `./docker/localstack-init` → `/etc/localstack/init/ready.d`

Scripts in `localstack-init/` run when LocalStack is ready. They should:
- create the main SQS queue
- create the DLQ queue
- apply a redrive policy (after N failures → move to DLQ)

## Common commands

Start in background:

```bash
docker compose up -d
```

Stop:

```bash
docker compose down
```

View logs:

```bash
docker compose logs -f
```

Clean reset (removes volumes too):

```bash
docker compose down -v
docker compose up -d
```
