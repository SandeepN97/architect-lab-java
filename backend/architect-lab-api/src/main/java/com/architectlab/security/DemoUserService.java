package com.architectlab.security;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DemoUserService {
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Map<String, DemoUser> users = Map.of(
            "student", new DemoUser("student", passwordEncoder.encode("student123"), List.of("STUDENT")),
            "admin", new DemoUser("admin", passwordEncoder.encode("admin123"), List.of("ADMIN", "STUDENT")),
            "observer", new DemoUser("observer", passwordEncoder.encode("observer123"), List.of("OBSERVER")));

    public Optional<UserProfile> authenticate(String username, String password) {
        DemoUser user = users.get(username);
        if (user == null || !passwordEncoder.matches(password, user.passwordHash())) {
            return Optional.empty();
        }
        return Optional.of(new UserProfile(user.username(), user.roles()));
    }

    public List<UserProfile> demoUsers() {
        return users.values().stream().map(user -> new UserProfile(user.username(), user.roles())).toList();
    }

    private record DemoUser(String username, String passwordHash, List<String> roles) {
    }
}
