package com.architectlab.rate;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {
    private final Clock clock;
    private final Map<String, FixedWindowBucket> fixedWindows = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> slidingWindows = new ConcurrentHashMap<>();
    private final Map<String, TokenBucket> tokenBuckets = new ConcurrentHashMap<>();

    public RateLimiterService() {
        this(Clock.systemUTC());
    }

    RateLimiterService(Clock clock) {
        this.clock = clock;
    }

    public RateLimitDecision allow(String principal, RateLimitConfig config) {
        return switch (config.algorithm()) {
            case FIXED_WINDOW -> fixedWindow(principal, config);
            case SLIDING_WINDOW -> slidingWindow(principal, config);
            case TOKEN_BUCKET -> tokenBucket(principal, config);
        };
    }

    public void reset() {
        fixedWindows.clear();
        slidingWindows.clear();
        tokenBuckets.clear();
    }

    private RateLimitDecision fixedWindow(String principal, RateLimitConfig config) {
        long now = clock.millis();
        long windowMillis = config.windowSeconds() * 1000L;
        FixedWindowBucket bucket = fixedWindows.compute(principal, (key, existing) -> {
            if (existing == null || now >= existing.windowStartMillis + windowMillis) {
                return new FixedWindowBucket(now, 0);
            }
            return existing;
        });
        synchronized (bucket) {
            if (bucket.count < config.requestsPerWindow()) {
                bucket.count++;
                return new RateLimitDecision(true, config.requestsPerWindow() - bucket.count, 0, config.algorithm());
            }
            return new RateLimitDecision(false, 0, bucket.windowStartMillis + windowMillis - now, config.algorithm());
        }
    }

    private RateLimitDecision slidingWindow(String principal, RateLimitConfig config) {
        long now = clock.millis();
        long cutoff = now - config.windowSeconds() * 1000L;
        Deque<Long> timestamps = slidingWindows.computeIfAbsent(principal, ignored -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= cutoff) {
                timestamps.removeFirst();
            }
            if (timestamps.size() < config.requestsPerWindow()) {
                timestamps.addLast(now);
                return new RateLimitDecision(true, config.requestsPerWindow() - timestamps.size(), 0, config.algorithm());
            }
            long retryAfter = timestamps.peekFirst() + config.windowSeconds() * 1000L - now;
            return new RateLimitDecision(false, 0, retryAfter, config.algorithm());
        }
    }

    private RateLimitDecision tokenBucket(String principal, RateLimitConfig config) {
        long now = clock.millis();
        TokenBucket bucket = tokenBuckets.computeIfAbsent(principal, ignored -> new TokenBucket(config.requestsPerWindow(), now));
        synchronized (bucket) {
            refill(bucket, config, now);
            if (bucket.tokens >= 1) {
                bucket.tokens -= 1;
                return new RateLimitDecision(true, (long) bucket.tokens, 0, config.algorithm());
            }
            double tokensPerMillis = (double) config.requestsPerWindow() / (config.windowSeconds() * 1000L);
            long retryAfter = (long) Math.ceil((1 - bucket.tokens) / tokensPerMillis);
            return new RateLimitDecision(false, 0, retryAfter, config.algorithm());
        }
    }

    private void refill(TokenBucket bucket, RateLimitConfig config, long now) {
        long elapsed = Math.max(0, now - bucket.lastRefillMillis);
        double tokensPerMillis = (double) config.requestsPerWindow() / (config.windowSeconds() * 1000L);
        bucket.tokens = Math.min(config.requestsPerWindow(), bucket.tokens + elapsed * tokensPerMillis);
        bucket.lastRefillMillis = now;
    }

    private static final class FixedWindowBucket {
        private final long windowStartMillis;
        private int count;

        private FixedWindowBucket(long windowStartMillis, int count) {
            this.windowStartMillis = windowStartMillis;
            this.count = count;
        }
    }

    private static final class TokenBucket {
        private double tokens;
        private long lastRefillMillis;

        private TokenBucket(double tokens, long lastRefillMillis) {
            this.tokens = tokens;
            this.lastRefillMillis = lastRefillMillis;
        }
    }
}
