# ArchitectLab: Interactive System Design Playground

ArchitectLab is an interactive system-design playground built with Java, Spring Boot, React, Redis, PostgreSQL, Redpanda/Kafka, OpenTelemetry-ready configuration, Prometheus, and Grafana.

It turns common system-design interview concepts into playable simulations. Users can run traffic, inject failures, publish events, inspect telemetry, compare architectures, and understand tradeoffs visually through real backend APIs and a live dashboard.

## MVP 1 scope

This repository intentionally starts small: **Rate Limiter Lab + Command Center + Telemetry + JWT Security**.

Included today:

- Spring Boot API with Java 21 source compatibility.
- JWT demo login with `STUDENT`, `ADMIN`, and `OBSERVER` roles.
- RBAC guardrails where students can run traffic, observers are read-only, and admins own failure/configuration commands.
- Command pattern for `START_TRAFFIC`, `STOP_TRAFFIC`, `ENABLE_CACHE`, `DISABLE_CACHE`, `INJECT_LATENCY`, and `RESET_LAB`.
- Fixed-window, sliding-window, and token-bucket rate-limit algorithms.
- In-memory event stream that mirrors a future Kafka/Redpanda topic.
- Micrometer metrics and Prometheus scrape endpoint.
- React + TypeScript dashboard with dark-mode cards, architecture flow, command center, telemetry, event stream, and security console.
- Docker Compose infrastructure for PostgreSQL, Redis, Redpanda, Prometheus, Grafana, and MinIO.

## Monorepo layout

```text
architect-lab-java/
├── backend/
│   └── architect-lab-api/       # Spring Boot MVP API
├── frontend/
│   └── architect-lab-ui/        # React + TypeScript dashboard
├── infra/                       # Prometheus/Grafana provisioning
├── docs/                        # Architecture, security, telemetry, roadmap
├── labs/                        # Learning notes per lab
├── docker-compose.yml
└── pom.xml
```

## Quick start

### 1. Start local infrastructure

```bash
docker compose up -d postgres redis redpanda prometheus grafana minio
```

### 2. Run the Spring Boot API

```bash
mvn -pl backend/architect-lab-api spring-boot:run
```

The API starts on <http://localhost:8080>.

Useful endpoints:

- `POST /api/auth/login`
- `POST /api/commands`
- `GET /api/events`
- `GET /api/telemetry`
- `GET /actuator/prometheus`

### 3. Run the React dashboard

```bash
cd frontend/architect-lab-ui
npm install
npm run dev
```

The UI starts on <http://localhost:5173>.

## Demo credentials

| Username | Password | Roles |
| --- | --- | --- |
| `student` | `student123` | `STUDENT` |
| `admin` | `admin123` | `ADMIN`, `STUDENT` |
| `observer` | `observer123` | `OBSERVER` |

Role behavior:

- `STUDENT`: start/stop traffic and choose the rate-limit algorithm.
- `ADMIN`: run student actions plus cache toggles, latency injection, and lab reset.
- `OBSERVER`: inspect telemetry and events only.

## Command Center API example

```bash
TOKEN=$(curl -s http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"student","password":"student123"}' | jq -r .accessToken)

curl -s http://localhost:8080/api/commands \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"type":"START_TRAFFIC","parameters":{"rps":5000}}'
```

## Architecture

```text
React UI
  ↓
Spring Boot API
  ↓
Command Service ──> Simulation State
  ↓                    ↓
Event Publisher      Rate Limiter Service
  ↓                    ↓
Event Stream UI      Telemetry Service ──> Micrometer ──> Prometheus/Grafana
```

The first implementation uses in-memory adapters so contributors can run the lab quickly. The Docker Compose services are ready for follow-up work that swaps in Redis-backed rate limiting, Redpanda events, PostgreSQL audit logs, and MinIO object storage.

## Roadmap

1. Redis Lua-backed distributed rate limiter.
2. Redpanda event publisher and event replay.
3. PostgreSQL users and audit records.
4. Consistent hashing visualizer.
5. URL shortener with redirect analytics.
6. Notification pub/sub simulator.
7. News feed lab.
8. Object storage lab with MinIO.
9. Interview mode design canvas.

## Testing

```bash
mvn test
```

```bash
cd frontend/architect-lab-ui && npm install && npm run build
```
