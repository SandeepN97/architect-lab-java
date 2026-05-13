package com.architectlab.rate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RateLimiterServiceTest {
    @Test
    void fixedWindowRejectsAfterLimit() {
        RateLimiterService service = new RateLimiterService();
        RateLimitConfig config = new RateLimitConfig(RateLimitAlgorithm.FIXED_WINDOW, 2, 60, true);

        assertThat(service.allow("alice", config).allowed()).isTrue();
        assertThat(service.allow("alice", config).allowed()).isTrue();
        assertThat(service.allow("alice", config).allowed()).isFalse();
    }

    @Test
    void slidingWindowTracksPrincipalsIndependently() {
        RateLimiterService service = new RateLimiterService();
        RateLimitConfig config = new RateLimitConfig(RateLimitAlgorithm.SLIDING_WINDOW, 1, 60, true);

        assertThat(service.allow("alice", config).allowed()).isTrue();
        assertThat(service.allow("alice", config).allowed()).isFalse();
        assertThat(service.allow("bob", config).allowed()).isTrue();
    }
}
