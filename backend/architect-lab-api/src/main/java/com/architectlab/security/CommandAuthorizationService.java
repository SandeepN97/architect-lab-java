package com.architectlab.security;

import com.architectlab.command.CommandType;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class CommandAuthorizationService {
    private static final Set<CommandType> ADMIN_ONLY_COMMANDS = EnumSet.of(
            CommandType.ENABLE_CACHE,
            CommandType.DISABLE_CACHE,
            CommandType.INJECT_LATENCY,
            CommandType.RESET_LAB);

    public boolean canExecute(CommandType commandType, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (ADMIN_ONLY_COMMANDS.contains(commandType)) {
            return hasRole(authentication, "ROLE_ADMIN");
        }
        return hasRole(authentication, "ROLE_ADMIN") || hasRole(authentication, "ROLE_STUDENT");
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(role::equals);
    }
}
