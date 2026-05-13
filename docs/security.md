# Security Model

The MVP uses local demo credentials and HMAC-signed JWTs.

| User | Password | Roles |
| --- | --- | --- |
| `student` | `student123` | `STUDENT` |
| `admin` | `admin123` | `ADMIN`, `STUDENT` |
| `observer` | `observer123` | `OBSERVER` |

RBAC rules:

- `STUDENT` can start/stop traffic and choose the rate-limit algorithm.
- `ADMIN` can run student actions plus failure/configuration commands such as cache toggles, latency injection, and lab reset.
- `OBSERVER` can read telemetry and events but cannot mutate lab state.
- Prometheus can scrape `/actuator/prometheus` without a bearer token for local development.

Demo passwords are checked through Spring Security password encoders instead of plaintext comparison so the local implementation follows the same shape as a production user store.
