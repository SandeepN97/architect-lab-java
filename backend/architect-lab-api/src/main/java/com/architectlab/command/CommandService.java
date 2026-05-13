package com.architectlab.command;

import com.architectlab.event.EventPublisher;
import com.architectlab.event.LabEvent;
import com.architectlab.rate.RateLimitDecision;
import com.architectlab.rate.RateLimiterService;
import com.architectlab.telemetry.TelemetryService;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CommandService {
    private final Map<CommandType, LabCommand> commands;
    private final SimulationState simulationState;
    private final RateLimiterService rateLimiterService;
    private final TelemetryService telemetryService;
    private final EventPublisher eventPublisher;

    public CommandService(List<LabCommand> commands, SimulationState simulationState, RateLimiterService rateLimiterService, TelemetryService telemetryService, EventPublisher eventPublisher) {
        this.commands = commands.stream().collect(Collectors.toMap(LabCommand::commandName, Function.identity()));
        this.simulationState = simulationState;
        this.rateLimiterService = rateLimiterService;
        this.telemetryService = telemetryService;
        this.eventPublisher = eventPublisher;
    }

    public CommandResult execute(CommandRequest request, Principal principal) {
        LabCommand command = commands.get(request.type());
        if (command == null) {
            throw new IllegalArgumentException("Unsupported command " + request.type());
        }
        String actor = principal == null ? "anonymous" : principal.getName();
        CommandResult result = command.execute(new CommandContext(simulationState, rateLimiterService, telemetryService, eventPublisher, actor), request);
        eventPublisher.publish(LabEvent.of("COMMAND_EXECUTED", actor, "rate-limiter", Map.of("command", request.type().name(), "accepted", result.accepted())));
        return result;
    }

    @Scheduled(fixedRate = 1000)
    public void tickTrafficSimulation() {
        if (!simulationState.isRunning() || simulationState.activeTrafficRps() == 0) {
            return;
        }
        int samples = Math.min(simulationState.activeTrafficRps(), 1000);
        for (int i = 0; i < samples; i++) {
            RateLimitDecision decision = rateLimiterService.allow("traffic-user-" + (i % 25), simulationState.rateLimitConfig());
            boolean cacheHit = simulationState.cacheEnabled() && i % 5 != 0;
            double latency = simulationState.injectedLatencyMillis() + (decision.allowed() ? 8 : 2) + (cacheHit ? 0 : 20);
            telemetryService.recordRequest(decision.allowed(), latency, cacheHit);
        }
        eventPublisher.publish(LabEvent.of("TRAFFIC_TICK", "simulation-engine", "rate-limiter", Map.of("rps", simulationState.activeTrafficRps(), "sampledRequests", samples)));
    }
}
