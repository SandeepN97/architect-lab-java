package com.architectlab.command;

import com.architectlab.event.EventPublisher;
import com.architectlab.rate.RateLimiterService;
import com.architectlab.telemetry.TelemetryService;

public record CommandContext(
        SimulationState simulationState,
        RateLimiterService rateLimiterService,
        TelemetryService telemetryService,
        EventPublisher eventPublisher,
        String actor) {
}
