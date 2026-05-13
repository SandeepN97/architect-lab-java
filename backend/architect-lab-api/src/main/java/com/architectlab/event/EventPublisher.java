package com.architectlab.event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {
    private final List<LabEvent> events = new ArrayList<>();

    public synchronized LabEvent publish(LabEvent event) {
        events.add(event);
        if (events.size() > 250) {
            events.remove(0);
        }
        return event;
    }

    public synchronized List<LabEvent> recent() {
        return events.stream().sorted(Comparator.comparing(LabEvent::timestamp).reversed()).toList();
    }

    public synchronized long count() {
        return events.size();
    }
}
