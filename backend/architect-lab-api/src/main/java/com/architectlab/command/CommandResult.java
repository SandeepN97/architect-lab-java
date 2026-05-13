package com.architectlab.command;

import java.time.Instant;
import java.util.Map;

public record CommandResult(CommandType type, boolean accepted, String message, Instant executedAt, Map<String, Object> state) {
}
