package com.architectlab.telemetry;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class TelemetryService {
    private final AtomicLong totalRequests = new AtomicLong();
    private final AtomicLong allowedRequests = new AtomicLong();
    private final AtomicLong rejectedRequests = new AtomicLong();
    private final AtomicLong cacheHits = new AtomicLong();
    private final AtomicLong cacheMisses = new AtomicLong();
    private final List<Double> latencySamples = new ArrayList<>();
    private final Counter allowedCounter;
    private final Counter rejectedCounter;
    private final DistributionSummary latencySummary;

    public TelemetryService(MeterRegistry meterRegistry) {
        this.allowedCounter = Counter.builder("architectlab_rate_limiter_allowed_total").description("Allowed lab requests").register(meterRegistry);
        this.rejectedCounter = Counter.builder("architectlab_rate_limiter_rejected_total").description("Rejected lab requests").register(meterRegistry);
        this.latencySummary = DistributionSummary.builder("architectlab_request_latency_millis").description("Simulated request latency").publishPercentileHistogram().register(meterRegistry);
        meterRegistry.gauge("architectlab_requests_total", totalRequests);
    }

    public synchronized void recordRequest(boolean allowed, double latencyMillis, boolean cacheHit) {
        totalRequests.incrementAndGet();
        if (allowed) {
            allowedRequests.incrementAndGet();
            allowedCounter.increment();
        } else {
            rejectedRequests.incrementAndGet();
            rejectedCounter.increment();
        }
        if (cacheHit) {
            cacheHits.incrementAndGet();
        } else {
            cacheMisses.incrementAndGet();
        }
        latencySamples.add(latencyMillis);
        if (latencySamples.size() > 1000) {
            latencySamples.remove(0);
        }
        latencySummary.record(latencyMillis);
    }

    public synchronized TelemetrySnapshot snapshot(int activeTrafficRps, boolean cacheEnabled, long eventCount) {
        long total = totalRequests.get();
        long allowed = allowedRequests.get();
        long rejected = rejectedRequests.get();
        long cacheTotal = cacheHits.get() + cacheMisses.get();
        return new TelemetrySnapshot(
                total,
                allowed,
                rejected,
                total == 0 ? 1 : (double) allowed / total,
                total == 0 ? 0 : (double) rejected / total,
                percentile(0.95),
                cacheTotal == 0 ? 0 : (double) cacheHits.get() / cacheTotal,
                activeTrafficRps,
                cacheEnabled,
                eventCount);
    }

    public synchronized void reset() {
        totalRequests.set(0);
        allowedRequests.set(0);
        rejectedRequests.set(0);
        cacheHits.set(0);
        cacheMisses.set(0);
        latencySamples.clear();
    }

    private double percentile(double percentile) {
        if (latencySamples.isEmpty()) {
            return 0;
        }
        List<Double> sorted = latencySamples.stream().sorted(Comparator.naturalOrder()).toList();
        int index = (int) Math.ceil(percentile * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }
}
