package com.architectlab.command;

import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EnableCacheCommand implements LabCommand {
    @Override
    public CommandType commandName() {
        return CommandType.ENABLE_CACHE;
    }

    @Override
    public CommandResult execute(CommandContext context, CommandRequest request) {
        context.simulationState().cacheEnabled(true);
        return new CommandResult(commandName(), true, "Redis-style cache enabled", Instant.now(), Map.of("cacheEnabled", true));
    }
}
