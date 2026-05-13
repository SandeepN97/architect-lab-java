# ArchitectLab Architecture

ArchitectLab starts with a single Spring Boot API and React dashboard, but the package boundaries mirror a future distributed system:

```text
React UI -> Spring Boot API -> Command Service -> Simulation Engine -> Event Publisher
                                             -> Rate Limiter Service
                                             -> Telemetry Service -> Prometheus/Grafana
```

The MVP keeps events in memory so the lab can run locally without infrastructure. Docker Compose includes Redpanda, Redis, PostgreSQL, Prometheus, Grafana, and MinIO so later modules can replace the adapters without changing the UI contract.

## Extension seams

- Add a `LabCommand` implementation for each new operation.
- Replace `EventPublisher` with Kafka/Redpanda producer and consumer adapters.
- Replace the in-memory rate-limit maps with Redis Lua scripts.
- Add new lab controllers under `/api/labs/{labSlug}` while reusing telemetry, events, and security.
