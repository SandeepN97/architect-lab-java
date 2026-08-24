# ArchitectLab — Interactive System Design Playground

> **Learn distributed systems by changing them.** Run traffic, switch rate-limiting algorithms, inject latency, inspect events, and watch telemetry change through real Spring Boot APIs and a React dashboard.

**ArchitectLab** is a hands-on system design lab built with **Java 21, Spring Boot, React, Redis, PostgreSQL, Redpanda/Kafka, Prometheus, and Grafana**. Instead of only reading system-design diagrams, you can interact with the system and observe the tradeoffs.

**Useful for:** backend engineers, Java/Spring developers, system-design interview preparation, and anyone learning distributed-systems concepts through working code.

### What you can explore today

| Area | What the lab demonstrates |
| --- | --- |
| **Rate limiting** | Fixed-window, sliding-window, and token-bucket algorithms |
| **Traffic simulation** | Start/stop controlled request load and change RPS |
| **Failure injection** | Add latency and observe system behavior |
| **Security** | JWT authentication with `STUDENT`, `ADMIN`, and `OBSERVER` RBAC |
| **Events** | In-memory event stream shaped for future Redpanda/Kafka adapters |
| **Observability** | Micrometer metrics, Prometheus scraping, Grafana infrastructure |
| **Architecture** | Command pattern, adapter boundaries, replaceable infrastructure |

> **Current scope:** MVP 1 is the **Rate Limiter Lab + Command Center + Telemetry + JWT Security**. Redis-backed distributed limiting, Redpanda event replay, PostgreSQL audit storage, and additional labs are roadmap items rather than claimed as complete.

---

## Why ArchitectLab exists

System design is often taught as static boxes and arrows. ArchitectLab turns those boxes into a runnable environment where you can ask questions such as:

- What changes when a fixed-window limiter becomes a token bucket?
- What does injected latency look like in telemetry?
- Which operations should an observer, student, or administrator be allowed to perform?
- How would an in-memory adapter evolve into Redis, Kafka/Redpanda, or PostgreSQL without rewriting the application?

The goal is to connect **system-design theory, backend implementation, security, and observability** in one project that is small enough to understand and extensible enough to grow.

---

## MVP 1 scope

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

> These are intentionally simple **local demo credentials** for the learning environment, not production credentials.

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

## Contributing

ArchitectLab is intentionally structured as a growing collection of small, inspectable labs. Good contributions are focused and demonstrable: a new algorithm, adapter, metric, failure mode, visualization, or learning note that makes a system-design tradeoff easier to understand.

If you want to contribute, open an issue describing the behavior you want to demonstrate before making a large architectural change.
