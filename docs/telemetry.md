# Telemetry

The backend exposes Spring Boot Actuator and Micrometer metrics at `/actuator/prometheus`.

Custom MVP metrics:

- `architectlab_rate_limiter_allowed_total`
- `architectlab_rate_limiter_rejected_total`
- `architectlab_request_latency_millis`
- `architectlab_requests_total`

The UI polls `/api/telemetry` once per second for a live learning dashboard.
