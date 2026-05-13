package com.architectlab.rate;

public record RateLimitDecision(boolean allowed, long remaining, long retryAfterMillis, RateLimitAlgorithm algorithm) {
}
