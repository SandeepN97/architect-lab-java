package com.architectlab.command;

import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class StopTrafficCommand implements LabCommand {
    @Override
    public CommandType commandName() {
        return CommandType.STOP_TRAFFIC;
    }

    @Override
    public CommandResult execute(CommandContext context, CommandRequest request) {
        context.simulationState().stop();
        return new CommandResult(commandName(), true, "Traffic simulation stopped", Instant.now(), Map.of("activeTrafficRps", 0));
    }
}
