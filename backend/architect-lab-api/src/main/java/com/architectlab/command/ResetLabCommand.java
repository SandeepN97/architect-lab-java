package com.architectlab.command;

import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ResetLabCommand implements LabCommand {
    @Override
    public CommandType commandName() {
        return CommandType.RESET_LAB;
    }

    @Override
    public CommandResult execute(CommandContext context, CommandRequest request) {
        context.simulationState().stop();
        context.rateLimiterService().reset();
        context.telemetryService().reset();
        return new CommandResult(commandName(), true, "Rate limiter lab reset", Instant.now(), Map.of("activeTrafficRps", 0));
    }
}
