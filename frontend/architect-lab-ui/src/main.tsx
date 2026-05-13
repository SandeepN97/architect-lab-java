import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

type Telemetry = {
  totalRequests: number;
  allowedRequests: number;
  rejectedRequests: number;
  successRate: number;
  errorRate: number;
  p95LatencyMillis: number;
  cacheHitRatio: number;
  activeTrafficRps: number;
  cacheEnabled: boolean;
  eventCount: number;
};

type LabEvent = {
  id: string;
  type: string;
  actor: string;
  lab: string;
  timestamp: string;
  payload: Record<string, unknown>;
};

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

const initialTelemetry: Telemetry = {
  totalRequests: 0,
  allowedRequests: 0,
  rejectedRequests: 0,
  successRate: 1,
  errorRate: 0,
  p95LatencyMillis: 0,
  cacheHitRatio: 0,
  activeTrafficRps: 0,
  cacheEnabled: true,
  eventCount: 0,
};

function App() {
  const [token, setToken] = useState('');
  const [telemetry, setTelemetry] = useState<Telemetry>(initialTelemetry);
  const [events, setEvents] = useState<LabEvent[]>([]);
  const [algorithm, setAlgorithm] = useState('TOKEN_BUCKET');
  const [status, setStatus] = useState('Log in with a demo account to run the MVP lab.');
  const [roles, setRoles] = useState<string[]>([]);

  const authHeaders = useMemo(() => ({
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  }), [token]);

  useEffect(() => {
    if (!token) return;
    const interval = window.setInterval(async () => {
      const [telemetryResponse, eventsResponse] = await Promise.all([
        fetch(`${API_URL}/api/telemetry`, { headers: authHeaders }),
        fetch(`${API_URL}/api/events`, { headers: authHeaders }),
      ]);
      if (telemetryResponse.ok) setTelemetry(await telemetryResponse.json());
      if (eventsResponse.ok) setEvents(await eventsResponse.json());
    }, 1000);
    return () => window.clearInterval(interval);
  }, [authHeaders, token]);

  async function login(username: string, password: string) {
    const response = await fetch(`${API_URL}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    });
    const body = await response.json();
    setToken(body.accessToken);
    setRoles(body.user.roles);
    setStatus(`Authenticated as ${body.user.username} with ${body.user.roles.join(', ')} role(s).`);
  }

  async function sendCommand(type: string, parameters: Record<string, unknown> = {}) {
    const response = await fetch(`${API_URL}/api/commands`, {
      method: 'POST',
      headers: authHeaders,
      body: JSON.stringify({ type, parameters }),
    });
    const body = await response.json();
    setStatus(response.ok ? body.message : `${body.code ?? 'ERROR'}: ${body.message ?? 'Command failed'}`);
  }

  async function updateAlgorithm(value: string) {
    setAlgorithm(value);
    await fetch(`${API_URL}/api/rate-limiter/config`, {
      method: 'POST',
      headers: authHeaders,
      body: JSON.stringify({ algorithm: value, limit: 100, windowSeconds: 60 }),
    });
  }

  const canOperate = roles.includes('STUDENT') || roles.includes('ADMIN');
  const canAdminister = roles.includes('ADMIN');

  return (
    <main className="shell">
      <section className="hero panel">
        <div>
          <p className="eyebrow">ArchitectLab</p>
          <h1>Interactive System Design Playground</h1>
          <p className="lede">Run traffic, trigger failures, publish events, and watch live telemetry for a Java/Spring Boot rate limiter lab.</p>
        </div>
        <div className="login-row">
          <button className="primary" onClick={() => login('student', 'student123')}>Student login</button>
          <button onClick={() => login('admin', 'admin123')}>Admin login</button>
          <button onClick={() => login('observer', 'observer123')}>Observer login</button>
        </div>
      </section>

      <section className="grid two">
        <div className="panel">
          <p className="eyebrow">Rate Limiter Lab</p>
          <h2>Client → API Gateway → Rate Limiter → Service → Database</h2>
          <div className="diagram">
            {['Client', 'Gateway', 'Limiter', 'Service', 'Database'].map((node) => <div className="node" key={node}>{node}</div>)}
          </div>
          <label>Algorithm</label>
          <select value={algorithm} onChange={(event) => updateAlgorithm(event.target.value)} disabled={!canOperate}>
            <option value="TOKEN_BUCKET">Token bucket</option>
            <option value="FIXED_WINDOW">Fixed window</option>
            <option value="SLIDING_WINDOW">Sliding window</option>
          </select>
          <p className="hint">{status}</p>
        </div>

        <div className="panel command-center">
          <p className="eyebrow">Command Center</p>
          <h2>Operate the simulation</h2>
          <button onClick={() => sendCommand('START_TRAFFIC', { rps: 500 })} disabled={!canOperate}>Send 500 RPS</button>
          <button onClick={() => sendCommand('START_TRAFFIC', { rps: 5000 })} disabled={!canOperate}>Send 5,000 RPS</button>
          <button onClick={() => sendCommand('STOP_TRAFFIC')} disabled={!canOperate}>Stop traffic</button>
          <button onClick={() => sendCommand('ENABLE_CACHE')} disabled={!canAdminister}>Enable cache</button>
          <button onClick={() => sendCommand('DISABLE_CACHE')} disabled={!canAdminister}>Disable cache</button>
          <button onClick={() => sendCommand('INJECT_LATENCY', { latencyMillis: 275 })} disabled={!canAdminister}>Inject 275ms latency</button>
          <button onClick={() => sendCommand('RESET_LAB')} disabled={!canAdminister}>Reset lab</button>
        </div>
      </section>

      <section className="panel">
        <p className="eyebrow">Telemetry Dashboard</p>
        <div className="metrics">
          <Metric label="Total requests" value={telemetry.totalRequests.toLocaleString()} />
          <Metric label="Allowed" value={telemetry.allowedRequests.toLocaleString()} />
          <Metric label="Rejected" value={telemetry.rejectedRequests.toLocaleString()} />
          <Metric label="p95 latency" value={`${telemetry.p95LatencyMillis.toFixed(0)} ms`} />
          <Metric label="Error rate" value={`${(telemetry.errorRate * 100).toFixed(1)}%`} />
          <Metric label="Cache hit ratio" value={`${(telemetry.cacheHitRatio * 100).toFixed(1)}%`} />
        </div>
      </section>

      <section className="grid two">
        <div className="panel">
          <p className="eyebrow">Event Stream Viewer</p>
          <div className="events">
            {events.slice(0, 8).map((event) => (
              <article key={event.id}>
                <strong>{event.type}</strong>
                <span>{event.actor} · {new Date(event.timestamp).toLocaleTimeString()}</span>
              </article>
            ))}
          </div>
        </div>
        <div className="panel">
          <p className="eyebrow">Security Console</p>
          <h2>JWT + RBAC demo</h2>
          <p>Demo users: student, admin, observer. Students can start/stop traffic and choose algorithms; admins can inject failures, toggle cache, and reset labs; observers are read-only.</p>
        </div>
      </section>
    </main>
  );
}

function Metric({ label, value }: { label: string; value: string }) {
  return <div className="metric"><span>{label}</span><strong>{value}</strong></div>;
}

createRoot(document.getElementById('root')!).render(<App />);
