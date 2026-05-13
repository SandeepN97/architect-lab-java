package com.architectlab.command;

import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class StartTrafficCommand implements LabCommand {
    @Override
    public CommandType commandName() {
        return CommandType.START_TRAFFIC;
    }

    @Override
    public CommandResult execute(CommandContext context, CommandRequest request) {
        int rps = parameterAsInt(request, "rps", 250);
        context.simulationState().start(rps);
        return new CommandResult(commandName(), true, "Traffic simulation started at " + rps + " RPS", Instant.now(), Map.of("activeTrafficRps", rps));
    }

    static int parameterAsInt(CommandRequest request, String name, int fallback) {
        Object value = request.parameters() == null ? null : request.parameters().get(name);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String string) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }
}
