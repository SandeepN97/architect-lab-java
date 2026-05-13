package com.architectlab.telemetry;

public record TelemetrySnapshot(
        long totalRequests,
        long allowedRequests,
        long rejectedRequests,
        double successRate,
        double errorRate,
        double p95LatencyMillis,
        double cacheHitRatio,
        int activeTrafficRps,
        boolean cacheEnabled,
        long eventCount) {
}
