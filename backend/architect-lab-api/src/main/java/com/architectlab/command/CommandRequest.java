package com.architectlab.command;

import java.util.Map;

public record CommandRequest(CommandType type, Map<String, Object> parameters) {
}
