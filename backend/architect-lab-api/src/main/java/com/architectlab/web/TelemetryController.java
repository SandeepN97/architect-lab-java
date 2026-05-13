package com.architectlab.web;

import com.architectlab.command.SimulationState;
import com.architectlab.event.EventPublisher;
import com.architectlab.telemetry.TelemetryService;
import com.architectlab.telemetry.TelemetrySnapshot;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {
    private final TelemetryService telemetryService;
    private final SimulationState simulationState;
    private final EventPublisher eventPublisher;

    public TelemetryController(TelemetryService telemetryService, SimulationState simulationState, EventPublisher eventPublisher) {
        this.telemetryService = telemetryService;
        this.simulationState = simulationState;
        this.eventPublisher = eventPublisher;
    }

    @GetMapping
    public TelemetrySnapshot snapshot() {
        return telemetryService.snapshot(simulationState.activeTrafficRps(), simulationState.cacheEnabled(), eventPublisher.count());
    }
}
