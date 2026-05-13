package com.architectlab.command;

import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DisableCacheCommand implements LabCommand {
    @Override
    public CommandType commandName() {
        return CommandType.DISABLE_CACHE;
    }

    @Override
    public CommandResult execute(CommandContext context, CommandRequest request) {
        context.simulationState().cacheEnabled(false);
        return new CommandResult(commandName(), true, "Redis-style cache disabled", Instant.now(), Map.of("cacheEnabled", false));
    }
}
