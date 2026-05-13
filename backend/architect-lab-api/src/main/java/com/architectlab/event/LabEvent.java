package com.architectlab.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record LabEvent(UUID id, String type, String actor, String lab, Instant timestamp, Map<String, Object> payload) {
    public static LabEvent of(String type, String actor, String lab, Map<String, Object> payload) {
        return new LabEvent(UUID.randomUUID(), type, actor, lab, Instant.now(), payload);
    }
}
