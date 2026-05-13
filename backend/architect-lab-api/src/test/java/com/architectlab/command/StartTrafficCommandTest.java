package com.architectlab.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class StartTrafficCommandTest {
    @Test
    void readsRpsParameter() {
        CommandRequest request = new CommandRequest(CommandType.START_TRAFFIC, Map.of("rps", 5000));

        assertThat(StartTrafficCommand.parameterAsInt(request, "rps", 250)).isEqualTo(5000);
    }

    @Test
    void fallsBackForInvalidParameter() {
        CommandRequest request = new CommandRequest(CommandType.START_TRAFFIC, Map.of("rps", "fast"));

        assertThat(StartTrafficCommand.parameterAsInt(request, "rps", 250)).isEqualTo(250);
    }
}
