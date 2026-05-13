package com.architectlab.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.architectlab.command.CommandType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class CommandAuthorizationServiceTest {
    private final CommandAuthorizationService service = new CommandAuthorizationService();

    @Test
    void studentCanStartTrafficButCannotInjectLatency() {
        var student = authentication("ROLE_STUDENT");

        assertThat(service.canExecute(CommandType.START_TRAFFIC, student)).isTrue();
        assertThat(service.canExecute(CommandType.INJECT_LATENCY, student)).isFalse();
    }

    @Test
    void adminCanRunFailureInjectionCommands() {
        var admin = authentication("ROLE_ADMIN");

        assertThat(service.canExecute(CommandType.INJECT_LATENCY, admin)).isTrue();
        assertThat(service.canExecute(CommandType.DISABLE_CACHE, admin)).isTrue();
    }

    private UsernamePasswordAuthenticationToken authentication(String role) {
        return new UsernamePasswordAuthenticationToken("demo", null, List.of(new SimpleGrantedAuthority(role)));
    }
}
