package com.architectlab.command;

import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class InjectLatencyCommand implements LabCommand {
    @Override
    public CommandType commandName() {
        return CommandType.INJECT_LATENCY;
    }

    @Override
    public CommandResult execute(CommandContext context, CommandRequest request) {
        int latencyMillis = StartTrafficCommand.parameterAsInt(request, "latencyMillis", 250);
        context.simulationState().injectedLatencyMillis(latencyMillis);
        return new CommandResult(commandName(), true, "Injected service latency set to " + latencyMillis + " ms", Instant.now(), Map.of("latencyMillis", latencyMillis));
    }
}
