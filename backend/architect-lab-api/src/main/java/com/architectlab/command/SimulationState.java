package com.architectlab.command;

import com.architectlab.rate.RateLimitAlgorithm;
import com.architectlab.rate.RateLimitConfig;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class SimulationState {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean cacheEnabled = new AtomicBoolean(true);
    private final AtomicInteger activeTrafficRps = new AtomicInteger(0);
    private final AtomicInteger injectedLatencyMillis = new AtomicInteger(15);
    private volatile RateLimitAlgorithm algorithm = RateLimitAlgorithm.TOKEN_BUCKET;
    private volatile int limit = 100;
    private volatile int windowSeconds = 60;

    public boolean isRunning() {
        return running.get();
    }

    public void start(int rps) {
        running.set(true);
        activeTrafficRps.set(Math.max(0, rps));
    }

    public void stop() {
        running.set(false);
        activeTrafficRps.set(0);
    }

    public boolean cacheEnabled() {
        return cacheEnabled.get();
    }

    public void cacheEnabled(boolean enabled) {
        cacheEnabled.set(enabled);
    }

    public int activeTrafficRps() {
        return activeTrafficRps.get();
    }

    public int injectedLatencyMillis() {
        return injectedLatencyMillis.get();
    }

    public void injectedLatencyMillis(int latencyMillis) {
        injectedLatencyMillis.set(Math.max(0, latencyMillis));
    }

    public RateLimitConfig rateLimitConfig() {
        return new RateLimitConfig(algorithm, limit, windowSeconds, cacheEnabled());
    }

    public void configureRateLimiter(RateLimitAlgorithm algorithm, int limit, int windowSeconds) {
        this.algorithm = algorithm;
        this.limit = Math.max(1, limit);
        this.windowSeconds = Math.max(1, windowSeconds);
    }
}
