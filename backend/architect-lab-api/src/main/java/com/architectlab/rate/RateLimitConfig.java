package com.architectlab.rate;

public record RateLimitConfig(RateLimitAlgorithm algorithm, int requestsPerWindow, int windowSeconds, boolean cacheEnabled) {
    public static RateLimitConfig defaults() {
        return new RateLimitConfig(RateLimitAlgorithm.TOKEN_BUCKET, 100, 60, true);
    }
}
